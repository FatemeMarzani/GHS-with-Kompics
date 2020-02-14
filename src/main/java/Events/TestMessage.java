package Events;

import se.sics.kompics.KompicsEvent;

public class TestMessage implements KompicsEvent {
    public String src;
    public String dst;
    public String name;
    public int level;

    public TestMessage(String src, String dst, String name, int level) {
        this.src = src;
        this.dst = dst;
        this.name = name;
        this.level = level;

    }
}
