package app.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import javax.annotation.Nonnull;

interface CmdInfoComponent extends GUIComponent {
    @Nonnull ObservableList<Node> getSyllCmdListChildren() throws IllegalStateException;

    @Nonnull ObservableList<Node> getModusCmdListChildren() throws IllegalStateException;
}
