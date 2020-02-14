package Events;

import Components.Node;
import misc.Edge;
import se.sics.kompics.Init;

import java.util.ArrayList;
import java.util.HashMap;

public class InitMessage extends Init<Node> {
    public String nodeName;
    public boolean isRoot = false;
    public HashMap<String,Edge> neighbours = new HashMap<>();

    public InitMessage(String nodeName, boolean isRoot,
                       HashMap<String, Edge> neighbours) {
        this.nodeName = nodeName;
        this.isRoot = isRoot;
        this.neighbours = neighbours;
    }
}