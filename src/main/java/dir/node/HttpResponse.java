package dir.node;


import java.io.Serializable;
import java.util.Collection;

public class HttpResponse implements Serializable {

    private byte [] data;
    private int status;
    private Collection<Header> headers;

    public HttpResponse(byte [] data, Collection<Header> headers, int status){
        this.data = data;
        this.headers = headers;
        this.status = status;
    }

    public byte[] getData() {
        return data;
    }

    public Collection<Header> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }


    public static class Header implements Serializable {
        private String name;
        private String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }


        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

}

