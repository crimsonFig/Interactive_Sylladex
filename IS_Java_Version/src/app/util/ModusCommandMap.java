package app.util;

import javafx.util.Pair;
import modus.ModusBuffer;

import java.util.Map;
import java.util.function.BiConsumer;

public class ModusCommandMap extends CommandMap<Pair<BiConsumer<String[], ModusBuffer>, String>> {
    public ModusCommandMap(Case caseType) {
        super(caseType);
    }

    public void command(String command, String[] args, ModusBuffer modusBuffer) {
        super.get(command).getKey().accept(args, modusBuffer);
    }

    public String desc(String command) {
        return super.get(command).getValue();
    }

    public static boolean isValid(ModusCommandMap map) {
        if (map == null) return false;
        for(Map.Entry<String, Pair<BiConsumer<String[], ModusBuffer>, String>> entry : map.entrySet()) {
            String commandName = entry.getKey();
            Pair<BiConsumer<String[], ModusBuffer>, String> commandPair = entry.getValue();

            //Name is empty, command is null, or description is empty -> return false
            if (commandName != null && commandName.isEmpty() ||
                commandPair.getKey() == null ||
                commandPair.getValue().isEmpty())
                return false;
        }
        return true;
    }
}
