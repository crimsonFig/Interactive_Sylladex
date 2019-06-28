package app.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class InputConsoleControl implements TextInputComponent, LoadableFXML {
    static final         VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_CLASSIC_CONSOLE_INPUT;
    private static final Logger        LOGGER    = LogManager.getLogger(InputConsoleControl.class);

    private final StringProperty submittedInput;
    @FXML private Button         textInputSubmitButton;
    @FXML private TextField      textInput;

    public InputConsoleControl() {
        this.submittedInput = new SimpleStringProperty(this, "submitted_input", "");
    }

    @FXML
    private void initialize() {
        // both input and submit button should start as disabled.
        textInputSubmitButton.setDisable(true); // this should be enabled when the text input is valid
        textInput.setDisable(true);             // this should be enabled externally when the app is ready for commands

        textInput.textProperty().addListener((bean, oldVal, newVal) -> textInputSubmitButton.setDisable(newVal.trim().isEmpty()));
        textInput.sceneProperty().addListener((bean, oldScene, newScene) -> {
            if (newScene != null) {
                Parent root = newScene.getRoot();
                if (root.getOnKeyPressed() == null) {
                    //if the enter key is pressed, fire the submit button, else focus on the field.
                    root.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            textInputSubmitButton.fire();
                            keyEvent.consume();
                        } else {
                            textInput.requestFocus();
                        }
                    });
                } else LOGGER.warn("root node `" + root.toString() + "` already has an OnKeyPressed handler set.");
            }
        });
    }

    /**
     * @implNote by using a property with change listeners, this method gets around repeated commands by alternating between
     * trimmed and trim+appending a space character. All property subscribers should perform text sanitation and not assume this will.
     */
    @FXML
    void submit() {
        //consume the textInput field
        String rawInputString = textInput.textProperty().getValueSafe().trim();
        if (rawInputString.isEmpty()) return;
        textInput.clear();
        // if the input was the same as the last input, add a space in order trigger the change listener
        if (rawInputString.equals(submittedInput.getValueSafe())) rawInputString = rawInputString + " ";
        submittedInput.setValue(rawInputString);

    }

    @Override
    @Nonnull
    public Consumer<ChangeListener<String>> getSubmittedInputSubscriber() {
        return submittedInput::addListener;
    }

    @Override
    @CheckForNull
    public TextField getTextInput() {
        return textInput;
    }

    @Override
    @Nonnull
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
