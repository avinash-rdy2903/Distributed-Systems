import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LookUpCache<K,V> extends HashMap<K,V> {

    private File f = null;
    private static final ObjectMapper parser = new ObjectMapper();
    private boolean loadJson(String file) throws IOException{

        this.f = new File(file);
        if(!f.exists()){
            return false;
        }
        for(Map.Entry<K,V> entry:parser.readValue(f,new TypeReference<Map<K,V>>(){}).entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
        return true;
    }
    public LookUpCache(String file) throws IOException {
        super();
        if(!loadJson(file)){
            throw new IOException("Could not load from given directory: "+file);
        }
    }
    public boolean flushToJSON() throws IOException{

        parser.writeValue(f,this);
        return true;
    }
}
