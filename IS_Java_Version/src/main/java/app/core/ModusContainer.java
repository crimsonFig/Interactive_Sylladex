package app.core;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;
import app.modus.Modus;
import app.util.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * The ModusContainer class provides support to the sylladex in managing the modi in the modus package. This enables users to take a valid
 * and {@link app.modus.Modus Modus} conforming java class and "plug" it into the sylladex for use.
 * <br><br> This class also tracks what modus is currently in use, and what modi are currently available, as well as
 * metadata about the particular modus, to help let the sylladex "know" how to handle certain events and actions.
 * <p> Author's note: originally the direction of the Modus Manager was to allow users to add
 * a java file that complied with Modus interface to a file directory and then the sylladex could be refreshed and load the code given, but
 * this form of plug-and-play of adding "valid" modus java files to to a given directory for the manager to pick up for the sylladex to use
 * is a bit too insecure for my preference. it would be better to simply allow for a person to mod the open-source version of this by adding
 * those java files mentioned above to the modus package and then compile it into a jar. Later, once the modusValidation function can be
 * more robust and check for malicious behavior, then this direction can be resumed and utilize the files through a URL filestream from
 * getResourcesAsStream to temp files for the jar to use until the sylladex is closed, or use file chooser which would allow the jar to keep
 * track of the various modus files wherever they happen to reside. would require re- enabling the tracking of File paths through Metadata
 * class.
 *
 * @author Triston Scallan
 * @see app.modus.Modus
 */
@ParametersAreNonnullByDefault
class ModusContainer {
    static final         String                          MODUS_PREFIX = "modus.";
    private static final Logger                          LOGGER       = LogManager.getLogger(ModusContainer.class);
    /** Tracks the current active Modus as an index of {@link #modusClassList}. -1 means no active Modus. */
    private final        ReadOnlyObjectWrapper<Metadata> currentModusMetadata;
    private final        StringProperty                  modusInput;
    /** Tracks all available Fetch Modi for the Sylladex */
    private final        List<Class<? extends Modus>>    modusClassList;
    private              ModusBuffer                     modusBuffer;

    /**
     * Constructor
     *
     * @param submittedInputSubscriberProperty
     *         the property for adding listeners to submitted input
     * @param displayProperty
     *         the property for the current display
     * @param outputProperty
     *         the property for the current text output
     * @param inputProperty
     *         the property for the current text input
     * @param deckProperty
     *         the property for the master deck
     * @param openHandProperty
     *         the property for the open hand
     */
    ModusContainer(ReadOnlyObjectProperty<Consumer<ChangeListener<String>>> submittedInputSubscriberProperty,
                   ReadOnlyObjectProperty<? extends Pane> displayProperty,
                   ReadOnlyObjectProperty<? extends TextInputControl> outputProperty,
                   ReadOnlyObjectProperty<? extends TextInputControl> inputProperty,
                   ListProperty<Card> deckProperty,
                   ListProperty<String> openHandProperty) {
        this.modusInput = new SimpleStringProperty(this, "modus_input", "");
        this.modusBuffer = new ModusBuffer(modusInput, displayProperty, outputProperty, deckProperty, openHandProperty);
        this.currentModusMetadata = new ReadOnlyObjectWrapper<>(this, "current_modus_metadata", null);

        modusClassList = ModusLocator.getModiAsClassList();
        LOGGER.info("ClassListing: complete.");
        if (modusClassList.isEmpty()) {
            throw new RuntimeException("Final result of modusClassList in ModusContainer constructor is empty.");
        }
        try {
            currentModusMetadata.addListener((bean, oldV, newV) -> inputProperty.getValue().setDisable(newV == null));
            displayProperty.getValue().widthProperty().addListener((bean, oldV, newV) -> {
                if (this.getCurrentModusMetadata() != null) {
                    Platform.runLater(this::requestDrawToDisplay);
                }
            });
            displayProperty.getValue().heightProperty().addListener((bean, oldV, newV) -> {
                if (this.getCurrentModusMetadata() != null) Platform.runLater(this::requestDrawToDisplay);
            });
            submittedInputSubscriberProperty.getValue().accept(this::handleModusInput);
        } catch (RuntimeException e) {
            LOGGER.catching(e);
        }

    }

