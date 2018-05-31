package analysis.exercise3;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import analysis.exercise1.CHAAlgorithm;
import soot.Body;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.NewExpr;
import soot.jimple.Ref;

public class VTAAlgorithm extends CallGraphAlgorithm {

	private final RefTypeGraph graph = new RefTypeGraph();
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
		System.out.println("------------");
		this.typeGraph.printMe();
		System.out.println("------------");
		graph.finaliseTypePropagationGraph();
		System.out.println("all nodes: " + graph.getNodes());
		entries = getEntryPoints(scene);
		entries.forEach(e -> {
			if (e.getSignature().contains("exercise3")) {

				if (e.hasActiveBody()) {

					Set<SootMethod> outEdges = initialCallGraph.edgesOutOf(e);
					List<RefTypeNode> nodeTypes = graph.getNodesForMethod(e);

					System.out.println("method: " + e);
					System.out.println("node list: " + graph.getNodesForMethod(e));
					List<RefType> allTypes = this.typeGraph.getAllTypes();
					cg.addNode(e);
					outEdges.forEach(edge -> {

						// check if edge should be contained in call graph
						// graph.getNodeByRef();
						System.out.println("out edge: " + edge);
						//System.out.println("declaring class: " + edge.getDeclaringClass().getType());
						for(RefType t : allTypes) {
							System.out.println("comparing type: " + t + " to type " +  edge.getDeclaringClass().getType());
							if(t == edge.getDeclaringClass().getType()) {
								System.out.println("Found matching type !");
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
				System.out.println("unit: " + unit);
				if (unit instanceof AssignStmt) {
					AssignStmt ass = (AssignStmt) unit;
					Value leftOp = ass.getLeftOp();
					this.typeGraph.addNode(leftOp);
					System.out.println("is assignment");
					System.out.println("leftOp: " + leftOp);
					System.out.println("rightOp: " + ass.getRightOp() + "\n");
					Value rightOp = ass.getRightOp();
//					if(rightOp instanceof Ref) {
//						System.out.println("have to set alias");
//					}
//					if(rightOp instanceof Expr) {
//						System.out.println("have to resolve expr type");
//					}
					// if (leftOp instanceof FieldRef) {
					// System.out.println("left is field ref");
					// FieldRef refLeft = (FieldRef) leftOp;
					// System.out.println("refLeft: " + refLeft + "\n");

					if (ass.containsInvokeExpr()) {
						// if(ass.getInvokeExpr() instanceof SpecialInvokeExpr) {
						// System.out.println("I should add this class to type graph");
						// SpecialInvokeExpr spI = (SpecialInvokeExpr) ass.getInvokeExpr();
						// System.out.println("special invoke: " + spI);
						// }
						System.out.println("contains invoke expr");
						SootMethod method = ass.getInvokeExpr().getMethod();
						Type returnType = method.getReturnType();
						System.out.println("invoke method: " + method);
						System.out.println("return type: " + returnType);
						
						// if (graph.containsNode(refLeft)) {
						// RefTypeNode nodeContained = graph.getNodeByRef(refLeft);
						// nodeContained.addType(returnType);
						// } else {
						//
						// RefTypeNode node = new RefTypeNode(refLeft);
						// graph.addNode(node, m);
						// }
						contructInitGraph(method, leftOp);

					} else {
						System.out.println("doesn't contain invoke expr");
						if (rightOp instanceof NewExpr) {
							NewExpr newExpr = (NewExpr) rightOp;
							RefType type = newExpr.getBaseType();
							System.out.println("found new expr: " + newExpr);
							System.out.println("base type: " + type);
							
							if(recursiveLeft != null) {
								System.out.println("I am a recirsive call and therefore adding the type to: " + recursiveLeft);
								this.typeGraph.addTypeToNode(type, recursiveLeft);
							}
							else {
								System.out.println("I am a normal call and add type to: " + leftOp);
								this.typeGraph.addTypeToNode(type, leftOp);
							}
//							if (leftOp instanceof FieldRef) {
//								System.out.println("left is field ref2");
//								FieldRef refLeft = (FieldRef) leftOp;
//								System.out.println("refLeft: " + refLeft + "\n");
//							}

						}
						else {
							System.out.println("I have to add an edge");
							this.typeGraph.addEdge(leftOp, rightOp);
						}
						// if (ass.getRightOp() instanceof FieldRef) {
						// FieldRef refRight = (FieldRef) ass.getRightOp();
						// //graph.addEdgeByRefs(refRight, refLeft);
						// }
					}
					// }
				}
				System.out.println("\n");
			}
		}
	}

}
