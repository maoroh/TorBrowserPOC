package client.ui;

import client.Client;
import client.connection.UnionRoutingURLConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import dir.node.HttpRequest;
import dir.node.HttpResponse;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    private final String HTTP_PROXY_HOST = "localhost";

    private final String HTTP_PROXY_PORT = "8000";

    @FXML
    private WebView webView;

    @FXML
    private TextField urlField;

    private WebEngine engine;

    private Client client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        client = new Client();
        try {
            client.replaceKeysWithNodes();
            URL.setURLStreamHandlerFactory((protocol) -> {
                if (protocol.contains("https") || protocol.contains("http")) {
                    return new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL u) {
                            return new UnionRoutingURLConnection(u, client);
                        }
                    };
                }
                else {
                    return null;
                }
            });

            engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);

            webView.setZoom(0.75);
            //Home Page
            engine.load("https://www.google.com");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }


    @FXML
    public void loadUrl(ActionEvent actionEvent){
        Platform.runLater(()->{
            String url = urlField.getText();
            if(!url.contains("http://") && !url.contains("https://")){
                url = "http://" + url;
            }
            engine.load(url);
        });

    }
}