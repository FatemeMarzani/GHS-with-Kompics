package Events;

import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;

public class JoinMessage implements KompicsEvent {
    public String src;
    public String dst;
    public String fragmentName;
    public int level;

    public JoinMessage(String src, String dst, String fragmentName, int level) {
        this.src = src;
        this.dst = dst;
        this.fragmentName = fragmentName;
        this.level = level;
    }
}
