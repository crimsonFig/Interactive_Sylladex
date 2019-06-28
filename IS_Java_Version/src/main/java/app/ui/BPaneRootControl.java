package app.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class BPaneRootControl implements RootComponent, LoadableFXML {
    static final VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_SIMPLE_ROOT_BORDERPANE;

    @FXML private BorderPane root;

    @CheckForNull
    @Override
    public BorderPane getRoot() {
        return root;
    }

    @Nonnull
    @Override
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
