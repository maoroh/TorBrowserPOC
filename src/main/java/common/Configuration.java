package common;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import node.NodeInfo;

import java.io.InputStreamReader;
import java.util.List;


public class Configuration {

    private static Configuration _instance;

    @SerializedName("nodes")
    private List<NodeInfo> _nodes;
    @SerializedName("directoryServer")
    private ServerConfig _directoryServerConfig;


    private static Gson _gson;
    private static JsonReader _reader;


    public List<NodeInfo> getNodes() {
        return _nodes;
    }

    public ServerConfig getDirectoryServerConfig() {
        return _directoryServerConfig;
    }


    public class ServerConfig {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }


    public static Configuration getConfig(){
        if(_instance == null){
            _gson = new Gson();
            _reader = new JsonReader(new InputStreamReader( Configuration.class.getClassLoader().getResourceAsStream("configuration.json")));
           _instance =  _gson.fromJson(_reader, new TypeToken<Configuration>(){}.getType());
        }
        return _instance;
    }
}
