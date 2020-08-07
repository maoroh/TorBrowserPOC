package node;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;

public class HttpRequest implements Serializable {

    private URI uri;
    private Collection<HttpResponse.Header> headers;

    public HttpRequest(URI uri, Collection<HttpResponse.Header> headers) {
        this.uri = uri;
        this.headers = headers;
    }

    public URI getUrl() {
        return uri;
    }

    public Collection<HttpResponse.Header> getHeaders() {
        return headers;
    }
}
