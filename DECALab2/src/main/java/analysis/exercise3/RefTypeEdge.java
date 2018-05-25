package analysis.exercise3;

public class RefTypeEdge {
	
	private RefTypeNode first, second;
	
	

	public RefTypeEdge(RefTypeNode first, RefTypeNode second) {
		super();
		this.first = first;
		this.second = second;
	}

	public RefTypeNode getFirst() {
		return first;
	}

	public void setFirst(RefTypeNode first) {
		this.first = first;
	}

	public RefTypeNode getSecond() {
		return second;
	}

	public void setSecond(RefTypeNode second) {
		this.second = second;
	}
	
	

}
