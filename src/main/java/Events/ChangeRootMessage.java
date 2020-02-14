package Events;

import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;

public class ChangeRootMessage implements KompicsEvent {
    public String src;
    public String dst;
    public int dist;
    public ChangeRootMessage(String src, String dst, int dist) {
        this.src = src;
        this.dst = dst;
        this.dist = dist;
    }
}
