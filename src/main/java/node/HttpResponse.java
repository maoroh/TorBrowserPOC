package node;


import java.io.Serializable;
import java.util.Collection;

public class HttpResponse implements Serializable {

    private byte [] data;
    private Collection<Header> headers;

    public HttpResponse(byte [] data, Collection<Header> headers){
        this.data = data;
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public Collection<Header> getHeaders() {
        return headers;
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

