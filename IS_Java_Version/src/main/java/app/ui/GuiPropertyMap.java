package app.ui;

import app.modus.Modus;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 */
@ParametersAreNonnullByDefault
public class GuiPropertyMap
        implements MenuBarComponent, CmdInfoComponent, ModusSelectComponent, DisplayComponent, TextInputComponent, TextOutputComponent {

    private final ReadOnlyObjectWrapper<Pane>             display;
    private final ReadOnlyObjectWrapper<TextInputControl> textInput;
    private final ReadOnlyObjectWrapper<TextInputControl> textOutput;

    private final ReadOnlyObjectWrapper<Consumer<ChangeListener<String>>>          submittedInputSubscriber;
    private final ReadOnlyObjectWrapper<ObjectProperty<EventHandler<ActionEvent>>> modusSelectionHandlerProperty;

    private final ReadOnlyListWrapper<Node>                   syllCmdListChildren;
    private final ReadOnlyListWrapper<Node>                   modusCmdListChildren;
    private final ReadOnlyListWrapper<Class<? extends Modus>> modusMenuSelectableClassList;


    GuiPropertyMap(MenuBarComponent menuBarComponent,
                   CmdInfoComponent cmdInfoComponent,
                   ModusSelectComponent modusSelectComponent,
                   DisplayComponent displayComponent,
                   TextInputComponent textInputComponent,
                   TextOutputComponent textOutputComponent) {
        display = new ReadOnlyObjectWrapper<>(this,
                                              "display",
                                              Objects.requireNonNull(displayComponent.getDisplay(),
                                                                     "Expected GUI elements must not be null."));
        textInput = new ReadOnlyObjectWrapper<>(this,
                                                "text_input",
                                                Objects.requireNonNull(textInputComponent.getTextInput(),
                                                                       "Expected GUI elements must not be null."));
        textOutput = new ReadOnlyObjectWrapper<>(this,
                                                 "text_output",
                                                 Objects.requireNonNull(textOutputComponent.getTextOutput(),
                                                                        "Expected GUI elements must not be null."));
        submittedInputSubscriber = new ReadOnlyObjectWrapper<>(this,
                                                               "submitted_input_subscriber",
                                                               textInputComponent.getSubmittedInputSubscriber());
        // note!! while this is a 'read only' list property - this only prevents the list from being replaced, but we can add to this list!
        syllCmdListChildren = new ReadOnlyListWrapper<>(this, "sylladex_command_list_children", cmdInfoComponent.getSyllCmdListChildren());
        modusCmdListChildren = new ReadOnlyListWrapper<>(this, "modus_command_list_children", cmdInfoComponent.getModusCmdListChildren());
        modusMenuSelectableClassList = new ReadOnlyListWrapper<>(this,
                                                                 "modus_menu_selectable_class_list",
                                                                 modusSelectComponent.getModusMenuSelectableClassList());
        modusSelectionHandlerProperty = new ReadOnlyObjectWrapper<>(this,
                                                                    "modus_selection_handler_property",
                                                                    modusSelectComponent.modusSelectionHandlerProperty());
    }

    /* Getters - Public access ************************************************************************************** */
    @Nonnull
    @Override
    public ObjectProperty<EventHandler<ActionEvent>> modusSelectionHandlerProperty() {
        return modusSelectionHandlerProperty.getValue();
    }

    @Nonnull
    @Override
    public ObservableList<Class<? extends Modus>> getModusMenuSelectableClassList() {
        return modusMenuSelectableClassList.getValue();
    }

    @Nonnull
    @Override
    public ObservableList<Node> getSyllCmdListChildren() {
        return syllCmdListChildren.getValue();
    }

    @Nonnull
    @Override
    public ObservableList<Node> getModusCmdListChildren() {
        return modusCmdListChildren.getValue();
    }

    @Nonnull
    @Override
    public Pane getDisplay() {
        return display.getValue();
    }

    @Nonnull
    @Override
    public Consumer<ChangeListener<String>> getSubmittedInputSubscriber() {
        return submittedInputSubscriber.getValue();
    }

    @Nonnull
    @Override
    public TextInputControl getTextInput() {
        return textInput.getValue();
    }

    @Nonnull
    @Override
    public TextInputControl getTextOutput() {
        return textOutput.getValue();
    }

    /* Property getters - read only access for external classes to apply listeners to ******************************* */
    public ReadOnlyListProperty<Node> syllCmdListChildrenProperty() {
        return syllCmdListChildren.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<Node> modusCmdListChildrenProperty() {
        return modusCmdListChildren.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<? extends Pane> displayProperty() {
        return display.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<? extends TextInputControl> textInputProperty() {
        return textInput.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<? extends TextInputControl> textOutputProperty() {
        return textOutput.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Consumer<ChangeListener<String>>> submittedInputSubscriberProperty() {
        return submittedInputSubscriber.getReadOnlyProperty();
    }

    /* Setters - Package access only for GUI classes to update, external shouldn't access *************************** */
    void setSyllCmdListChildren(ObservableList<Node> syllCmdListChildren) {
        this.syllCmdListChildren.setValue(syllCmdListChildren);
    }

    void setModusCmdListChildren(ObservableList<Node> modusCmdListChildren) {
        this.modusCmdListChildren.setValue(modusCmdListChildren);
    }

    void setDisplay(Pane display) {
        this.display.setValue(display);
    }

    void setTextInput(TextInputControl textInput) {
        this.textInput.setValue(textInput);
    }

    void setTextOutput(TextInputControl textOutput) {
        this.textOutput.setValue(textOutput);
    }

    void setSubmittedInputSubscriber(Consumer<ChangeListener<String>> submittedInputSubscriber) {
        this.submittedInputSubscriber.setValue(submittedInputSubscriber);
    }

    /* Setters - Public access *************************************************************************************** */

    @Override
    public void setAllModusMenuSelectableClassList(Collection<Class<? extends Modus>> modusList) {
        modusMenuSelectableClassList.setAll(modusList);
    }
}
