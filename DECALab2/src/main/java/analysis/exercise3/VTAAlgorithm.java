package analysis.exercise3;

import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import analysis.exercise1.CHAAlgorithm;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;

public class VTAAlgorithm extends CallGraphAlgorithm {
	
	private final RefTypeGraph graph = new RefTypeGraph();

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
				contructInitGraph(e);
			}
		});
		graph.finaliseTypePropagationGraph();
	}

	private void contructInitGraph(SootMethod m) {

		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {
				if(unit instanceof AssignStmt) {
					AssignStmt ass = (AssignStmt) unit;
					Value leftOp = ass.getLeftOp();
					if(leftOp instanceof FieldRef) {
						FieldRef refLeft = (FieldRef) leftOp;
						if (ass.containsInvokeExpr()) {
							SootMethod method = ass.getInvokeExpr().getMethod();
							Type returnType = method.getReturnType();
							
							if(graph.containsNode(refLeft)) {
								RefTypeNode nodeContained = graph.getNodeByRef(refLeft);
								nodeContained.addType(returnType);
							}else {
								RefTypeNode node = new RefTypeNode(refLeft);
								graph.addNode(node);
							}
							contructInitGraph(method);
							
						}else {
							if(ass.getRightOp() instanceof FieldRef) {
								FieldRef refRight = (FieldRef) ass.getRightOp();
								graph.addEdgeByRefs(refRight, refLeft);
							}
						}
					}
				}
			}
		}
	}

}
