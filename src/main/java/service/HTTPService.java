package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HTTPService {

    private final Logger logger = LogManager.getLogger();

    public void startServer() throws Throwable {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
        server.createContext("/service", new CustomHttpHandler());
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        server.setExecutor(threadPoolExecutor);
        server.start();
        logger.info(" HTTP Server started on port 8000");
    }

    private class CustomHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            String htmlResponse = null;
            if(!"GET".equals(httpExchange.getRequestMethod()) || !validArgs(httpExchange)) {
                httpExchange.sendResponseHeaders(400, 0);
            } else {
                String requestParamValue = handleGetRequest(httpExchange);
                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append("<html>")
                        .append("<body>")
                        .append("<h1>")
                        .append("Hello ")
                        .append(requestParamValue)
                        .append("</h1>")
                        .append("</body>")
                        .append("</html>");
                htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
                httpExchange.sendResponseHeaders(200, htmlResponse.length());
            }
            if(htmlResponse != null){
                outputStream.write(htmlResponse.getBytes());
            }
            outputStream.flush();
            outputStream.close();
        }


        private String handleGetRequest(HttpExchange httpExchange) {
            return httpExchange.getRequestURI()
                    .toString()
                    .split("\\?")[1]
                    .split("=")[1];
        }
    }

    private boolean validArgs(HttpExchange httpExchange) {
        return httpExchange.getRequestURI()
                .toString()
                .split("\\?").length > 1;
    }
}
