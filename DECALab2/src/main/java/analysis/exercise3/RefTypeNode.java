package analysis.exercise3;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;
import soot.Type;
import soot.jimple.FieldRef;

public class RefTypeNode {

	private List<Type> types;
	private FieldRef ref;

	
	public RefTypeNode(FieldRef ref) {
		super();
		this.types = new ArrayList<>();
		this.ref = ref;
	}

	public List<Type> getTypes() {
		return types;
	}

	public void addType(Type type) {
		if (!containsType(type)) {
			this.types.add(type);
		}
	}
	public FieldRef getRef() {
		return ref;
	}
	public void setRef(FieldRef ref) {
		this.ref = ref;
	}


	public boolean containsType(Type type) {
		return types.contains(type);
	}
}
