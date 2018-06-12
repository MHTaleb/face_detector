package face.face_detector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mysql.ImagesMysql;
import org.apache.commons.io.FileUtils;

public class MainApp extends Application {

    static {

        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage stage) throws Exception,IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");


        URL inputUrl = getClass().getResource("/haarcascades/haarcascade_frontalface_alt.xml");
        File dest = new File("/haarcascades/haarcascade_frontalface_alt.xml");
        FileUtils.copyURLToFile(inputUrl, dest);
        inputUrl = getClass().getResource("/lbpcascades/lbpcascade_frontalface.xml");
        dest = new File("/lbpcascades/lbpcascade_frontalface.xml");
        FileUtils.copyURLToFile(inputUrl, dest);

        String trainingDir = "c:/trainingDir/";
        final File file = new File(trainingDir);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                 FileUtils.deleteDirectory(file);
                System.out.println("Failed to create directory!");
            }
        }
        stage.setOnCloseRequest(e -> {
            FXMLController.stopAcquisition();
            try{
                 FileUtils.deleteDirectory(file);
            
            }catch(Exception ex ){
            ex.printStackTrace();
            }
                
                
        });
       
        ImagesMysql.read();

        stage.setTitle("JavaFX and Maven");
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
