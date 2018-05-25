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
        while (newChanges = true) {
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

}
