package Events;

import misc.TableRow;
import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;

public class ReportMessage implements KompicsEvent {
    public String src;
    public String dst;
    public boolean isAccepted;
    public String name;
    public int level;
    public ArrayList<TableRow> routeTable;
    public int weight;

    public ReportMessage(String src, String dst, boolean isAccepted, int weight, String name, int level)  {
        this.src = src;
        this.dst = dst;
        this.isAccepted = isAccepted;
        this.weight = weight;

        this.name = name;
        this.level = level;
    }

    public ReportMessage(String src, String dst, boolean isAccepted, int weight, String name, int level, ArrayList<TableRow> routeTable)  {
        this.src = src;
        this.dst = dst;
        this.isAccepted = isAccepted;
        this.weight = weight;
        this.name = name;
        this.level = level;
        this.routeTable = routeTable;
    }
}
