package dir.node.service;

import actions.RoutingFromServiceAction;
import actions.ServiceRequestAction;
import dir.node.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpServiceClient implements ServiceClient {

    private final Logger logger = LogManager.getLogger();

    public RoutingFromServiceAction createRequest(ServiceRequestAction.ServerRequest clientRequest, byte[] key, UUID sessionId) throws Throwable {
        try {
            logger.info("Exit Node getting data from " + clientRequest.getRequest().getUrl() + "...");
            CloseableHttpClient httpClient = HttpClients.createDefault();

            URIBuilder builder = new URIBuilder(clientRequest.getRequest().getUrl());

            HttpGet httpGet = new HttpGet(builder.build());

            final RequestConfig params = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
            httpGet.setConfig(params);

            clientRequest.getRequest().getHeaders()
                    .forEach(h -> httpGet.setHeader(h.getName(), h.getValue()));

            CloseableHttpResponse httpRealResponse = httpClient.execute(httpGet);

            byte[] data = Optional.ofNullable(httpRealResponse.getEntity())
                    .map(entity -> {
                        try {
                            return EntityUtils.toByteArray(entity);
                        } catch (IOException e) {
                            return new byte[]{};
                        }
                    }).orElse(new byte[]{});

            HttpResponse httpResponse = new HttpResponse(data, Arrays.stream(httpRealResponse.getAllHeaders()).map(header ->
                    new HttpResponse.Header(header.getName(), header.getValue()))
                    .collect(Collectors.toList()), httpRealResponse.getStatusLine().getStatusCode());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            objectOutputStream.writeObject(httpResponse);
            httpRealResponse.close();
            httpClient.close();
            return RoutingFromServiceAction.of(AESUtils
                    .AESEncrypt(bos.toByteArray(),
                            key), sessionId);
        } catch (Throwable e) {
            return RoutingFromServiceAction.of(AESUtils
                    .AESEncrypt(SerializeUtils
                                    .serializeObject(new HttpResponse(new byte[]{}, null, 400)),
                            key), sessionId);
        }
    }
}
