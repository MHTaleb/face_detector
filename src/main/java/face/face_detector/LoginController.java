/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face.face_detector;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author taleb
 */
public class LoginController implements Initializable {

    private final String ACCESS_USERNAME = "admin";
    private final String ACCESS_PASSWORD = "admin";
    
    @FXML
    private JFXTextField username;
    @FXML
    private JFXPasswordField password;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void reset(ActionEvent event) {
        username.setText("");
        password.setText("");
    }

    @FXML
    private void connect(ActionEvent event) throws IOException {
        
        if(username.getText().equals(ACCESS_USERNAME) && password.getText().equals(ACCESS_PASSWORD))
        {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));
              Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
        }
        
    }
    
}
