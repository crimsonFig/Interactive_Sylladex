package app.ui;

import app.modus.Modus;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class SidebarControl implements ModusSelectComponent, CmdInfoComponent, LoadableFXML {
    static final VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_CLASSIC_SIDEBAR;

    ///tab - Commands
    @FXML private Accordion cmdAcc;
    @FXML private VBox      syllCmdList;
    @FXML private Tab       cmdTab;
    @FXML private VBox      modusCmdList;
    @FXML private VBox      miscList;
    ///tab - Modus List
    @FXML private VBox      modusMenuList;
    ///tab - Help
    @FXML private Button    bReset;

    private boolean currentActiveModusChanged = false;

    /**
     * the observable list allows an abstract and independent way of binding model objects to any UI presentation.
     */
    private ObservableList<Class<? extends Modus>> modusMenuSelectableClassList = FXCollections.observableArrayList();

    /**
     * this field property is meant to encapsulate an event handler that will be set during runtime (strategy design). It's purpose will
     * enable domain specific handling that is independent of the UI handling, allowing UI to be swapped and injected.
     */
    private ObjectProperty<EventHandler<ActionEvent>> modusSelectionHandler = new SimpleObjectProperty<>(this,
                                                                                                         "modus_selection_handler",
                                                                                                         (event) -> {});

    @FXML
    private void initialize() {
        bReset.setDisable(true);

        // this code adds a listener that essentially creates a selectable box for each modus class added, and vice versa.
        // this code relies on the #modusSelectionHandler being set by an external class (see core.Sylladex) before the listener is invoked
        // this was done in order to place bulky UI code in UI controllers instead of domain logic controllers
        modusMenuSelectableClassList.addListener((ListChangeListener<Class<? extends Modus>>) (listChange) -> {
            while (listChange.next()) {
                // For each added class, create a pane with a button that references a modus class.
                // The button should invoke the injected modus selection handler, disable the fired button, and select the command tab
                for (Class<? extends Modus> modusClazz : listChange.getAddedSubList()) {
                    GridPane pane = new GridPane();
                    pane.getColumnConstraints()
                        .add(new ColumnConstraints(10, 100, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
                    pane.getRowConstraints().add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
                    pane.getRowConstraints().add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
                    pane.setPrefWidth(257.0);
                    pane.setPrefHeight(40.0);
                    pane.setPadding(new Insets(0, 5, 5, 5));

                    Label name = new Label(modusClazz.getSimpleName());
                    name.setFont(new Font("Courier", 13));
                    pane.add(name, 0, 0);

                    Button button = new Button("Select");
                    button.setId(ModusSelectComponent.BUTTON_ID_PREFIX + modusClazz.getCanonicalName());
                    button.setFont(new Font("Courier", 11));
                    GridPane.setHalignment(button, HPos.RIGHT);
                    pane.add(button, 0, 1);

                    // apply handlers, with a weak reference to disjoint this object from owning externally referred objects.
                    button.addEventHandler(ActionEvent.ACTION, new WeakEventHandler<>(this.modusSelectionHandlerProperty().getValue()));
                    button.addEventHandler(ActionEvent.ACTION, new WeakEventHandler<>(this::selectCommandTabHandler));
                    button.addEventHandler(ActionEvent.ACTION, new WeakEventHandler<>(this::disableActiveModusButtonHandler));

                    //create and add a Separator between this and next pane.
                    Separator hLine = new Separator();
                    hLine.setPrefWidth(200);

                    this.modusMenuList.getChildren().addAll(new VBox(pane, hLine));
                }
                // For each removed class, target the button based on the ID given to it, and remove the previously added container
                for (Class<? extends Modus> modusClazz : listChange.getRemoved()) {
                    this.modusMenuList.getChildren()
                                      .remove(modusMenuList.lookup("#" +
                                                                   ModusSelectComponent.BUTTON_ID_PREFIX +
                                                                   modusClazz.getCanonicalName()).getParent().getParent());
                }
            }
        });

        //applies a listener that sets the active modus changed flag to true if the modus command list had an addition.
        modusCmdList.getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) if (change.wasAdded()) currentActiveModusChanged = true;
        });
    }

    /**
     * Handler for the modus selection button. This handler will shift the current selected tab to the commands tab (only if the active
     * modus changed flag is true).
     *
     * @param event
     *         the event fired by the button. This is expected to be ignored.
     */
    void selectCommandTabHandler(ActionEvent event) {
        if (this.currentActiveModusChanged) this.cmdTab.getTabPane().getSelectionModel().select(this.cmdTab);
    }

    /**
     * Handler for the modus selection button. If the active modus changed flag is true, then this handler will disable to button that was
     * selected and will then reset the current active modus changed flag (to false).
     *
     * @param event
     *         the event fired by the button
     */
    void disableActiveModusButtonHandler(ActionEvent event) {
        if (this.currentActiveModusChanged) {
            this.modusMenuList.getChildren()
                              .stream()
                              .filter(VBox.class::isInstance)
                              .map(childNode -> (GridPane) ((VBox) childNode).getChildren().get(0))
                              .forEach(childNode -> childNode.getChildren()
                                                             .stream()
                                                             .filter(Button.class::isInstance)
                                                             .findFirst()
                                                             .orElse(new Button())
                                                             .setDisable(false));
            ((Button) event.getSource()).setDisable(true);
            this.currentActiveModusChanged = false;
        }
    }

    @FXML
    void reset() {
        //TODO: set currentmodus to -1 and reset reinitialize the sylladex
    }

    @FXML
    void displayClick(MouseEvent event) {
        //TODO: consider displaying info on click?
    }

    @Nonnull
    @Override
    public ObservableList<Node> getSyllCmdListChildren() throws IllegalStateException {
        if (syllCmdList == null) throw new IllegalStateException("Gui element hierarchy must be loaded/initialized.");
        return syllCmdList.getChildren();
    }

    @Nonnull
    @Override
    public ObservableList<Node> getModusCmdListChildren() throws IllegalStateException {
        if (modusCmdList == null) throw new IllegalStateException("Gui element hierarchy must be loaded/initialized.");
        return modusCmdList.getChildren();
    }

    @Nonnull
    @Override
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }

    @Nonnull
    @Override
    public ObservableList<Class<? extends Modus>> getModusMenuSelectableClassList() {
        return modusMenuSelectableClassList;
    }

    @Override
    public void setAllModusMenuSelectableClassList(Collection<Class<? extends Modus>> selectableClassList) {
        modusMenuSelectableClassList.setAll(Objects.requireNonNull(selectableClassList));
    }

    @Nonnull
    @Override
    public ObjectProperty<EventHandler<ActionEvent>> modusSelectionHandlerProperty() {
        return modusSelectionHandler;
    }
}
