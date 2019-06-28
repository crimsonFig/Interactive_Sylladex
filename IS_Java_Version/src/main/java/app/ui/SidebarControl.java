package app.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SidebarControl implements ModusSelectComponent, CmdInfoComponent, LoadableFXML {
    static final VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_CLASSIC_SIDEBAR;

    ///tab - Commands
    @FXML private Accordion cmdAcc;
    @FXML private VBox      syllCmdList;
    @FXML private Tab       cmdTab; // todo: set tab to be shown on content update
    @FXML private VBox      modusCmdList;
    @FXML private VBox      miscList;
    ///tab - Modus List
    @FXML private VBox      modusMenuList;
    ///tab - Help
    @FXML private Button    bReset;

    @FXML
    private void initialize() {
        bReset.setDisable(true);
        //set the view to be on the commands tab from the modusMenuList tab
        // todo - can move this to a controller...on tab content property change, perform below as a lambda
        // getCmdTab().getTabPane().getSelectionModel().select(getCmdTab());
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
    public ObservableList<Node> getModusMenuListChildren() throws IllegalStateException {
        if (modusMenuList == null) throw new IllegalStateException("Gui element hierarchy must be loaded/initialized.");
        return modusMenuList.getChildren();
    }

    @Nonnull
    @Override
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
