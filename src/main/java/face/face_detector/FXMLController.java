package face.face_detector;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javax.imageio.ImageIO;
import mysql.ImagesMysql;
import mysql.User;
import mysql.UsersMysql;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.opencv.core.CvType.CV_32SC1;

import tools.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class FXMLController implements Initializable {

    @FXML
    private ImageView currentFrame;

    @FXML
    private HBox videoContainer;
    @FXML
    private JFXCheckBox haarClassifier;

    @FXML
    private JFXCheckBox lbpClassifier;

    private CascadeClassifier faceCascade;
    private int absoluteFaceSize;

    // a timer for acquiring the video stream
    private static ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private static VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;

    @FXML
    private JFXButton launchButton;

    @FXML
    private JFXTextField username;
    @FXML
    private JFXTextField nom;
    @FXML
    private JFXTextField prenom;
    @FXML
    private JFXDatePicker date;
    @FXML
    private JFXTextField telephone;
    @FXML
    private Label detected;
    @FXML
    private TableView<ImagesDTO> tableDataBase;
    @FXML
    private TableColumn<ImagesDTO, Long> colId;
    @FXML
    private TableColumn<ImagesDTO, ImageView> colImage;

    @FXML
    void launchCam(ActionEvent event) {

        if (!this.cameraActive) {
            videoContainer.alignmentProperty().set(Pos.TOP_LEFT);
            currentFrame.fitHeightProperty().set(768);
            currentFrame.fitWidthProperty().set(1024);
            // start the video capture
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);

                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.launchButton.setText("ARRETER CAPTURE");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.launchButton.setText("LANCER CAPTURE");
            videoContainer.alignmentProperty().set(Pos.CENTER);
            currentFrame.fitHeightProperty().set(100);
            currentFrame.fitWidthProperty().set(100);

            // stop the timer
            this.stopAcquisition();
        }
    }

    int count = 0;

    private void saveToFile(Image image) {
        count++;
        File outputFile = new File(trainingDir + username.getText() + "-" + count + ".png");

        try {
            outputFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImagesMysql.insert(trainingDir + username.getText() + "-" + count + ".png", trainingDir + username.getText() + "-" + count + ".png");
        UsersMysql.insert(nom.getText(), prenom.getText(), telephone.getText(), date.getValue().toString(), Integer.parseInt(username.getText()));
    }

    int detectCount = 5;

    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {

                    // face detection
                    detectAndDisplay(frame);
                    try {
                        detectCount++;
                        if (detectCount % 20 == 0) {
                            detectUser();
                            detectCount = 1;
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        System.out.println("detection exception");
                    }
                }

            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    private void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }

    }

    /**
     * The action triggered by selecting the Haar Classifier checkbox. It loads
     * the trained set to be used for frontal face detection.
     */
    @FXML
    protected void haarSelected(Event event) {
        // check whether the lpb checkbox is selected and deselect it
        if (this.lbpClassifier.isSelected()) {
            this.lbpClassifier.setSelected(false);
        }

        this.checkboxSelection("/haarcascades/haarcascade_frontalface_alt.xml");
    }

    /**
     * The action triggered by selecting the LBP Classifier checkbox. It loads
     * the trained set to be used for frontal face detection.
     */
    @FXML
    protected void lbpSelected(Event event) {
        // check whether the haar checkbox is selected and deselect it
        if (this.haarClassifier.isSelected()) {
            this.haarClassifier.setSelected(false);
        }

        this.checkboxSelection("/lbpcascades/lbpcascade_frontalface.xml");
    }

    private void checkboxSelection(String classifierPath) {
        System.out.println("path : " + classifierPath);
        // load the classifier(s)
        faceCascade.load(classifierPath);

        // now the video capture can start
        launchButton.setDisable(false);
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    public static void stopAcquisition() {
        if (timer != null && !timer.isShutdown()) {
            try {
                // stop the timer
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (capture.isOpened()) {
            // release the camera
            capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }

    String trainingDir = "c:/trainingDir/";

    @FXML
    public void saveUser(Event event) {
        Image imagev = currentFrame.getImage();

        File file = new File(trainingDir);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        saveToFile(imagev);

    }

    public synchronized void detectUser() throws NumberFormatException {

        Image imagev = currentFrame.getImage();

        File file = new File(trainingDir);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        File temp = new File("c:/temp");
        if (!temp.exists()) {
            if (temp.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        File outputFile = new File("c:/temp/0-current.png");
        try {
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedImage bImage = SwingFXUtils.fromFXImage(imagev, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        opencv_core.Mat testImage = imread("c:/temp/0-current.png", CV_LOAD_IMAGE_GRAYSCALE);

        File root = new File(trainingDir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

        MatVector images = new MatVector(imageFiles.length);

        opencv_core.Mat labels = new opencv_core.Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            opencv_core.Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            int label = Integer.parseInt(image.getName().split("\\-")[0]);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        // FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        // FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

        faceRecognizer.train(images, labels);

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        faceRecognizer.predict(testImage, label, confidence);
        System.out.println(label.limit());
        System.out.println("confidence = " + confidence.get());
        int predictedLabel = label.get(0);

        if (confidence.get() > 1000) {
            predictedLabel = -1;
        }

        System.out.println("Predicted label: " + predictedLabel);

        Task<Void> task = new afficheResultatPrediction(predictedLabel, confidence.get());
        task.run();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setStyle("\"-fx-alignment: CENTER-RIGHT;\"");
    }

    @FXML
    public void charger() throws FileNotFoundException {
        List<ImagesDTO> images = new ArrayList<>();

        File[] files = new File(trainingDir).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                final ImagesDTO imagesDTO = new ImagesDTO();
                long id = Long.parseLong(file.getName().split("-")[0]);
                imagesDTO.setId(id);
                final ImageView image = new ImageView(new Image(new FileInputStream(file)));
                double imageWidth = image.getImage().getWidth();
                image.fitWidthProperty().bind(
                        Bindings.when(colImage.widthProperty().lessThan(imageWidth + 40))
                                .then(colImage.widthProperty().subtract(40))
                                .otherwise(imageWidth));
                image.fitHeightProperty().bind(
                        Bindings.when(colImage.widthProperty().lessThan(imageWidth + 40))
                                .then(colImage.widthProperty().subtract(40).divide(3).multiply(4))
                                .otherwise(imageWidth / 3 * 4));
                imagesDTO.setImage(image);
                images.add(imagesDTO);
            }
        }

        tableDataBase.getItems().addAll(images);
    }

    private class afficheResultatPrediction extends Task<Void> {

        private final int predictedLabel;
        private final double confidence;

        public afficheResultatPrediction(int predictedLabel, double confidence) {
            this.predictedLabel = predictedLabel;
            this.confidence = confidence;
        }

        @Override
        protected Void call() throws Exception {

            Platform.runLater(new RunnableImpl(predictedLabel, confidence));

            return null;
        }

        class RunnableImpl implements Runnable {

            private final int predictedLabel;
            private final double confidence;

            public RunnableImpl(int predictedLabel, double confidence) {
                this.predictedLabel = predictedLabel;
                this.confidence = confidence;
            }

            @Override
            public void run() {
                User user = UsersMysql.read(predictedLabel, detected);
                String displayMessage = user.getNom() + " " + user.getPrenom();
//                if (confidence < 300) {
//                    displayMessage += " taux exactitude 90%";
//                } else if (confidence < 500) {
//                    displayMessage += " taux exactitude 80%";
//                } else if (confidence < 800) {
//                    displayMessage += " taux exactitude 70%";
//                } else if (confidence < 1100) {
//                    displayMessage += " taux exactitude 60%";
//                } else if (confidence < 1500) {
//                    displayMessage += " taux exactitude 50%";
//                }
                detected.setText(displayMessage);
            }
        }
    }
}
