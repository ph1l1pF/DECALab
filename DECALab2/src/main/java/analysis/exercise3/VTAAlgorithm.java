package analysis.exercise3;

import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import analysis.exercise1.CHAAlgorithm;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;

public class VTAAlgorithm extends CallGraphAlgorithm {

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
	}

	private void contructInitGraph(SootMethod m) {

		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {
				if(unit instanceof AssignStmt) {
					AssignStmt ass = (AssignStmt) unit;
					Value leftOp = ass.getLeftOp();
					if(leftOp instanceof FieldRef) {
						FieldRef ref = (FieldRef) leftOp;
					}
				}
			}
		}
	}

}
