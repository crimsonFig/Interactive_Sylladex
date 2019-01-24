package app.util;

import java.util.Map;

public class SyllCommandMap extends CommandMap<Runnable> {
    public SyllCommandMap(Case caseType) {
        super(caseType);
    }

    public void command(String command) {
        super.get(command).run();
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
