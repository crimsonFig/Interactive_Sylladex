package app.ui;

import app.modus.Modus;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface ModusSelectComponent extends GUIComponent {
    /**
     * The preferred prefix to be used when specifying node IDs that reference a selectable modus. The intended use is to use the format
     * `{@code Button_ID_PREFIX + modusClassObject.getCanonicalName()}` when setting the node ID string.
     */
    String BUTTON_ID_PREFIX = "modus_select_button_";

    /**
     * getter for the expected modusMenuSelectableClassList object. This object is to be used by an external domain class (such as the core
     * class {@link app.core.Sylladex}) to add and remove models that need to be presented by the UI. The particular list referred by this
     * getter will require the subclass of this interface to (within the presentation code) include a selection handler injection by the
     * {@link #modusSelectionHandlerProperty()}, which will provide a handler implementation during runtime.
     *
     * @return a list contained Class objects that reference Modus subclasses, an element of the list represents a selectable modus class.
     *
     * @implSpec the getter must provide a read-write enabled list that is owned by the instance object.
     */
    @Nonnull
    ObservableList<Class<? extends Modus>> getModusMenuSelectableClassList();

    /**
     * setter for the modusMenuSelectableClassList object. This method should only be called after FXML has been loaded and the initialize
     * method finished invoking, and is primarily for synchronizing the domain data between UI ModusSelectComponent swaps so that the UI
     * state can be transferred to a new UI during runtime.
     *
     * @param selectableClassList
     *         the (possibly empty) collection of selectable Modus subclass Class objects.
     * @throws NullPointerException
     *         if the collection is null or the collection contains a null element.
     */
    void setAllModusMenuSelectableClassList(@Nonnull Collection<Class<? extends Modus>> selectableClassList);

    /**
     * This property allows the core to supply a modus selection handler. This value is meant to be set by an external class, rather than be
     * read. This would allow to dynamically set the handler for use in a listener for when the modus menu list is changed.
     *
     * @return the Property object holding the modus selection handler
     */
    @Nonnull
    ObjectProperty<EventHandler<ActionEvent>> modusSelectionHandlerProperty();
}
