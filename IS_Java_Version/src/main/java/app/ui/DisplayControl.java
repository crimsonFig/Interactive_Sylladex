package app.ui;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class DisplayControl extends HBox implements DisplayComponent, LoadableFXML {
    static final  VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_SIMPLE_DISPLAY_STACKPANE;
    @FXML private StackPane     display;

    @FXML
    private void displayClick(MouseEvent event) {
    }

    @CheckForNull
    @Override
    public StackPane getDisplay() {
        return display;
    }

    @Nonnull
    @Override
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
