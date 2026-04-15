package dev.skypaolo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Minigame {
    
    private final StringProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty imagePath;
    
    public Minigame(String id, String title, String description, String imagePath) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.imagePath = new SimpleStringProperty(imagePath);
    }
    
    public String getId() {
        return id.get();
    }
    
    public void setId(String id) {
        this.id.set(id);
    }
    
    public StringProperty idProperty() {
        return id;
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getImagePath() {
        return imagePath.get();
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }
    
    public StringProperty imagePathProperty() {
        return imagePath;
    }
    
    @Override
    public String toString() {
        return "Minigame{" +
                "id='" + getId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
