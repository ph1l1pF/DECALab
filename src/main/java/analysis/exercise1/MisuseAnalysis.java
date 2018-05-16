package analysis.exercise1;

import java.util.List;

import analysis.AbstractAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Unit;
import soot.ValueBox;

public class MisuseAnalysis extends AbstractAnalysis{
	public MisuseAnalysis(Body body, VulnerabilityReporter reporter) {
		super(body, reporter);
	}
	
	@Override
	protected void flowThrough(Unit unit) {
		List<ValueBox> useBoxes = unit.getUseBoxes();
		
		for(int i = 0; i < useBoxes.size(); i++) {
			ValueBox box = useBoxes.get(i);
			if(box.getValue().toString().equals("staticinvoke <javax.crypto.Cipher: javax.crypto.Cipher getInstance(java.lang.String)>(\"AES\")")) {
				reporter.reportVulnerability(this.method.toString(), unit);
			}

		}

	}
}
