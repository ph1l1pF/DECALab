package analysis.exercise3;

import soot.SootClass;
import soot.Type;
import soot.jimple.FieldRef;

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

    public void addTypeToNode(FieldRef ref, Type type) {
        for (RefTypeNode node : nodes) {
            if (node.getRef().getField().getSignature().equals(ref.getField().getSignature())) {
                node.addType(type);
            }
        }
    }

    public void finaliseTypePropagationGraph() {
        boolean newChanges = true;
        while (newChanges) {
            newChanges = false;
            for (RefTypeEdge edge : edges) {
                for (Type type : edge.getFirst().getTypes()) {
                    if (!edge.getSecond().containsType(type)) {
                        newChanges = true;
                        edge.getSecond().addType(type);
                    }
                }
            }
        }
    }
    
    public boolean containsNode(FieldRef ref) {
		for(RefTypeNode node : nodes) {
			if(node.getRef().getField().getSignature().equals(ref.getField().getSignature())) {
				return true;
			}
		}
		return false;
	}



	public RefTypeNode getNodeByRef(FieldRef ref) {
		for(RefTypeNode node : nodes) {
			if(node.getRef().getField().getSignature().equals(ref.getField().getSignature())) {
				return node;
			}
		}
		throw new IllegalArgumentException("ref not contained in graph");
	}
	
	public void addEdgeByRefs(FieldRef ref1, FieldRef ref2) {
		RefTypeNode n1 = getNodeByRef(ref1);
		RefTypeNode n2 = getNodeByRef(ref2);
		
		//TODO: check whether edge is already contained
		edges.add(new RefTypeEdge(n1, n2));
	}


	public void addNode(RefTypeNode node) {
		nodes.add(node);
		
	}

}
