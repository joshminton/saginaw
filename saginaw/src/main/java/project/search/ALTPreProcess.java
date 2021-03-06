package project.search;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import project.map.Graph;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ALTPreProcess {

    Int2ObjectOpenHashMap distancesTo;
    Int2ObjectOpenHashMap distancesFrom;

    final int NUM_LANDMARKS = 10;
    final double RADIUS = 10;

    ArrayList<Integer> landmarks;

    Graph graph;

    public ALTPreProcess(Graph graph, boolean core) throws IOException {
        String filePrefix = graph.getFilePrefix();
        distancesTo = new Int2ObjectOpenHashMap<double[]>(); //need to compute
        distancesFrom = new Int2ObjectOpenHashMap<double[]>();
        landmarks = new ArrayList<>();
        this.graph = graph;

        GenerateLandmarks(core);
        DijkstraLandmarks dj;

        File dfDir;

        if(core){
            dfDir = new File(filePrefix.concat("coreDistancesFrom.ser"));
        }else{
            dfDir = new File(filePrefix.concat("distancesFrom.ser"));
        }
        if(dfDir.exists()){
            System.out.print("Loading distances from... ");
            FileInputStream fileIn = new FileInputStream(dfDir);
            FSTObjectInput objectIn = new FSTObjectInput(fileIn);
            try {
                distancesFrom = (Int2ObjectOpenHashMap<double[]>) objectIn.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            fileIn.close();
            objectIn.close();
            System.out.println("Done.");
        } else {
            System.out.print("Creating distances from... ");
            dj = new DijkstraLandmarks(graph, landmarks, true, core);
            distancesFrom = dj.getDistTo();
            FileOutputStream fileOut = new FileOutputStream(dfDir);
            FSTObjectOutput objectOut = new FSTObjectOutput(fileOut);
            objectOut.writeObject(distancesFrom);
            objectOut.close();
            dj.clear();
            System.out.println("Done.");
        }

        File dtDir;
        if(core){
            dtDir = new File(filePrefix.concat("coreDistancesTo.ser"));
        }else{
            dtDir = new File(filePrefix.concat("distancesTo.ser"));
        }
        if(dtDir.exists()){
            System.out.print("Loading distances to... ");
            FileInputStream fileIn = new FileInputStream(dtDir);
            FSTObjectInput objectIn = new FSTObjectInput(fileIn);
            try {
                distancesTo = (Int2ObjectOpenHashMap<double[]>) objectIn.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            fileIn.close();
            objectIn.close();
            System.out.println("Done.");

        } else {
            System.out.print("Creating distances to... ");
            dj = new DijkstraLandmarks(graph, landmarks, false, core);                             // <-- need reverse graph here
            distancesTo = dj.getDistTo();
            FileOutputStream fileOut = new FileOutputStream(dtDir);
            FSTObjectOutput objectOut = new FSTObjectOutput(fileOut);
            objectOut.writeObject(distancesTo);
            objectOut.close();
            dj.clear();
            System.out.print("Done.");
        }
    }

    public void GenerateLandmarks(boolean core){
        int size;
        Map<Integer, ArrayList<double[]>> fwd;
        Map<Integer, ArrayList<double[]>> bck;

        List<Integer> fwdNodes;
        List<Integer> bckNodes;


//        if(core){
            fwd = graph.getFwdCore();
            bck = graph.getBckCore();

            fwdNodes = new ArrayList<>(fwd.keySet());
            bckNodes = new ArrayList<>(bck.keySet());

//        }else{
//
//            fwd = graph.getFwdCore();
//            bck = graph.getBckCore();
//
//            fwdNodes = new ArrayList<>();
//            bckNodes = new ArrayList<>();
//
//
//            for(int i = 0; i < fwd.size(); i++){
//                fwdNodes.add(i);
//            }
//
//            for(int i = 0; i < bck.size(); i++){
//                bckNodes.add(i);
//            }
//        }

        Random random = new Random(20);

        size = fwdNodes.size();






        if(graph.getRegion().equals("britain")) {
            landmarks.add(3967407);
            landmarks.add(59083);
            landmarks.add(2642529);
            landmarks.add(3861974);
            landmarks.add(3226881);
            landmarks.add(1635202);
            landmarks.add(3950134);
            landmarks.add(74648);
            landmarks.add(670600);
            landmarks.add(3612575);
        } else {

//        } else if(graph.getRegion().equals("walese")){
//            landmarks.add(Long.parseLong("260093216"));
//            landmarks.add(Long.parseLong("1886093447"));
//            landmarks.add(Long.parseLong("4254105731"));
//            landmarks.add(Long.parseLong("1491252547"));
//            landmarks.add(Long.parseLong("296030988"));
//            landmarks.add(Long.parseLong("1351220556"));
//            landmarks.add(Long.parseLong("262840382"));
//            landmarks.add(Long.parseLong("344881575"));
//            landmarks.add(Long.parseLong("1795462073"));
//        } else if(graph.getRegion().equals("francee")){
//            landmarks.add(Long.parseLong("1997249188"));
//            landmarks.add(Long.parseLong("420592228"));
//            landmarks.add(Long.parseLong("1203772336"));
//            landmarks.add(Long.parseLong("292093917"));
//            landmarks.add(Long.parseLong("629419387"));
//            landmarks.add(Long.parseLong("1161458782"));
//            landmarks.add(Long.parseLong("702241324"));
//            landmarks.add(Long.parseLong("31898581"));
//            landmarks.add(Long.parseLong("600118738"));
//            landmarks.add(Long.parseLong("268366322"));
//        } else {

            for (int x = 0; x < 10; x++) {
                boolean exitFlag = false;
                while (!exitFlag) {
                    Integer node = fwdNodes.get(random.nextInt(size));
                    if (bckNodes.contains(node)) {
                        landmarks.add(node);
                        exitFlag = true;
                    }
                }
            }
        }

//        landmarks = generateCircularLandmarks();

    }

    private ArrayList<Integer> generateCircularLandmarks(){
        Point2D.Double centre = new Point2D.Double(-1.657300, 53.381509);

        Double x, y;
        ArrayList<Integer> landmarks = new ArrayList<>();

        for(int z = 0; z < NUM_LANDMARKS; z++){
            x = -RADIUS * Math.sin(360/NUM_LANDMARKS * z);
            y = RADIUS * Math.cos(360/NUM_LANDMARKS * z);
            double[] point = new double[]{centre.getX() + x, centre.getY() + y};
            System.out.println(point[1] + ", " + point[0]);
            landmarks.add(graph.findClosest(point));
            System.out.println(graph.getGraphNodeLocation(landmarks.get(landmarks.size() - 1))[1] + ", " + graph.getGraphNodeLocation(landmarks.get(landmarks.size() - 1))[0]);
        }

        return landmarks;
    }

    public ArrayList<Integer> getLandmarks() {
        return landmarks;
    }
}