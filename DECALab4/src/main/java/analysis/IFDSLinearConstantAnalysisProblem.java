package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.Pair;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.MulExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public class IFDSLinearConstantAnalysisProblem extends DefaultJimpleIFDSTabulationProblem<Pair<Local, Integer>, InterproceduralCFG<Unit, SootMethod>> {

    protected final static int LOWER_BOUND = -1000;

    protected final static int UPPER_BOUND = 1000;

    protected InterproceduralCFG<Unit, SootMethod> icfg;

    public IFDSLinearConstantAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
        super(icfg);
        this.icfg = icfg;
    }

    @Override
    public Map<Unit, Set<Pair<Local, Integer>>> initialSeeds() {
        for (SootClass c : Scene.v().getApplicationClasses()) {
            for (SootMethod m : c.getMethods()) {
                if (!m.hasActiveBody()) {
                    continue;
                }
                if (m.getName().equals("entryPoint")) {
                    return DefaultSeeds.make(Collections.singleton(m.getActiveBody().getUnits().getFirst()), zeroValue());
                }
            }
        }
        throw new IllegalStateException("scene does not contain 'entryPoint'");
    }

    // TODO: You have to implement the FlowFunctions interface.
    // Use Pair<Local, Integer> as type for the data-flow facts.
    @Override
    protected FlowFunctions<Unit, Pair<Local, Integer>, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Unit, Pair<Local, Integer>, SootMethod>() {
            
            
            @Override
            public FlowFunction<Pair<Local, Integer>> getNormalFlowFunction(Unit curr, Unit next) {
                // TODO: Implement this flow function factory to obtain an intra-procedural data-flow analysis.

               
                if (curr instanceof AssignStmt) {
                    AssignStmt assignStmt = (AssignStmt) curr;
                    Local leftLocal = null;
                    if (assignStmt.getLeftOp() instanceof Local) {
                        leftLocal = (Local) assignStmt.getLeftOp();
                    }
                    if ((assignStmt.getRightOp() instanceof IntConstant) || (assignStmt.getRightOp() instanceof JAddExpr) || assignStmt.getRightOp() instanceof JMulExpr) {
                    
                        // TODO: provide not null, but a sensible value to evaluateExpression
                        int result = evaluateExpression(assignStmt.getRightOp(), null);
                        if (leftLocal != null) {
                            Pair pair = new Pair<Local, Integer>(leftLocal, result);
                            //TODO return a Flowfunction
                        } else {
                            throw new UnsupportedOperationException("test");
                        }

                    }

                }
                
                return Identity.v();
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getCallFlowFunction(Unit callsite, SootMethod dest) {
                // TODO: Implement this flow function factory to map the actual into the formal arguments.
                // Caution, actual parameters may be integer literals as well.
                return Identity.v();
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getReturnFlowFunction(Unit callsite, SootMethod callee, Unit exit, Unit retsite) {
                // TODO: Map the return value back into the caller's context if applicable.
                // Since Java has pass-by-value semantics for primitive data types, you do not have to map the formals
                // back to the actuals at the exit of the callee.
                return Identity.v();
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getCallToReturnFlowFunction(Unit callsite, Unit retsite) {
                // TODO: getCallToReturnFlowFunction can be left to return id in many analysis; this time as well?
                return Identity.v();
            }
        };
    }

    /**
     * Evaluate the given expression given as a Value (depending on what subclass of Value expression is).
     * @param expr
     * @param dataFlowFacts
     * @return An integer that comes out of the evaluation.
     */
    private int evaluateExpression(Value expr, List<Pair<Local,Integer>> dataFlowFacts) {

        if (expr instanceof IntConstant) {
            return ((IntConstant) expr).value;
        } else if (expr instanceof Local) {
            Local local = (Local) expr;
            for(Pair<Local, Integer> pair: dataFlowFacts) {
                if(pair.getO1().getName().equals(local.getName())) {
                    return pair.getO2();
                }
            }
        } else if (expr instanceof JAddExpr) {
            JAddExpr jAddExpr = (JAddExpr) expr;
            if (jAddExpr.getSymbol().contains("+")) {
                return evaluateExpression(jAddExpr.getOp1(),dataFlowFacts) + evaluateExpression(jAddExpr.getOp2(),dataFlowFacts);
            } else if (jAddExpr.getSymbol().contains("-")) {
                return evaluateExpression(jAddExpr.getOp1(),dataFlowFacts) - evaluateExpression(jAddExpr.getOp2(),dataFlowFacts);
            }
        } else if (expr instanceof JMulExpr) {
            JMulExpr jMulExpr = (JMulExpr) expr;
            if (jMulExpr.getSymbol().contains("*")) {
                return evaluateExpression(jMulExpr.getOp1(),dataFlowFacts) * evaluateExpression(jMulExpr.getOp2(),dataFlowFacts);
            } else if (jMulExpr.getSymbol().contains("/")) {
                return evaluateExpression(jMulExpr.getOp1(),dataFlowFacts) / evaluateExpression(jMulExpr.getOp2(),dataFlowFacts);
            }
        }
        throw new IllegalArgumentException("evaluateExpr failed: " + expr.getClass());
    }

    @Override
    protected Pair<Local, Integer> createZeroValue() {
        return new Pair<>(new JimpleLocal("<<zero>>", NullType.v()), Integer.MIN_VALUE);
    }

}
