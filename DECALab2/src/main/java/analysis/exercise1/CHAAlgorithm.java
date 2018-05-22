package analysis.exercise1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

public class CHAAlgorithm extends CallGraphAlgorithm {

	@Override
	protected String getAlgorithm() {
		return "CHA";
	}

	@Override
	protected void populateCallGraph(Scene scene, CallGraph cg) {
		// Your implementation goes here, also feel free to add methods as needed
		// To get your entry points we prepared getEntryPoints(scene) in the superclass
		// for you

		Stream<SootMethod> stream = this.getEntryPoints(scene);
		Hierarchy h = scene.getActiveHierarchy();

		stream.forEach(m -> {
			if (m.getSignature().contains("exercise1")) {
				addNodeSafely(cg, m);
				findAndAddSuccessors(m, h, cg);
			}
		});

	}

	private SootMethod extractCalledMethodFromUnit(Unit unit) {
		SootMethod method = null;
		if (unit instanceof AssignStmt) {
			AssignStmt ass = (JAssignStmt) unit;
			if (ass.containsInvokeExpr()) {
				method = ass.getInvokeExpr().getMethod();
			}
		} else if (unit instanceof InvokeStmt) {
			InvokeStmt inv = (InvokeStmt) unit;
			method = inv.getInvokeExpr().getMethod();
		}
		return method;
	}

	/**
	 * Search through method m for other called methods m' in the body of m. Add
	 * edges (m,m') and make recursive calls for those found methods m'. Works like depth first search.
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
				if (!rootMethod.isConstructor()) {
					List<SootClass> childClasses = getDirectAndIndirectSubClasses(rootMethod.getDeclaringClass(), h);
					
					for (SootClass childClass : childClasses) {
						for (SootMethod childMethod : childClass.getMethods()) {
							if (childMethod.getSubSignature().equals(rootMethod.getSubSignature())) {
								calledMethods.add(childMethod);
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

	/**
	 * Returns a list of direct and indirect subclasses of the given class c.
	 * @param c
	 * @param h
	 * @return
	 */
	private List<SootClass> getDirectAndIndirectSubClasses(SootClass c, Hierarchy h) {
		List<SootClass> childClasses = new ArrayList<>();
		if (c.isInterface()) {
			childClasses = h.getDirectImplementersOf(c);
		} else {
			childClasses = h.getDirectSubclassesOf(c);
		}
		List<SootClass> newList = new ArrayList<>();
		newList.addAll(childClasses);
		for (SootClass cc : childClasses) {
			newList.addAll(getDirectAndIndirectSubClasses(cc, h));
		}

		return newList;
	}

	private void addNodeSafely(CallGraph cg, SootMethod node) {
		if (!cg.hasNode(node)) {
			cg.addNode(node);
		}
	}

}
