package app.util;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SyllCommandMap extends CommandMap<Runnable> {
    public SyllCommandMap(Case caseType) {
        super(caseType);
    }

    /**
     * executes the <code>Runnable</code> associated with the given command
     * @param command the command to be used as a key
     * @throws NoSuchCommandException if the given command is associated with null
     */
    public void command(String command) throws NoSuchCommandException {
        Optional.ofNullable(get(command))
                .orElseThrow(() -> NoSuchCommandException.forCommand(command))
                .run();
    }

    @Override
    public Runnable put(String key, Runnable value) {
        Objects.requireNonNull(value, "ERROR: SyllCommandMap#put requires a non-null value");
        return super.put(key, value);
    }

    public static boolean isValid(SyllCommandMap map) {
        if (map == null) return false;
        for(Map.Entry<String, Runnable> entry : map.entrySet()) {
            String commandName = entry.getKey();
            Runnable commandValue = entry.getValue();

            //Name is empty or command is null -> return false
            if (commandName != null && commandName.isEmpty() ||
                commandName != null && commandValue == null)
                return false;
        }
        return true;
    }
}
