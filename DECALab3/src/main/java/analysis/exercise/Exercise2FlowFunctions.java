package analysis.exercise;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import analysis.TaintAnalysisFlowFunctions;
import analysis.VulnerabilityReporter;
import analysis.fact.DataFlowFact;
import heros.FlowFunction;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

public class Exercise2FlowFunctions extends TaintAnalysisFlowFunctions {

    private VulnerabilityReporter reporter;

    public Exercise2FlowFunctions(VulnerabilityReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public FlowFunction<DataFlowFact> getCallFlowFunction(Unit callSite, SootMethod callee) {
        return new FlowFunction<DataFlowFact>() {
            @Override
            public Set<DataFlowFact> computeTargets(DataFlowFact fact) {
                if (fact.equals(DataFlowFact.zero()))
                    return Collections.emptySet();
                prettyPrint(callSite, fact);
                Set<DataFlowFact> out = Sets.newHashSet();
                if (!(callSite instanceof Stmt)) {
                    return out;
                }
                Stmt callSiteStmt = (Stmt) callSite;
                // TODO: Implement Exercise 1c) here
                if (callSiteStmt instanceof InvokeStmt) {

                    InvokeExpr invoke = callSiteStmt.getInvokeExpr();
                    HashSet<Integer> taintedParams = new HashSet<Integer>();
                    for (int i = 0; i < invoke.getArgCount(); i++) {
                        Value v = invoke.getArg(i);
                        if (v.equals(fact.getVariable())) {
                            taintedParams.add(i);
                        }
                    }
                    SootMethod m = invoke.getMethod();
                    for (int i = 0; i < m.getActiveBody().getParameterLocals().size(); i++) {

                        Local l = m.getActiveBody().getParameterLocal(i);

                        if (taintedParams.contains(i)) {
                            out.add(new DataFlowFact(l));
                        }
                    }

                }
                // TODO: Implement interprocedural part of Exercise 2 here
                return out;
            }

        };
    }

    public FlowFunction<DataFlowFact> getCallToReturnFlowFunction(final Unit call, Unit returnSite) {
        return new FlowFunction<DataFlowFact>() {

            @Override
            public Set<DataFlowFact> computeTargets(DataFlowFact val) {

                Set<DataFlowFact> out = Sets.newHashSet();
                Stmt callSiteStmt = (Stmt) call;
                out.add(val);
                modelStringOperations(val, out, callSiteStmt);

                if (val.equals(DataFlowFact.zero())) {
                    // TODO: Implement Exercise 1a) here
                    if (call instanceof AssignStmt) {
                        if (call.toString().contains("getParameter(")) {
                            AssignStmt assign = (AssignStmt) call;
                            if (assign.getLeftOp() instanceof Local) {
                                Local local = (Local) assign.getLeftOp();
                                out.add(new DataFlowFact(local));
                            } else {
                                if (assign.getLeftOp() instanceof FieldRef) {
                                    System.out.println("left is field ref");
                                    FieldRef left = (FieldRef) assign.getLeftOp();
                                    out.add(new DataFlowFact(left.getField()));
                                }
                            }
                        }
                    }
                }
                if (call instanceof Stmt && call.toString().contains("executeQuery")) {
                    Stmt stmt = (Stmt) call;
                    Value arg = stmt.getInvokeExpr().getArg(0);
                    if (val.getVariable().equals(arg)) {
                        reporter.reportVulnerability();
                    }
                }
                return out;
            }
        };
    }

    private void modelStringOperations(DataFlowFact fact, Set<DataFlowFact> out, Stmt callSiteStmt) {
        if (callSiteStmt instanceof AssignStmt && callSiteStmt.toString().contains("java.lang.StringBuilder append(")
                && callSiteStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            Value arg0 = callSiteStmt.getInvokeExpr().getArg(0);
            Value base = ((InstanceInvokeExpr) callSiteStmt.getInvokeExpr()).getBase();
            /*
             * Does the propagated value match the first parameter of the append call or the
             * base variable
             */
            if (fact.getVariable().equals(arg0) || fact.getVariable().equals(base)) {
                /* Yes, then taint the left side of the assignment */
                Value leftOp = ((AssignStmt) callSiteStmt).getLeftOp();
                if (leftOp instanceof Local) {
                    out.add(new DataFlowFact((Local) leftOp));
                }
            }
        }

        /*
         * For any call x = var.toString(), if the base variable var is tainted, then x
         * is tainted.
         */
        if (callSiteStmt instanceof AssignStmt && callSiteStmt.toString().contains("toString()")) {
            if (callSiteStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) callSiteStmt.getInvokeExpr();
                if (fact.getVariable().equals(instanceInvokeExpr.getBase())) {
                    Value leftOp = ((AssignStmt) callSiteStmt).getLeftOp();
                    if (leftOp instanceof Local) {
                        out.add(new DataFlowFact((Local) leftOp));
                    }
                }
            }
        }
    }

    @Override
    public FlowFunction<DataFlowFact> getNormalFlowFunction(final Unit curr, Unit succ) {
        return new FlowFunction<DataFlowFact>() {
            @Override
            public Set<DataFlowFact> computeTargets(DataFlowFact fact) {
                prettyPrint(curr, fact);
                Set<DataFlowFact> out = Sets.newHashSet();
                out.add(fact);

                // TODO: Implement Exercise 1a) here
                if (curr instanceof AssignStmt) {

                    AssignStmt ass = (AssignStmt) curr;
                    DataFlowFact rightVariable = null;

                    if (ass.getRightOp() instanceof Local) {
                        Local rightLocal = (Local) ass.getRightOp();
                        rightVariable = new DataFlowFact(rightLocal);
                    } else {
                        if (ass.getRightOp() instanceof FieldRef) {
                            FieldRef rightLocal = (FieldRef) ass.getRightOp();
                            rightVariable = new DataFlowFact(rightLocal.getField());
                        }
                    }

                    if (out.contains(rightVariable)) {

                        DataFlowFact leftVariable = null;
                        if (ass.getLeftOp() instanceof FieldRef) {
                            FieldRef leftField = (FieldRef) ass.getLeftOp();
                            leftVariable = new DataFlowFact(leftField.getField());
                        } else {
                            if (ass.getLeftOp() instanceof Local) {
                                Local leftLocal = (Local) ass.getLeftOp();
                                leftVariable = new DataFlowFact(leftLocal);
                            }
                        }

                        out.add(leftVariable);
                    }
                }
                // TODO: Implement cases for field load and field store statement of Exercise 2)
                // here
                return out;
            }
        };
    }

    @Override
    public FlowFunction<DataFlowFact> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt,
            Unit retSite) {
        return new FlowFunction<DataFlowFact>() {
            @Override
            public Set<DataFlowFact> computeTargets(DataFlowFact fact) {
                prettyPrint(callSite, fact);
                return Collections.emptySet();
            }
        };
    }

}
