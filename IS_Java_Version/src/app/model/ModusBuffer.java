package app.model;

import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ModusBuffer {
    private       Consumer<ModusBuffer>         modusInputRedirector = null;
    private final AtomicReference<String>       wrappedModusInput;
    private final AtomicReference<StackPane>    wrappedDisplay;
    private final AtomicReference<TextArea>     wrappedTextOutput;
    private final AtomicReference<List<Card>>   wrappedDeck;
    private final AtomicReference<List<String>> wrappedOpenHand;

    public ModusBuffer(AtomicReference<String> wrappedModusInput,
                       AtomicReference<StackPane> wrappedDisplay,
                       AtomicReference<TextArea> wrappedTextOutput,
                       AtomicReference<List<Card>> wrappedDeck,
                       AtomicReference<List<String>> wrappedOpenHand) {
        this.wrappedModusInput = wrappedModusInput;
        this.wrappedDisplay = wrappedDisplay;
        this.wrappedTextOutput = wrappedTextOutput;
        this.wrappedDeck = wrappedDeck;
        this.wrappedOpenHand = wrappedOpenHand;
    }

    public void clearModusInputRedirector() {
        modusInputRedirector = null;
    }

    public Consumer<ModusBuffer> getModusInputRedirector() {
        return modusInputRedirector;
    }

    public Consumer<ModusBuffer> getAndResetModusInputRedirector() {
        Consumer<ModusBuffer> modusInputRedirectorValue = modusInputRedirector;
        modusInputRedirector = null;
        return modusInputRedirectorValue;
    }

    public void setModusInputRedirector(Consumer<ModusBuffer> modusInputRedirector) {
        this.modusInputRedirector = modusInputRedirector;
    }

    public void clearModusInput() {
        wrappedModusInput.set(null);
    }

    public String getModusInput() {
        return wrappedModusInput.get();
    }

    public String getAndResetModusInput() {
        return wrappedModusInput.getAndSet(null);
    }

    public StackPane getDisplay() {
        return wrappedDisplay.get();
    }

    public TextArea getTextOutput() {
        return wrappedTextOutput.get();
    }

    public List<Card> getDeck() {
        return wrappedDeck.get();
    }

    public List<String> getOpenHand() {
        return wrappedOpenHand.get();
    }
}

