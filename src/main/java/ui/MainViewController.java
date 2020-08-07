package ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    @FXML
    private WebView webView;

    @FXML
    private TextField urlField;

    private WebEngine engine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8000");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "8000");

        URL.setURLStreamHandlerFactory((protocol) -> {
                if (protocol.contains("https")) {
                    return new sun.net.www.protocol.http.Handler();
                } else return getURLStreamHandler(protocol);
        });

        engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        webView.setZoom(0.75);
        engine.load("http://www.google.com");
    }


    public static URLStreamHandler getURLStreamHandler(String protocol) {
        try {
            Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
            method.setAccessible(true);
            return (URLStreamHandler) method.invoke(null, protocol);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    public void loadUrl(ActionEvent actionEvent){
        Platform.runLater(()->{
            String url = urlField.getText();
            engine.load(url);
        });

    }
}