package app.ui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

@ParametersAreNonnullByDefault
abstract class BPaneLayoutGUIM implements GUIManager {
    @Nonnull
    BPaneRootControl initRootComponent() throws IOException {
        FXMLLoader loader = new FXMLLoader(BPaneRootControl.FXML_FILE.toURL());
        BorderPane root   = loader.load();

        VBox centerContainerComponent = new VBox();
        centerContainerComponent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(centerContainerComponent, Pos.CENTER);
        root.setCenter(centerContainerComponent);

        return Objects.requireNonNull(loader.getController(), "Controller was not injected during root load.");
    }

    @Nonnull
    <T extends GUIComponent & LoadableFXML> T initSimpleComponent(BorderPane appRoot,
                                                                  VIEW_RESOURCE viewResource,
                                                                  BPaneLayoutGUIM.Cardinal position) throws
                                                                                                          IOException,
                                                                                                          IllegalArgumentException {
        FXMLLoader loader        = new FXMLLoader(viewResource.toURL());
        Parent     componentRoot = loader.load();
        attachComponentToRoot(appRoot, componentRoot, position);
        return Objects.requireNonNull(loader.getController(), "Controller was not injected from FXML load");
    }

    private void attachComponentToRoot(BorderPane appRoot,
                                       Parent componentRoot,
                                       BPaneLayoutGUIM.Cardinal position) throws IllegalArgumentException {
        switch (position) {
            case TOP:
                appRoot.setTop(componentRoot);
                break;
            case BOTTOM:
                appRoot.setBottom(componentRoot);
                break;
            case LEFT:
                appRoot.setLeft(componentRoot);
                break;
            case RIGHT:
                appRoot.setRight(componentRoot);
                break;
            case CENTER:
                ((Pane) appRoot.getCenter()).getChildren().add(componentRoot);
                break;
            default:
                throw new IllegalArgumentException("Unexpected cardinal parameter supplied: " + position.toString());
        }
    }

    enum Cardinal {TOP, BOTTOM, LEFT, RIGHT, CENTER}
}
