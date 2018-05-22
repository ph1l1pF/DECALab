package analysis.exercise2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.exercise1.CHAAlgorithm;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

public class RTAAlgorithm extends CHAAlgorithm {

	private Set<SootClass> classesInstanciated = new HashSet<>();

	@Override
	protected String getAlgorithm() {
		return "RTA";
	}

	@Override
	protected void populateCallGraph(Scene scene, CallGraph cg) {
		// Your implementation goes here, also feel free to add methods as needed
		// To get your entry points we prepared getEntryPoints(scene) in the superclass
		// for you

		Stream<SootMethod> stream = this.getEntryPoints(scene);

		List<SootMethod> entryPoints = new ArrayList<>();
		stream.forEach(m -> {
			if (m.getSignature().contains("exercise2")) {
				entryPoints.add(m);
			}
		});

		entryPoints.forEach(m -> {

			findClassesInstanciated(m);
		});

		classesInstanciated.forEach(c -> System.out.println(c));

		Hierarchy h = scene.getActiveHierarchy();
		entryPoints.forEach(m -> {
			addNodeSafely(cg, m);
			findAndAddSuccessors(m, h, cg);
		});

	}

	private void findClassesInstanciated(SootMethod m) {
		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {
				SootMethod calledMethod = extractCalledMethodFromUnit(unit);
				if (calledMethod == null) {
					continue;
				}
				if (calledMethod.isConstructor()) {
					classesInstanciated.add(calledMethod.getDeclaringClass());
				} else {
					findClassesInstanciated(calledMethod);
				}

			}
		}
	}

	private SootMethod extractCalledMethodFromUnit(Unit unit) {
		SootMethod method = null;
		if (unit instanceof JAssignStmt) {
			JAssignStmt ass = (JAssignStmt) unit;
			if (ass.containsInvokeExpr()) {
				method = ass.getInvokeExpr().getMethod();
			}
		} else if (unit instanceof JInvokeStmt) {
			JInvokeStmt inv = (JInvokeStmt) unit;
			method = inv.getInvokeExpr().getMethod();
		}
		return method;
	}

	/**
	 * Search through method m for other called methods m' in the body of m. Add
	 * edges (m,m') and make recursive calls for those found methods m'.
	 * 
	 * @param m
	 *            The method we are currently going through.
	 * @param h
	 *            The hierarchy needed to find subclasses.
	 * @param cg
	 *            The call graph which we nodes to edges to.
	 */
	private void findAndAddSuccessors(SootMethod m, Hierarchy h, CallGraph cg) {
		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {

				// find the method that is called by unit, if no method is called, go on with
				// for loop
				SootMethod rootMethod = extractCalledMethodFromUnit(unit);
				if (rootMethod == null) {
					continue;
				}

				// here we no that a called method was found
				List<SootMethod> calledMethods = new ArrayList<>();
				calledMethods.add(rootMethod);

				// due to the possibility of polymorphic calls, we need to search for methods in
				// sub classes too
				SootClass declaringClass = rootMethod.getDeclaringClass();
				if (!rootMethod.isConstructor()) {
					List<SootClass> childClasses = new ArrayList<>();
					if (declaringClass.isInterface()) {
						childClasses = h.getDirectImplementersOf(declaringClass);
					} else {
						childClasses = h.getDirectSubclassesOf(declaringClass);
					}
					for (SootClass childClass : childClasses) {
						if (classesInstanciated.contains(childClass)) {
							for (SootMethod childMethod : childClass.getMethods()) {
								if (childMethod.getSubSignature().equals(rootMethod.getSubSignature())) {
									calledMethods.add(childMethod);
								}
							}
						}

					}
				}

				// now we have found all methods, so we add the nodes and edges to the call
				// graph
				// and then make the recursive calls for each of the methods we found
				for (SootMethod calledMethod : calledMethods) {
					addNodeSafely(cg, calledMethod);
					if (!cg.hasEdge(m, calledMethod)) {
						cg.addEdge(m, calledMethod);
					}
					findAndAddSuccessors(calledMethod, h, cg);
				}

			}
		}
	}

	private void addNodeSafely(CallGraph cg, SootMethod node) {
		if (!cg.hasNode(node)) {
			cg.addNode(node);
		}
	}

}
