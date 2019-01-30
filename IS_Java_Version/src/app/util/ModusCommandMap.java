package app.util;

import javafx.util.Pair;
import app.model.ModusBuffer;

import java.util.*;
import java.util.function.BiConsumer;

public class ModusCommandMap extends CommandMap<Pair<BiConsumer<String[], ModusBuffer>, String>> {
    public ModusCommandMap(Case caseType) {
        super(caseType);
    }

    /**
     * executes the lambda associated with the command key
     *
     * @param command the command to be used as a map key
     * @param args the arguments to pass to the command lambda
     * @param modusBuffer the modusBuffer to pass to the command lambda
     * @throws NoSuchCommandException if the command fails to map to an existing value
     */
    public void command(String command, String[] args, ModusBuffer modusBuffer) throws NoSuchCommandException {
        Optional.ofNullable(get(command))
                .orElseThrow(() -> NoSuchCommandException.forCommand(command))
                .getKey()
                .accept(args, modusBuffer);
    }

    /**
     * gets the description associated within a command's value.
     *
     * @param command the command key associated with the target value
     * @return a string description of a command and it's proper syntax
     * @throws NoSuchCommandException if the command fails to map to an existing value
     */
    public String desc(String command) throws NoSuchCommandException {
        return Optional.ofNullable(get(command))
                       .orElseThrow(() -> NoSuchCommandException.forCommand(command))
                       .getValue();
    }

    /**
     * Associates the key with a given value
     *
     * @param key the key to associate with a value
     * @param value the value to bind to the key. replaces the old value if key already has a mapping.
     * @return the old value associated with key, if it exists. null otherwise.
     * @throws NullPointerException if the value or it's components are null
     */
    @Override
    public Pair<BiConsumer<String[], ModusBuffer>, String> put(String key,
                                                               Pair<BiConsumer<String[], ModusBuffer>, String> value) {
        Objects.requireNonNull(value, "ERROR: ModusCommandMap#put requires a non-null value");
        Objects.requireNonNull(value.getKey(), "ERROR: ModusCommandMap#put requires a non-null key in value");
        Objects.requireNonNull(value.getValue(), "ERROR: ModusCommandMap#put requires a non-null value in value");
        return super.put(key, value);
    }

    public static boolean isValid(ModusCommandMap map) {
        if (map == null) return false;
        for (Map.Entry<String, Pair<BiConsumer<String[], ModusBuffer>, String>> entry : map.entrySet()) {
            String commandName = entry.getKey();
            Pair<BiConsumer<String[], ModusBuffer>, String> commandPair = entry.getValue();

            //Name is empty, command is null, or description is empty -> return false
            if (commandName != null && commandName.isEmpty() ||
                commandPair.getKey() == null ||
                commandPair.getValue().isEmpty()) return false;
        }
        return true;
    }
}
