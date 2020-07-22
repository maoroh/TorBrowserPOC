package router;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NodeInfo implements Serializable {

    @SerializedName("host")
    private String _host;
    @SerializedName("port")
    private int _port;

    public NodeInfo(String host, int port){
        _host = host;
        _port = port;
    }

    public static NodeInfo of(String host, int port){
        return new NodeInfo(host,port);
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        _host = host;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }
}
