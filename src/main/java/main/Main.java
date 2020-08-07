package main;

import client.Client;
import dir.Directory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import node.HttpRequest;
import service.HTTPService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class Main  extends Application {


    public static void main(String[] args) throws Throwable {

        HTTPService httpService = new HTTPService();
        httpService.startServer();
        Directory dirServer = new Directory();
        dirServer.startListening();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("mainView.fxml"));
        stage.setTitle("HTMLViewer");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}

