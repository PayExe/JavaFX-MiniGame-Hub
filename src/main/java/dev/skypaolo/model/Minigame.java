package dev.skypaolo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Vault-Tec Model: Minigame Data Structure
 * Represents a mini game entry in the hub with title, description, and image.
 * S.P.E.C.I.A.L. Stats: Intelligence (data structure), Charisma (presentation)
 */
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
    
    // ID Property
    public String getId() {
        return id.get();
    }
    
    public void setId(String id) {
        this.id.set(id);
    }
    
    public StringProperty idProperty() {
        return id;
    }
    
    // Title Property
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    // Description Property
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    // Image Path Property
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
