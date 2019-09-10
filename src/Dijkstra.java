public class Dijkstra {

    public static void main(String[] args){
        int M=2000; //(代表此路不通)
        int[][] adjacencyMatrix={//邻接矩阵
                {0,3,M,M,30},
                {M,0,25,8,M},
                {M,M,0,M,10},
                {20,M,4,0,12},
                {5,M,M,M,0}
        };

        int[][] weight2 = {
                {0,10,M,30,100},
                {M,0,50,M,M},
                {M,M,0,M,10},
                {M,M,20,0,60},
                {M,M,M,M,0}
        };
        //start label
        int start=0;
        dijkstraUse(weight2,start);
    }

    /**
     * Come true the use of Dijkstra
     * @param adjacencyMatrix
     * @param start
     */
    public static String[] dijkstraUse(int[][] adjacencyMatrix,int start){
        //顶点个数
        int n=adjacencyMatrix.length;
        //保存以求得最短路径的终点序号,1表示已求出
        int[] visitedS=new int[n];
        //存放从开始顶点到其他各点的最短路径
        int[] dist=new int[n];
        //存放从start到其他各点的最短路径的字符串表示
        String[] path=new String[n];
        for(int i=0;i<n;i++){
            path[i]=new String(start+"-->"+i);
        }

        //init,标记开始顶点
        visitedS[start]=1;
//        dist[start]=0;

        for(int i=0;i<n;i++){
            dist[i]=adjacencyMatrix[start][i];
//            path[i]=new String(start+" -> "+i);
        }


        //要加入n-1个顶点
        for(int count = 1;count <= n - 1;count++){

            int k = -1;    //选出一个距离初始顶点start最近的未标记顶点
            int dmin = Integer.MAX_VALUE;
            //1.求出dist最短路径
            for(int i=0;i<n;i++){
                if(visitedS[i]==0 && dist[i]<dmin){
                    //get the min value
                    dmin=dist[i];
                    //store the label
                    k=i;
                }
            }

            //label the next node
            visitedS[k]=1;
            dist[k]=dmin;

            //2.以K为中间点，计算下一个步骤的路径至。
            for(int i=0;i<n;i++){
                if(visitedS[i]==0 && dist[k]+adjacencyMatrix[k][i]<dist[i]){
                    dist[i]=dist[k]+adjacencyMatrix[k][i];
                    path[i]=path[k]+"-->"+i;
                }
            }

        }

//        for(int i=0;i<n;i++){
//            System.out.println("从"+start+"出发到"+i+"的最短路径为："+path[i]);
//        }
        return path;
    }
}
