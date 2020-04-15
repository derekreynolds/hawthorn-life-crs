package com.hawthornlife.crs;


import javafx.scene.image.*;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/favicon.ico")));
    	stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/favicon-16x16.png")));
    	stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/favicon-32x32.png")));
    	stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/favicon-96x96.png")));
    	stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/hawthorn-life-logo.png")));
        
        stage.resizableProperty().setValue(Boolean.FALSE);
        
        stage.setTitle("Hawthorn Life CRS");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
