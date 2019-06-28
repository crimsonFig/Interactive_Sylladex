package app.ui;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

import javax.annotation.Nonnull;

public class MenuBarControl implements MenuBarComponent, LoadableFXML {
    static final VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_SIMPLE_MENUBAR;

    @FXML private MenuItem mAbout;

    @Nonnull
    @Override
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
