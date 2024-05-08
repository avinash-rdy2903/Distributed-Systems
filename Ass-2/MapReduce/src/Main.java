import API.Master;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
public class Main{
    public static void main(String[] args) throws InterruptedException, IOException{
        long id = Master.createCluster(3,2);
        Master.runCluster(id,"C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-2\\MapReduce\\input.txt","C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-2\\MapReduce\\Word\\Mapper.java","C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-2\\MapReduce\\InvertedIndex\\Reducer.java", "C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-2\\MapReduce\\output.txt");
    }
}