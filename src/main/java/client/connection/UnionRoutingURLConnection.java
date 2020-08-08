package client.connection;

import client.Client;
import dir.node.HttpRequest;
import dir.node.HttpResponse;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class UnionRoutingURLConnection extends URLConnection {

    private final Client _client;
    private byte[] data;
    private final String CONTENT_TYPE = "text/html; charset=utf-8";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                                     " AppleWebKit/537.36 (KHTML, like Gecko)" +
                                    " Chrome/74.0.3729.169 Safari/537.36";

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    public UnionRoutingURLConnection(URL url, Client client) {
        super(url);
        _client = client;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public int getContentLength() {
        return data.length;
    }

    @Override
    public long getContentLengthLong() {
        return data.length;
    }

    @Override
    public String getHeaderField(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return getContentType();
        } else if ("Content-Length".equalsIgnoreCase(name)) {
            return "" + getContentLength();
        }
        return null;
    }

    @Override
    public InputStream getInputStream()  {
        connect();
        return new ByteArrayInputStream(data);
    }

    @Override
    public void connect() {
        try {
            ArrayList<HttpResponse.Header> headers = new ArrayList<>();
            headers.add(new HttpResponse.Header("User-agent", USER_AGENT));
            headers.add(new HttpResponse.Header("Content-Type", CONTENT_TYPE));
            HttpResponse httpResponse = _client.sendAnonymousRequest(new HttpRequest(url.toURI(), headers));
            data = httpResponse.getData();
            connected = true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }
}
