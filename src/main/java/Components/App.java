package Components;


import Events.InitMessage;
import Ports.EdgePort;
import misc.Edge;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class App extends ComponentDefinition {

    String startNode = "";
    ArrayList<Edge> edges = new ArrayList<>();
    Map<String,Component> components = new HashMap<String,Component>();

    public App(){
        readTable();
    }

    public static void main(String[] args) throws InterruptedException{

        try {
            Kompics.createAndStart(App.class);
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            System.exit(1);
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        Kompics.shutdown();


    }

    public void readTable(){
        File resourceFile = new File("src/main/java/tables.txt");
        try (Scanner scanner = new Scanner(resourceFile)) {
            int i = 0;
            while (scanner.hasNext()){
                String line = scanner.nextLine();

                if (i > 0){
                    if (line.split(",").length > 1){
                        int weight = Integer.parseInt(line.split(",")[1]);
                        String rel = line.split(",")[0];
                        String src = rel.split("-")[0];
                        String dst = rel.split("-")[1];
                        edges.add(new Edge(src,dst,weight));
                    }
                    else{
                        startNode = line;
                        for(Edge edge:edges){
                            if (!components.containsKey(edge.src)){
                                 Component c = create(Node.class,new InitMessage(edge.src,true,findNeighbours(edge.src)));
                                 components.put(edge.src,c) ;
                            }
                            if (!components.containsKey(edge.dst)){
                                Component c = create(Node.class,new InitMessage(edge.dst,true,findNeighbours(edge.dst)));
                                components.put(edge.dst,c) ;
                            }
                            connect(components.get(edge.src).getPositive(EdgePort.class),
                                    components.get(edge.dst).getNegative(EdgePort.class), Channel.TWO_WAY);
                            connect(components.get(edge.src).getNegative(EdgePort.class),
                                    components.get(edge.dst).getPositive(EdgePort.class),Channel.TWO_WAY);
                        }
                    }
                }
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }




    }
    private HashMap<String,Edge> findNeighbours(String node){
        HashMap<String,Edge> nb = new HashMap<String,Edge>();
        for(Edge tr:edges){
            if(tr.src.equalsIgnoreCase(node) && !nb.containsKey(tr.dst)){
                nb.put(tr.dst , new Edge(tr.src, tr.dst, tr.weight));
            }
            else if (tr.dst.equalsIgnoreCase(node) && !nb.containsKey(tr.src)){
                nb.put(tr.src ,new Edge(tr.dst, tr.src, tr.weight));
            }
        }
        return nb;
    }
}
