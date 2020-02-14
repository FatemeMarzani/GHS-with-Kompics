package Ports;

import Events.*;
import se.sics.kompics.PortType;

public class EdgePort extends PortType {{
    positive(TestMessage.class);
    positive(InitiateMessage.class);
    positive(ReportMessage.class);
    positive(JoinMessage.class);
    positive(ChangeRootMessage.class);
    negative(TestMessage.class);
    negative(InitiateMessage.class);
    negative(ReportMessage.class);
    negative(JoinMessage.class);
    negative(ChangeRootMessage.class);
}}
