package analysis.exercise3;

import java.util.ArrayList;
import java.util.List;

public class RefTypeGraph {

	private List<RefTypeNode> nodes;
	
	private List<RefTypeEdge> edges;
	
	

	public RefTypeGraph() {
		super();
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();
	}
	
	

	public List<RefTypeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RefTypeNode> nodes) {
		this.nodes = nodes;
	}

	public List<RefTypeEdge> getEdges() {
		return edges;
	}

	public void setEdges(List<RefTypeEdge> edges) {
		this.edges = edges;
	}
	
	
}
