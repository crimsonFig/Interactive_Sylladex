package app.ui;

import javafx.scene.layout.Pane;

import javax.annotation.CheckForNull;

interface DisplayComponent extends GUIComponent {
    @CheckForNull Pane getDisplay();
}
