package service;

import client.Client;
import client.ClientRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import node.HttpRequest;
import node.HttpResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HTTPService {

    private final Logger logger = LogManager.getLogger();

    public void startServer() throws Throwable {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
        server.createContext("/", new CustomHttpHandler());
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        server.setExecutor(threadPoolExecutor);
        server.start();
        logger.info(" HTTP Server started on port 8000");
    }

    private class CustomHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            byte [] htmlResponse = null;
            Collection<HttpResponse.Header> headers = null;
      //      Header [] headers = null;
            int status = 200;
            if (!"GET".equals(httpExchange.getRequestMethod())) {
                httpExchange.sendResponseHeaders(400, 0);
            } else {
                try {
                    Client client = new Client();
                    logger.info("Attempting to get " + httpExchange.getRequestURI().toString());
                    List<HttpResponse.Header> headersList = new ArrayList<>();
                   httpExchange.getRequestHeaders().entrySet().stream().forEach(entry ->
                        headersList.add(new HttpResponse.Header(entry.getKey(),entry.getValue().iterator().next())));

                    HttpResponse httpResponse = client.sendAnonymousRequest(new HttpRequest(httpExchange.getRequestURI(),headersList));
                    htmlResponse = httpResponse.getData();
                    headers = httpResponse.getHeaders();

//                    URIBuilder builder = new URIBuilder(httpExchange.getRequestURI());
//                    // String requestParamValue = handleGetRequest(httpExchange);
//                    CloseableHttpClient httpClient = HttpClients.createDefault();
//                    HttpGet httpGet = new HttpGet(builder.build());
//                    CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
//                    htmlResponse = EntityUtils.toByteArray(httpResponse.getEntity());
//                     headers = httpResponse.getAllHeaders();
//                    status = httpResponse.getStatusLine().getStatusCode();
//                    httpResponse.close();
//                    httpClient.close();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }


//                StringBuilder htmlBuilder = new StringBuilder();
//                htmlBuilder.append("<html>")
//                        .append("<body>")
//                        .append("<h1>")
//                        .append("Hello ")
//                        .append("Proxy Works")
//                        .append("</h1>")
//                        .append("</body>")
//                        .append("</html>");
               // htmlResponse = htmlBuilder.toString();
               for(HttpResponse.Header header: headers){
                   if( !header.getName().contains("Transfer"))
                   httpExchange.getResponseHeaders().set(header.getName(), header.getValue());
               }

               // httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(status, htmlResponse.length);
            }
            if (htmlResponse != null) {
                outputStream.write(htmlResponse);
            }
            outputStream.flush();
            outputStream.close();
        }


    }
}
