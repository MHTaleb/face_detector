/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face.face_detector;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;

/**
 *
 * @author taleb
 */
public class ImagesDTO {

    
    
    private final LongProperty id = new SimpleLongProperty();
    private final ObjectProperty<ImageView> image = new SimpleObjectProperty<>();

    public ImageView getImage() {
        return image.get();
    }

    public void setImage(ImageView value) {
        image.set(value);
    }

    public ObjectProperty<ImageView> imageProperty() {
        return image;
    }
    
    

    public long getId() {
        return id.get();
    }

    public void setId(long value) {
        id.set(value);
    }

    public LongProperty idProperty() {
        return id;
    }
    
}
