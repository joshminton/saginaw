package project.search;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import project.map.MyGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ALTPreProcess {

    Long2ObjectOpenHashMap distancesTo;
    Long2ObjectOpenHashMap distancesFrom;

    ArrayList<Long> landmarks;

    MyGraph graph;

    public ALTPreProcess(MyGraph graph, String region) throws IOException {
        String filePrefix = graph.getFilePrefix();
        distancesTo = new Long2ObjectOpenHashMap<double[]>(); //need to compute
        distancesFrom = new Long2ObjectOpenHashMap<double[]>();
        landmarks = new ArrayList<Long>();
        this.graph = graph;

        GenerateLandmarks();
        DijkstraLandmarks dj;

        File dfDir = new File(filePrefix.concat("distancesFrom.ser"));
        if(dfDir.exists()){
            System.out.println("Found distancesFrom.");
            FileInputStream fileIn = new FileInputStream(dfDir);
            FSTObjectInput objectIn = new FSTObjectInput(fileIn);
            try {
                distancesFrom = (Long2ObjectOpenHashMap<double[]>) objectIn.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            fileIn.close();
            objectIn.close();
        } else {
            dj = new DijkstraLandmarks(graph, landmarks, true);
            distancesFrom = dj.getDistTo();
            FileOutputStream fileOut = new FileOutputStream(dfDir);
            FSTObjectOutput objectOut = new FSTObjectOutput(fileOut);
            objectOut.writeObject(distancesFrom);
            objectOut.close();
            dj.clear();
            distancesFrom = null;
        }
        System.out.println("Done first bit");

        File dtDir = new File(filePrefix.concat("distancesTo.ser"));
        if(dtDir.exists()){
            System.out.println("Found distancesTo.");
            FileInputStream fileIn = new FileInputStream(dtDir);
            FSTObjectInput objectIn = new FSTObjectInput(fileIn);
            try {
                distancesTo = (Long2ObjectOpenHashMap<double[]>) objectIn.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            fileIn.close();
            objectIn.close();
        } else {
            dj = new DijkstraLandmarks(graph, landmarks, false);                             // <-- need reverse graph here
            distancesTo = dj.getDistTo();
            FileOutputStream fileOut = new FileOutputStream(dtDir);
            FSTObjectOutput objectOut = new FSTObjectOutput(fileOut);
            objectOut.writeObject(distancesTo);
            objectOut.close();
            dj.clear();
            distancesTo = null;
        }
    }

    public void GenerateLandmarks(){
        Map<Long, ArrayList<double[]>> fwdGraph = graph.getFwdGraph();
        Map<Long, ArrayList<double[]>> bckGraph = graph.getBckGraph();
        int size = fwdGraph.size();
        Random random = new Random();
        List<Long> fwdNodes = new ArrayList<>(fwdGraph.keySet());
        List<Long> bckNodes = new ArrayList<>(bckGraph.keySet());

        if(graph.getRegion().equals("england")){
            landmarks.add(Long.parseLong("27103812"));
            landmarks.add(Long.parseLong("424430268"));
            landmarks.add(Long.parseLong("262840382"));
            landmarks.add(Long.parseLong("25276649"));
        } else if(graph.getRegion().equals("wales")){
            landmarks.add(Long.parseLong("260093216"));
            landmarks.add(Long.parseLong("1886093447"));
            landmarks.add(Long.parseLong("4254105731"));
            landmarks.add(Long.parseLong("1491252547"));
            landmarks.add(Long.parseLong("296030988"));
            landmarks.add(Long.parseLong("1351220556"));
//            landmarks.add(Long.parseLong("262840382"));
//            landmarks.add(Long.parseLong("344881575"));
//            landmarks.add(Long.parseLong("1795462073"));
        } else if(graph.getRegion().equals("france")){
            landmarks.add(Long.parseLong("1997249188"));
            landmarks.add(Long.parseLong("420592228"));
            landmarks.add(Long.parseLong("1203772336"));
            landmarks.add(Long.parseLong("292093917"));
            landmarks.add(Long.parseLong("629419387"));
            landmarks.add(Long.parseLong("1161458782"));
            landmarks.add(Long.parseLong("702241324"));
            landmarks.add(Long.parseLong("31898581"));
            landmarks.add(Long.parseLong("600118738"));
            landmarks.add(Long.parseLong("268366322"));
        } else {
            for(int x = 0; x < 10; x++){
                boolean exitFlag = false;
                while(!exitFlag){
                    long node = fwdNodes.get(random.nextInt(size));
                    if(bckNodes.contains(node)){
                        landmarks.add(node);
                        exitFlag = true;
                    }
                }
                System.out.println(landmarks.get(x));
            }
        }
    }

}