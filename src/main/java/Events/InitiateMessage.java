package Events;

import se.sics.kompics.KompicsEvent;

public class InitiateMessage implements KompicsEvent {
    public String src;
    public String dst;
    public String fragmentName;
    public int level;

    public InitiateMessage(String src, String dst, String fragmentName,int level) {
        this.src = src;
        this.dst = dst;
        this.fragmentName = fragmentName;
        this.level = level;
    }
}
