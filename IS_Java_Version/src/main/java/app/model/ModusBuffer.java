package app.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModusBuffer {
    private final StringProperty                                     modusInputProperty;
    private final ReadOnlyObjectProperty<? extends Pane>             displayProperty;
    private final ReadOnlyObjectProperty<? extends TextInputControl> textOutputProperty;
    private final ListProperty<Card>                                 deckProperty;
    private final ListProperty<String>                               openHandProperty;
    private       Consumer<ModusBuffer>                              inputRedirector = null;

    public ModusBuffer(StringProperty modusInputProperty,
                       ReadOnlyObjectProperty<? extends Pane> displayProperty,
                       ReadOnlyObjectProperty<? extends TextInputControl> textOutputProperty,
                       ListProperty<Card> deckProperty,
                       ListProperty<String> openHandProperty) {
        this.modusInputProperty = modusInputProperty;
        this.displayProperty = displayProperty;
        this.textOutputProperty = textOutputProperty;
        this.deckProperty = deckProperty;
        this.openHandProperty = openHandProperty;
    }

    public void clearModusInputRedirector() {
        inputRedirector = null;
    }

    public Optional<Consumer<ModusBuffer>> getInputRedirector() {
        return Optional.ofNullable(inputRedirector);
    }

    public void setInputRedirector(Consumer<ModusBuffer> inputRedirector) {
        this.inputRedirector = inputRedirector;
    }

    public Optional<Consumer<ModusBuffer>> getAndResetModusInputRedirector() {
        Consumer<ModusBuffer> modusInputRedirectorValue = inputRedirector;
        inputRedirector = null;
        return Optional.ofNullable(modusInputRedirectorValue);
    }

    public Pane getDisplay() {
        return displayProperty.get();
    }

    public TextArea getTextOutput() {
        return (TextArea) textOutputProperty.get();
    }

    public List<Card> getDeck() {
        return deckProperty.get();
    }

    public List<String> getOpenHand() {
        return openHandProperty.get();
    }

    @Nonnull
    public String getAndResetModusInput() {
        String valueSafe = modusInputProperty.getValueSafe();
        modusInputProperty.setValue("");
        return valueSafe;
    }
}

