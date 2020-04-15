package com.hawthornlife.crs;

import java.io.File;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FXMLController implements Initializable {
    
    private static Logger log = LoggerFactory.getLogger(FXMLController.class);
    
    @FXML
    private Stage stage;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        
        log.info("Entering");
        
        FileChooser fileChooser = new FileChooser();
        
        fileChooser.getExtensionFilters().addAll(
           new FileChooser.ExtensionFilter("Excel Files", "*.xlsm")
        );        
        
        File selectedFile = fileChooser.showOpenDialog(stage);  
        
        if(selectedFile == null)
            return;
        
        String file = selectedFile.getAbsolutePath();
       
        log.info("Transforming " + selectedFile.getAbsolutePath());
        
        try {
            XmlGenerator xmlGenerator = new XmlGenerator(selectedFile.getAbsolutePath());
            xmlGenerator.generate();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Hawthorn Life CRS");
            alert.setHeaderText("CRS Generated Successfully");
            alert.setContentText("Hawthorn Life CRS generated -> " + file.substring(0, file.lastIndexOf(".")) + ".xml");

            alert.showAndWait();
            
        } catch(Exception e) {
            log.error("Error", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Hawthorn Life CRS");
            alert.setHeaderText("CRS XML Generation Error");
            alert.setContentText("An error occured while generating the CRS file. Please consult log files.");

            alert.showAndWait();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }    
}
