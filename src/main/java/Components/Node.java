package Components;
import Events.*;
import Ports.EdgePort;
import misc.Edge;
import misc.EdgeType;
import misc.TableRow;
import se.sics.kompics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Node extends ComponentDefinition {
    private Negative<EdgePort> sendPort = negative(EdgePort.class);
    private Positive<EdgePort> receivePort = positive(EdgePort.class);
    private Boolean isRoot;
    private String nodeName;
    private String parentName;
    private String fragmentName;
    private int level = 0;

    private int dist = Integer.MAX_VALUE;
    private String distName;
    private int distLevel;

    private int waitingResponseCount = 0;

    private HashMap<String, Edge> neighbours;
    private String joinCandidate;
    private ArrayList<JoinMessage> receivedJoinMessage = new ArrayList<>();

    private ArrayList<TableRow> routeTable = new ArrayList<>();
    public Node(InitMessage initMessage) {
        nodeName = initMessage.nodeName;
        fragmentName = initMessage.nodeName;
        this.neighbours = initMessage.neighbours;
        this.isRoot = initMessage.isRoot;
        subscribe(startHandler, control);
        subscribe(initiateMessageHandler,receivePort);
        subscribe(testMessageHandler,receivePort);
        subscribe(reportMessageHandler,receivePort);
        subscribe(joinMessageHandler,receivePort);
        subscribe(changeRootMessageHandler,receivePort);
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                    if(entry.getValue().type == EdgeType.Basic) {
                        waitingResponseCount ++;
                        trigger(new TestMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
                    }
                }
        }
    };

    private Handler<InitiateMessage> initiateMessageHandler = new Handler<InitiateMessage>() {
        @Override
        public void handle(InitiateMessage event) {
            if(nodeName.equalsIgnoreCase(event.dst)) {
                waitingResponseCount = 0;
                parentName = event.src;
                isRoot = false;

                fragmentName = event.fragmentName;
                level = event.level;
                System.out.println(nodeName + ':' + event.src +" => fragment name changed to: " + event.fragmentName);
                for (JoinMessage e: receivedJoinMessage) {
                    ReplyJoinMessage(e);
                }
                receivedJoinMessage.clear();

                for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                    if(entry.getValue().type == EdgeType.Branch && !entry.getValue().dst.equalsIgnoreCase(event.src)) {
                        waitingResponseCount ++;
                        trigger(new InitiateMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
                    }
                    if(entry.getValue().type == EdgeType.Basic) {
                        waitingResponseCount ++;
                        trigger(new TestMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
                    }
                }
                HandleChildReportsDone();
            }
        }
    };

    private Handler<TestMessage> testMessageHandler = new Handler<TestMessage>() {
        @Override
        public void handle(TestMessage event) {
            if(nodeName.equalsIgnoreCase(event.dst)) {
                AtomicInteger weight = new AtomicInteger(neighbours.get(event.src).weight);

                if(event.name.equalsIgnoreCase(fragmentName)) {
                    trigger(new ReportMessage(nodeName, event.src, false, weight.get(), fragmentName, level), sendPort);
                } else if (!event.name.equalsIgnoreCase(fragmentName) && event.level <= level) {
                    trigger(new ReportMessage(nodeName, event.src, true, weight.get(), fragmentName, level), sendPort);
                } else {

                    new Thread(() -> {
                        while (level < event.level) {
                        }
                        weight.set(neighbours.get(event.src).weight);
                        if(event.name.equalsIgnoreCase(fragmentName)) {
                            trigger(new ReportMessage(nodeName, event.src, false, weight.get(), fragmentName, level), sendPort);
                        } else if (!event.name.equalsIgnoreCase(fragmentName)) {
                            trigger(new ReportMessage(nodeName, event.src, true, weight.get(), fragmentName, level), sendPort);
                        }
                    }).start();
                }
            }
        }
    };

    private Handler<ReportMessage> reportMessageHandler = new Handler<ReportMessage>() {
        @Override
        public void handle(ReportMessage event) {
            if(nodeName.equalsIgnoreCase(event.dst)) {
                waitingResponseCount --;
                if(!event.isAccepted) {
                    for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                        if(entry.getValue().dst.equalsIgnoreCase(event.src)){
                            if(entry.getValue().type == EdgeType.Basic) {
                                entry.getValue().type = EdgeType.Rejected;
                            }
                        }
                    }
                } else {
                    if(dist > event.weight) {
                        dist = event.weight;
                        distName = event.name;
                        distLevel = event.level;
                    }
                }
                HandleChildReportsDone();
            }
        }
    };

    private Handler<ChangeRootMessage> changeRootMessageHandler = new Handler<ChangeRootMessage>() {
        @Override
        public void handle(ChangeRootMessage event) {
            if(event.dst.equalsIgnoreCase(nodeName)) {
                for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                    if(entry.getValue().type == EdgeType.Branch && !entry.getValue().dst.equalsIgnoreCase(event.src)) {
                        trigger(new ChangeRootMessage(nodeName,entry.getValue().dst, event.dist),sendPort);
                    } else if (entry.getValue().dst.equalsIgnoreCase(event.src) && entry.getValue().type != EdgeType.Branch) {
                        entry.getValue().type = EdgeType.Branch;
                    }
                    else if (entry.getValue().type == EdgeType.Basic && entry.getValue().weight == event.dist) {
                        HandleReceivedJoinMessages(entry.getValue());
                    }
                }
            }
        }
    };

    private void HandleReceivedJoinMessages(Edge edge) {
        for (JoinMessage message: receivedJoinMessage) {
            if(message.src.equalsIgnoreCase(edge.dst)) {//if j send join msg to i too
                if(message.level == level) {
                    edge.type = EdgeType.Branch;
                    if (nodeName.compareTo(message.src) > 0) {
                        level = level + 1;
                        isRoot = true;
                        fragmentName = String.valueOf(edge.weight);
                        System.out.println(nodeName + '*' + message.src +" fragment name changed to: " +edge.weight);System.out.println(nodeName + '*' + message.src +" fragment name changed to: " +edge.weight);System.out.println(nodeName + '*' + message.src +" fragment name changed to: " +edge.weight);
                        Init();
                    }

                }


            }
        }


        joinCandidate = edge.dst;
        trigger(new JoinMessage(nodeName, edge.dst, fragmentName, level), sendPort);
    }

    private Handler<JoinMessage> joinMessageHandler = new Handler<JoinMessage>() {
        @Override
        public void handle(JoinMessage event) {
            if(nodeName.equalsIgnoreCase(event.dst)) {
                if(event.src.equalsIgnoreCase(joinCandidate)) {//if its my candidate
                        for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                            if(event.src.equalsIgnoreCase(entry.getValue().dst)) {
                                entry.getValue().type = EdgeType.Branch;
                                if(event.level == level) {
                                    if (nodeName.compareTo(event.src) > 0) {
                                        System.out.println(nodeName + ':' + event.src +" fragment name changed to: " + event.fragmentName);
                                        level = level + 1;
                                        fragmentName = String.valueOf(entry.getValue().weight);
                                        isRoot = true;
                                        Init();
                                    }
                                }  else if(level > event.level) {
                                    fragmentName = String.valueOf(entry.getValue().weight);
                                    isRoot = true;
                                    Init();
                                } else if (event.level > level) {//absorb
                                    System.out.println(nodeName + ':' + event.src +" fragment name changed to: " + event.fragmentName);
                                    fragmentName = event.fragmentName;
                                    level = event.level;
                                    isRoot = false;
                                }
                            }
                        }
                        joinCandidate = null;
                } else if (level > event.level) ReplyJoinMessage(event);
                else {
                    receivedJoinMessage.add(event);
                }
            }
        }
    };

    private void HandleChildReportsDone() {
        if(waitingResponseCount == 0) {
            if(isRoot) {
                if(dist < Integer.MAX_VALUE) {
                    isRoot = false;
                    for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
                        if(entry.getValue().type == EdgeType.Branch) {
                            trigger(new ChangeRootMessage(nodeName,entry.getValue().dst, dist),sendPort);
                        } else if (entry.getValue().type == EdgeType.Basic && entry.getValue().weight == dist) {
                            HandleReceivedJoinMessages(entry.getValue());
                        }
                    }
                }
            } else {

                if(dist < Integer.MAX_VALUE) {
                    trigger(new ReportMessage(nodeName, parentName, true, dist, distName, distLevel, routeTable), sendPort);
                } else {
                    trigger(new ReportMessage(nodeName, parentName, false, dist, distName, distLevel, routeTable), sendPort);
                }
            }
            dist = Integer.MAX_VALUE;
        }
    }

    private void ReplyJoinMessage(JoinMessage event) {
        for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
            if(entry.getValue().type == EdgeType.Basic && event.src.equalsIgnoreCase(entry.getValue().dst)) {
                entry.getValue().type = EdgeType.Branch;
                trigger(new JoinMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
            }
        }
    }

    private void Init() {
        for (Map.Entry<String, Edge> entry: neighbours.entrySet()) {
            if(entry.getValue().type == EdgeType.Branch) {
                waitingResponseCount ++;
                trigger(new InitiateMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
            }
            if(entry.getValue().type == EdgeType.Basic) {
                waitingResponseCount ++;
                trigger(new TestMessage(nodeName, entry.getValue().dst, fragmentName, level), sendPort);
            }
        }
    }
}

