

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configure {
    private static Configure configure = null;
    private static Properties properties = null;
    private Configure(String configFilePath){
        try {
            FileInputStream f = new FileInputStream((configFilePath));
            properties = new Properties();
            properties.load(f);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    private Properties getProps(){
        return this.properties;
    }
    public static Properties getProperties(){
        if(configure==null){
            configure = new Configure("C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-2\\MapReduce\\src\\mapReduce.config");
        }
        return configure.getProps();
    }
}
