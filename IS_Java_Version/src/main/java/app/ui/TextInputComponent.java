package app.ui;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextInputControl;

import java.util.function.Consumer;

interface TextInputComponent extends GUIComponent {
    Consumer<ChangeListener<String>> getSubmittedInputSubscriber();

    TextInputControl getTextInput();
}
