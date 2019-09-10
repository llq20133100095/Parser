import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by llq on 2018/1/17.
 * 1.Dependencies:det(system-2, The-1) nsubj(has-6, system-2) mark(described-4, as-3)....
 * 2.Change this dependencies to adjacencyMatrix1.
 * 3.Use the Dijkstra to get the SDP
 */
public class DepDijkstra {

    public static void main(String[] args) throws IOException{
        FileReader dependencies=new FileReader("./data/test_dependencies.txt");
//        FileReader taggedWord=new FileReader("./data/train_tagged.txt");
        BufferedReader bfDepen=new BufferedReader(dependencies);
//        BufferedReader bfTag=new BufferedReader(taggedWord);

        //Read entityFile
        FileReader entityFile=new FileReader("./data/test_entity_position.txt");
        BufferedReader bfEntityFile=new BufferedReader(entityFile);
        String lineEntity=null;

        //save the shortest path of <e1>entity</e1>.
        FileWriter e1SDP=new FileWriter("./data/test_e1_SDP.txt");
        //save the shortest path of <e2>entity</e2>.
        FileWriter e2SDP=new FileWriter("./data/test_e2_SDP.txt");
        //save the shortest path of position id in e1
        FileWriter e1WordPos=new FileWriter("./data/test_e1_sdp_pos.txt");
        //save the shortest path of position id in e2
        FileWriter e2WordPos=new FileWriter("./data/test_e2_sdp_pos.txt");

        String lineDepen=null;
        int sen=0;
        while((lineDepen=bfDepen.readLine())!=null && (lineEntity=bfEntityFile.readLine())!=null){
            //the number of word
            int wordNum=0;
            //Store the no repetition word
            Map<Integer,String> mapDepen=new HashMap<Integer,String>();
            //Start node
            int startNode=0;
            sen++;
            String[] strDepen=lineDepen.split("\t");
            for(String x:strDepen){
                //get (foreWord,backWord)
                String foreWord=x.split(", ")[0].split("\\(")[1];
                String backWord=x.split(", ")[1].split("\\)")[0];

                //get word number:split of "-"
                int foreWordIndex=foreWord.lastIndexOf("-");
                int backWordIndex=backWord.lastIndexOf("-");
                int foreWordNumber=Integer.parseInt(foreWord.substring(foreWordIndex+1));
                int backWordNumber=Integer.parseInt(backWord.substring(backWordIndex+1));

                //Max wordNum
                if(wordNum<foreWordNumber){
                    wordNum=foreWordNumber;
                }
                if(wordNum<backWordNumber){
                    wordNum=backWordNumber;
                }

                //put (foreWordNumber,foreWord) and (backWordNumber,backWord) into map
                mapDepen.put(foreWordNumber, foreWord.substring(0, foreWordIndex));
                mapDepen.put(backWordNumber,backWord.substring(0,backWordIndex));

                //get the start node number
                if(foreWord.equals("ROOT-0")) {
                    int indexCrossBar = backWord.lastIndexOf("-");
                    startNode = Integer.parseInt(backWord.substring(indexCrossBar + 1));
                }

            }

            //adjacencyMatrix
            int[][] adjacencyMatrix=SetAdjacencyMatrix(wordNum+1,strDepen);

            //Use Dijkstra:get the shortest path
            String[] shortestPath=Dijkstra.dijkstraUse(adjacencyMatrix, startNode);

            //Save the left and right SDP
            WriteSDP(lineEntity,mapDepen,shortestPath,e1SDP,e2SDP,e1WordPos,e2WordPos,sen);

//            sen++;
//            if(sen==51){
//                break;
//            }
        }

        bfDepen.close();
        bfEntityFile.close();
        e1SDP.close();
        e2SDP.close();
        e1WordPos.close();
        e2WordPos.close();
    }

    /**
     * Set the Adjacency Matrix
     * @param wordNum
     * @param strDepen
     * @return
     */
    public static int[][] SetAdjacencyMatrix(int wordNum,String[] strDepen){
        //adjacencyMatrix
        int[][] adjacencyMatrix=new int[wordNum][wordNum];
        int M=2000; //(代表此路不通)

        //init the adjacencyMatrix
        for(int x=0;x<adjacencyMatrix.length;x++){
            for(int y=0;y<adjacencyMatrix[x].length;y++){
                if(x==y){
                    adjacencyMatrix[x][y]=0;
                }else{
                    adjacencyMatrix[x][y]=M;
                }
            }
        }

        //set adjacencyMatrix[foreWordNum][backWordNum]=1
        for(String x:strDepen){
            //get (foreWord,backWord)
            String foreWord=x.split(", ")[0].split("\\(")[1];
            String backWord=x.split(", ")[1].split("\\)")[0];

            //foreWord number and backWore number
            if(foreWord.equals("ROOT-0")==false){
                int foreWordNum=Integer.parseInt(foreWord.substring(foreWord.lastIndexOf("-")+1));
                int backWordNum=Integer.parseInt(backWord.substring(backWord.lastIndexOf("-")+1));
                adjacencyMatrix[foreWordNum][backWordNum]=1;
            }
        }

        return adjacencyMatrix;
    }

    public static void WriteSDP(String lineEntity,Map<Integer,String> mapDepen,String[] shortestPath,FileWriter e1SDP,FileWriter e2SDP,FileWriter e1WordPos,FileWriter e2WordPos,int sen){
        /**
         * get two entity number
         */
        //Check the entity number
        String[] e1Pos=lineEntity.split("\\<e\\>")[0].split(" ");
        int e1=Integer.parseInt(e1Pos[e1Pos.length-1]);
        String[] e2Pos=lineEntity.split("\\<e\\>")[1].split(" ");
        int e2=Integer.parseInt(e2Pos[e2Pos.length-1]);


        /**
         * Get two SDP.
         * Save two SDP(e1,e2).
         */
        String[] e1SDPnum=shortestPath[e1].split("-->");
        String[] e2SDPnum={"1"};
        try{
            e2SDPnum=shortestPath[e2].split("-->");
        }catch (Exception e){
            System.out.println(sen);
            e.printStackTrace();
        }


        try{
            for(String x:e1SDPnum){
                e1SDP.write(mapDepen.get(Integer.parseInt(x)) + " ");
                e1WordPos.write(x+" ");
            }
            e1SDP.write("\n");
            e1WordPos.write("\r\n");

            for(String x:e2SDPnum){
                e2SDP.write(mapDepen.get(Integer.parseInt(x)) + " ");
                e2WordPos.write(x+" ");
            }
            e2SDP.write("\n");
            e2WordPos.write("\r\n");

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
