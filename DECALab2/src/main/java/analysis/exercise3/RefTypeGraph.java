package analysis.exercise3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.SootMethod;
import soot.Type;
import soot.jimple.FieldRef;

public class RefTypeGraph {

    private List<RefTypeNode> nodes;

    private List<RefTypeEdge> edges;

    private HashMap<SootMethod, List<RefTypeNode>> methodNodeMap;

    public RefTypeGraph() {
        super();
        this.methodNodeMap = new HashMap<SootMethod, List<RefTypeNode>>();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public List<RefTypeNode> getNodesForMethod(SootMethod m){
    	
    	return methodNodeMap.get(m);
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


	public void addNode(RefTypeNode node, SootMethod m) {
		nodes.add(node);
		System.out.println("here 12");
		if(this.methodNodeMap.containsKey(m)) {
			this.methodNodeMap.get(m).add(node);
		}
		else {
			ArrayList<RefTypeNode> list = new ArrayList<RefTypeNode>();
			list.add(node);
			this.methodNodeMap.put(m, list);
			System.out.println("added node to hash map, current map: " + this.methodNodeMap);
		}
		
	}

}
