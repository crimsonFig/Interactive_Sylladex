package app.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class ClassicStyleLayoutGUIM extends BPaneLayoutGUIM {
    private final GuiPropertyMap guiPropertyMap;

    /**
     * Initializes GUI layout components and then displays the stage
     *
     * @param primaryStage
     *         The stage to initialize the main application components onto
     * @throws IOException
     *         if unable to read, access, or load FXML resources
     * @throws NullPointerException
     *         if expected values are missing after loading or attempted accessed before loading
     */
    public ClassicStyleLayoutGUIM(Stage primaryStage) throws IOException, NullPointerException {
        // set up the base scene and root
        BPaneRootControl rootController = initRootComponent();
        BorderPane       appRoot        = Objects.requireNonNull(rootController.getRoot(), "Root should be loaded.");
        Scene            scene          = new Scene(appRoot, 1025, 750);
        scene.getStylesheets().add(VIEW_RESOURCE.CSS_APPLICATION.getExternalPath());
        primaryStage.setScene(scene);

        // set up the components and attach to base scene
        // this implementation is based on fx:controller injection and getController.
        // ...other possible implementation schemes to consider is fx:root and instantiation+setController.
        // The design choice behind accessing static fields is to allow fx injection but still explicitly choose which class to instantiate
        MenuBarComponent    menuBarComponent = initSimpleComponent(appRoot, MenuBarControl.FXML_FILE, Cardinal.TOP);
        SidebarControl      sidebarComponent = initSimpleComponent(appRoot, SidebarControl.FXML_FILE, Cardinal.RIGHT);
        DisplayComponent    displayComponent = initSimpleComponent(appRoot, DisplayControl.FXML_FILE, Cardinal.CENTER);
        TextOutputComponent outputComponent  = initSimpleComponent(appRoot, OutputConsoleControl.FXML_FILE, Cardinal.CENTER);
        TextInputComponent  inputComponent   = initSimpleComponent(appRoot, InputConsoleControl.FXML_FILE, Cardinal.CENTER);

        this.guiPropertyMap = new GuiPropertyMap(menuBarComponent,
                                                 sidebarComponent,
                                                 sidebarComponent,
                                                 displayComponent,
                                                 inputComponent,
                                                 outputComponent);

        // application GUI is fully initialized now, so display the stage
        primaryStage.show();
    }

    @Nonnull
    @Override
    public GuiPropertyMap getGuiPropertyMap() {
        return guiPropertyMap;
    }
}
