package exercises;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;
import soot.jimple.FieldRef;

public class RefTypeNode {
	
	private List<SootClass> types;
	private FieldRef ref;

	
	public RefTypeNode(FieldRef ref) {
		super();
		this.types = new ArrayList<>();
		this.ref = ref;
	}
	public List<SootClass> getTypes() {
		return types;
	}
	public void addType(SootClass type) {
		this.types.add(type);
	}
	public FieldRef getRef() {
		return ref;
	}
	public void setRef(FieldRef ref) {
		this.ref = ref;
	}
	
	

}
