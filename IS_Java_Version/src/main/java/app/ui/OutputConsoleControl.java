package app.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OutputConsoleControl implements TextOutputComponent, LoadableFXML {
    static final VIEW_RESOURCE FXML_FILE = VIEW_RESOURCE.FXML_COMPONENT_CLASSIC_CONSOLE_OUTPUT;

    @FXML private TextArea textOutput;

    @Override
    @CheckForNull
    public TextArea getTextOutput() {
        return textOutput;
    }

    @Override
    @Nonnull
    public VIEW_RESOURCE getViewResource() {
        return FXML_FILE;
    }
}
