package node;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class NodeInfo implements Serializable {

    @SerializedName("host")
    private String _host;
    @SerializedName("port")
    private int _port;
    private UUID _sessionId;

    public NodeInfo(){

    }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return _port == nodeInfo._port &&
                Objects.equals(_host, nodeInfo._host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_host, _port);
    }

    public UUID getSessionId() {
        return _sessionId;
    }

    public void setSessionId(UUID sessionId) {
        _sessionId = sessionId;
    }
}
