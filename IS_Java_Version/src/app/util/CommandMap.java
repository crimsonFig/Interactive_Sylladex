package app.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class CommandMap<V> {
    private LinkedHashMap<String, V> mapCaseBehavior;

    public static final String CMD_ERR = null;

    public enum Case {
        SENSITIVE, INSENSITIVE
    }

    CommandMap(Case caseType) {
        if (caseType == Case.INSENSITIVE) {
            mapCaseBehavior = new CaseInsensitiveMap<>();
        } else if (caseType == Case.SENSITIVE) {
            mapCaseBehavior = new LinkedHashMap<>();
        } else {
            throw new IllegalArgumentException("caseType argument is not valid Case option");
        }
    }

    public V put(String key, V value) {
        return mapCaseBehavior.put(key, value);
    }

    public V get(String key) {
        return mapCaseBehavior.get(key);
    }

    public Set<String> keySet() {
        return mapCaseBehavior.keySet();
    }

    public Set<Map.Entry<String, V>> entrySet() {
        return mapCaseBehavior.entrySet();
    }

    public static <T> boolean isValid(CommandMap<T> map) {
        if (map == null) return false;
        for(Map.Entry<String, T> entry : map.entrySet()) {
            String commandName = entry.getKey();
            T commandValue = entry.getValue();

            //Name is empty or value is null -> return false
            if (commandName != null && commandName.isEmpty() ||
                commandName != null && commandValue == null)
                return false;
        }
        return true;
    }

}