    //************** GETTERS/SETTERS *******************/

    /**
     * @return the currentModusMetadata
     */
    @CheckForNull
    Metadata getCurrentModusMetadata() {
        return currentModusMetadata.getValue();
    }

    /**
     * @return the read only property of the current Metadata object of the current active Modus
     */
    @Nonnull
    public ReadOnlyObjectProperty<Metadata> currentModusMetadataProperty() {
        return currentModusMetadata.getReadOnlyProperty();
    }

    /**
     * Utilized by the UI to display modus selection as well as the ability to check against runtime types.
     *
     * @return the unmodifiable list of all Class objects implementing the Modus interface
     */
    @Nonnull
    List<Class<? extends Modus>> getModusClassList() {
        return Collections.unmodifiableList(modusClassList);
    }

    //*************** UTILITY **************************/

    void handleModusInput(ObservableValue<? extends String> bean, String oldInput, @Nullable String newInput) {
        LOGGER.traceEntry("handleSyllInput(bean={}, oldInput={}, newInput={}", bean, oldInput, newInput);
        if (newInput == null) {
            LOGGER.warn("New input for submitted input handler was null. " +
                        "Input should not be set to null or changed outside of the input component controller. " +
                        "Please review and revise code. ");
            return;
        } else if (getCurrentModusMetadata() == null) {
            LOGGER.warn("New input while modus is not selected. Please review and revise code to prevent this warning.");
            return;
        }

        Optional<Consumer<ModusBuffer>> redirector = modusBuffer.getAndResetModusInputRedirector();
        //if a redirection is present then run it's method, otherwise parse input and execute command.
        if (redirector.isPresent()) {
            try {
                // run the modus lambda that is pointed to by the redirection
                modusInput.setValue(newInput);
                redirector.get().accept(modusBuffer);
            } catch (FatalModusException e) {
                e.printStackTraceLess(4);
                System.err.print("restarting modus.");
                modusBuffer.getTextOutput().appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
                recoverFromModusFailure();
            } catch (ModusRuntimeException e) {
                e.printStackTraceLess(4);
                modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
                modusBuffer.clearModusInputRedirector();
            }
        } else {
            // if input does not have modus prefix, input is not for this handler
            if (!newInput.trim().toUpperCase().startsWith(MODUS_PREFIX.toUpperCase())) return;

            //split the input into ["command", "arg1 arg2 arg3..."]
            String[] splitInput = newInput.trim().substring(MODUS_PREFIX.length()).split(" ", 2);
            String   command    = splitInput[0];

            //split the args string into a list, if any, then run the commandSwitch
            if (splitInput.length > 1) {
                String[] inputArgs = splitInput[1].split(",");
                for (int i = 0; i < inputArgs.length; i++) {
                    inputArgs[i] = inputArgs[i].trim();
                }
                execModusCmd(command, inputArgs);
            } else {
                execModusCmd(command);
            }
        }
        // reset modus input to a 'no input' state.
        modusInput.setValue("");
        LOGGER.traceExit();
    }

