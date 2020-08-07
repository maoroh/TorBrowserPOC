//package ui;
//
//import javafx.scene.Node;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Priority;
//import javafx.scene.layout.Region;
//import javafx.scene.web.WebEngine;
//import javafx.scene.web.WebView;
//
//class Browser extends Region {
//
//    final WebView browser = new WebView();
//    final WebEngine webEngine = browser.getEngine();
//
//    public Browser() {
//        //apply the styles
//        BorderPane border = new BorderPane();
//        getStyleClass().add("browser");
//        // load the web page
//        webEngine.loadContent("<html><body><h1>Maor</h1></body></html>");
//        //add the web view to the scene
//        getChildren().add(createSpacer());
//        getChildren().add(border);
//
//
//    }
//    private Node createSpacer() {
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        return spacer;
//    }
//
//    @Override protected void layoutChildren() {
//        double w = getWidth();
//        double h = getHeight();
//        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
//    }
//
//    @Override protected double computePrefWidth(double height) {
//        return 750;
//    }
//
//    @Override protected double computePrefHeight(double width) {
//        return 500;
//    }