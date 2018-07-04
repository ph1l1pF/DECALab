package analysis;

import java.util.*;

import com.beust.jcommander.ParameterException;
import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.Pair;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public class IFDSLinearConstantAnalysisProblem extends DefaultJimpleIFDSTabulationProblem<Pair<Local, Integer>, InterproceduralCFG<Unit, SootMethod>> {

    protected final static int LOWER_BOUND = -1000;

    protected final static int UPPER_BOUND = 1000;

    protected InterproceduralCFG<Unit, SootMethod> icfg;

    private Map<Unit, Set<Pair<Local, Integer>>> unitSetMap;

    public IFDSLinearConstantAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
        super(icfg);
        this.icfg = icfg;
        unitSetMap = initialSeeds();
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


                FlowFunction<Pair<Local, Integer>> flowFunction = new FlowFunction<Pair<Local, Integer>>() {

                    @Override
                    public Set<Pair<Local, Integer>> computeTargets(Pair<Local, Integer> localIntegerPair) {
                        Set<Pair<Local, Integer>> returnSet = new HashSet<>();
                        returnSet.addAll(getDataFlowFactsFromUnit(curr));


                        if (curr instanceof AssignStmt) {
                            AssignStmt assignStmt = (AssignStmt) curr;
                            Local leftLocal = null;
                            if (assignStmt.getLeftOp() instanceof Local) {
                                leftLocal = (Local) assignStmt.getLeftOp();
                            }
                            if ((assignStmt.getRightOp() instanceof IntConstant) || (assignStmt.getRightOp() instanceof JAddExpr) || assignStmt.getRightOp() instanceof JMulExpr) {

                                // TODO: provide not null, but a sensible value to evaluateExpression
                                int result = evaluateExpression(assignStmt.getRightOp(), curr);
                                if (leftLocal != null) {
                                    Pair pair = new Pair<Local, Integer>(leftLocal, result);

                                    addNewDataFlowFactToSet(returnSet, pair);
                                } else {
                                    //Do nothing, because no Action necessary for other types of left operators
                                    //throw new UnsupportedOperationException("test");
                                }

                            }

                        }
                        unitSetMap.put(next, returnSet);
                        return returnSet;
                    }


                };

                return flowFunction;
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

    private void addNewDataFlowFactToSet(Set<Pair<Local, Integer>> pairSet, Pair<Local, Integer> paramPair) {
        Set<Pair<Local, Integer>> tmpSet = new HashSet<>();
        tmpSet.addAll(pairSet);
        for (Pair<Local, Integer> pair : tmpSet) {
            if (pair.getO1().getName().equals(paramPair.getO1().getName())) {
                pairSet.remove(pair);
            }
        }
        pairSet.add(paramPair);
    }

    /**
     * Evaluate the given expression given as a Value (depending on what subclass of Value expression is).
     *
     * @param expr
     * @param unit
     * @return An integer that comes out of the evaluation.
     */
    private int evaluateExpression(Value expr, Unit unit) {

        if (expr instanceof IntConstant) {
            return ((IntConstant) expr).value;
        } else if (expr instanceof Local) {
            Local local = (Local) expr;
            List<Integer> integerSet = getDataFlowValueFromUnitAndLocal(unit, local);
            if (integerSet.size() == 0) {
                //Local not defiend
                throw new ParameterException("no DataflowFact for the local: " + local);
            } else if (integerSet.size() == 1) {
                return integerSet.get(0);
            } else {
                throw new ParameterException("more than one DataflowFact for the local: " + local);
            }
        } else if (expr instanceof JAddExpr) {
            JAddExpr jAddExpr = (JAddExpr) expr;
            if (jAddExpr.getSymbol().contains("+")) {
                return evaluateExpression(jAddExpr.getOp1(), unit) + evaluateExpression(jAddExpr.getOp2(), unit);
            } else if (jAddExpr.getSymbol().contains("-")) {
                return evaluateExpression(jAddExpr.getOp1(), unit) - evaluateExpression(jAddExpr.getOp2(), unit);
            }
        } else if (expr instanceof JMulExpr) {
            JMulExpr jMulExpr = (JMulExpr) expr;
            if (jMulExpr.getSymbol().contains("*")) {
                return evaluateExpression(jMulExpr.getOp1(), unit) * evaluateExpression(jMulExpr.getOp2(), unit);
            } else if (jMulExpr.getSymbol().contains("/")) {
                return evaluateExpression(jMulExpr.getOp1(), unit) / evaluateExpression(jMulExpr.getOp2(), unit);
            }
        }
        throw new IllegalArgumentException("evaluateExpr failed: " + expr.getClass());
    }

    private Set<Pair<Local, Integer>> getDataFlowFactsFromUnit(Unit unit) {
        return unitSetMap.get(unit);
    }

    /**
     * Size=0 => kein Dataflow fact existiert für das Local zu diesem Zeitpunkt, sonst size=1
     *
     * @param unit
     * @param local
     * @return
     */
    private List<Integer> getDataFlowValueFromUnitAndLocal(Unit unit, Local local) {
        Set<Pair<Local, Integer>> pairSet = getDataFlowFactsFromUnit(unit);
        List<Integer> integerSet = new ArrayList<>();
        for (Pair<Local, Integer> pair : pairSet) {
            if (pair.getO1().getName().equals(local.getName())) {
                integerSet.add(pair.getO2());
            }
        }
        return integerSet;
    }

    @Override
    protected Pair<Local, Integer> createZeroValue() {
        return new Pair<>(new JimpleLocal("<<zero>>", NullType.v()), Integer.MIN_VALUE);
    }

}