    /**
     * Messages a modus command statement to the currently selected modus for execution
     *
     * @param command
     *         a modus command associated with a ModusCommandMap entry
     * @param args
     *         the arguments to supply the command with
     */
    private void execModusCmd(String command, String... args) {
        LOGGER.traceEntry("invoking execModusCmd(command={}, args={}", command, args);
        try {
            currentModusMetadata.get().COMMAND_MAP.command(command, args, modusBuffer);
        } catch (NoSuchCommandException e) {
            LOGGER.error(command + " is not a known command.", e);
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
        } catch (IllegalSyntaxException e) {
            LOGGER.error("Expected args for command " + command + " was invalid: " + Arrays.toString(args), e);
            String errMsg = "ERROR - " + e + ".\n" + currentModusMetadata.get().COMMAND_MAP.desc(command) + "\n";
            modusBuffer.getTextOutput().appendText(errMsg);
        } catch (CommandRuntimeException e) {
            LOGGER.error("Exception occurred in modus that prevented completion of command. Modus integrity unknown.", e);
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
            modusBuffer.clearModusInputRedirector();
        } catch (FatalModusException e) {
            LOGGER.error("Fatal exception occurred in modus that prevented completion of command. " +
                         "Modus integrity is expected to be corrupted or invalid. Sylladex may attempt to restart/recover modus.", e);
            LOGGER.info("Restarting modus...");
            modusBuffer.getTextOutput().appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
            recoverFromModusFailure();
        }
    }

    void requestSave() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        getCurrentModusMetadata().REFERENCE.save(modusBuffer);
    }

    void requestLoad() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        getCurrentModusMetadata().REFERENCE.load(modusBuffer);
    }

    void requestDrawToDisplay() {
        if (getCurrentModusMetadata() == null) {
            LOGGER.error("No modus selected, unable to request a draw to display.");
        } else {
            getCurrentModusMetadata().REFERENCE.drawToDisplay(modusBuffer);
        }
    }

    @Nonnull
    String requestDescription() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        return Optional.ofNullable(getCurrentModusMetadata().REFERENCE.description()).orElse("");
    }

    /**
     * Instantiates the given <code>Modus</code> class and checks for instance validation. Will clear the
     * <code>modusBuffer</code> input and redirector fields, even if an exception is thrown.
     *
     * @param modusClass
     *         the modus to update the current selection for interfacing with
     * @throws RuntimeException
     *         if the class fails Metadata validation or was unable to be instantiated
     * @see Metadata#isValid(Metadata)
     */
    void updateCurrentModus(Class<? extends Modus> modusClass) throws RuntimeException {
        // reset any leftover values from the current modus object
        modusBuffer.clearModusInputRedirector();
        modusInput.setValue("");
        // replace with the new modus object in the form of the metadata object reference.
        Metadata newModusMetadata = ModusFactory.getModusMetadata(modusClass);
        currentModusMetadata.setValue(newModusMetadata);
    }

    /**
     * Initializes a new object of the current modus class, then effectively replaces the old object's reference. Includes (transitively) a
     * side effect of resetting the modusBuffer's input and redirector fields.
     *
     * @throws NullPointerException
     *         If there is no current modus metadata object active
     * @throws RuntimeException
     *         if the modus' constructor cannot be accessed or fails
     */
    void resetModus() throws RuntimeException {
        Objects.requireNonNull(getCurrentModusMetadata(), "Cannot reset modus if no modus is active.");
        try {
            updateCurrentModus(getCurrentModusMetadata().REFERENCE.getClass());
        } catch (RuntimeException e) {
            LOGGER.throwing(e);
            throw e;
        }
    }

    /**
     * Attempts to recover the current <code>Modus</code> instance back into a working state.
     *
     * @implNote resets the modus and on failure of that sets the <code>currentModusMetadata</code> to null, clears display.
     *         Transitively, this will also reset any redirection in the modus buffer, as the call to updateCurrentModus will be called
     *         eventually.
     * @see #resetModus()
     * @see #updateCurrentModus(Class)
     * @see ModusBuffer#clearModusInputRedirector()
     */
    private void recoverFromModusFailure() {
        try {
            resetModus();
        } catch (RuntimeException e) {
            LOGGER.error("Modus failure recovery method failed!", e);
            //catastrophic failure. unload modus and reset the view.
            currentModusMetadata.setValue(null);
            modusBuffer.getDisplay().getChildren().clear();
            modusBuffer.getTextOutput().appendText("!!!catastrophic modus error!!!\nPlease select a different modus.\n");
        }
    }
}
