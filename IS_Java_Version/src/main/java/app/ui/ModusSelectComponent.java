package app.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import javax.annotation.Nonnull;

interface ModusSelectComponent extends GUIComponent {
    @Nonnull ObservableList<Node> getModusMenuListChildren();
}
