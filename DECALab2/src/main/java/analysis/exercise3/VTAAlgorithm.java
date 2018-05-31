package analysis.exercise3;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import analysis.exercise1.CHAAlgorithm;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;

public class VTAAlgorithm extends CallGraphAlgorithm {

	private final TypeGraph typeGraph = new TypeGraph();

	@Override
	protected String getAlgorithm() {
		return "VTA";
	}

	@Override
	protected void populateCallGraph(Scene scene, CallGraph cg) {
		CallGraph initialCallGraph = new CHAAlgorithm().constructCallGraph(scene);

		// Your implementation goes here, also feel free to add methods as needed
		// To get your entry points we prepared getEntryPoints(scene) in the superclass
		// for you

		Stream<SootMethod> entries = getEntryPoints(scene);
		entries.forEach(e -> {
			if (e.getSignature().contains("exercise3")) {
				contructInitGraph(e, null);
			}
		});

		entries = getEntryPoints(scene);
		entries.forEach(e -> {
			if (e.getSignature().contains("exercise3")) {

				if (e.hasActiveBody()) {

					Set<SootMethod> outEdges = initialCallGraph.edgesOutOf(e);

					List<RefType> allTypes = this.typeGraph.getAllTypes();
					cg.addNode(e);
					outEdges.forEach(edge -> {
						for (RefType t : allTypes) {

							if (t == edge.getDeclaringClass().getType()) {

								cg.addNode(edge);
								cg.addEdge(e, edge);
							}
						}
					}

					);
				}
			}
		});
	}

	private void contructInitGraph(SootMethod m, Value recursiveLeft) {

		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {
		
				if (unit instanceof AssignStmt) {
					AssignStmt ass = (AssignStmt) unit;
					Value leftOp = ass.getLeftOp();
					this.typeGraph.addNode(leftOp);

					Value rightOp = ass.getRightOp();

					if (ass.containsInvokeExpr()) {

						SootMethod method = ass.getInvokeExpr().getMethod();

						contructInitGraph(method, leftOp);

					} else {

						if (rightOp instanceof NewExpr) {
							NewExpr newExpr = (NewExpr) rightOp;
							RefType type = newExpr.getBaseType();

							if (recursiveLeft != null) {

								this.typeGraph.addTypeToNode(type, recursiveLeft);
							} else {

								this.typeGraph.addTypeToNode(type, leftOp);
							}

						} else {
							this.typeGraph.addEdge(leftOp, rightOp);
						}

					}

				}
			}
		}
	}

}
