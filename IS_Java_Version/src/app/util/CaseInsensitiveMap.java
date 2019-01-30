package app.util;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * A rudimentary linked hash map for keys of type String that handles `get` and `put` methods as if the key was
 * case-insensitive. This is achieved with a second map that backs the case-insensitive keys to the stored
 * case-sensitive keys.
 * <br>
 * Adding elements to the map with any method other than `put` may have unexpected behavior.
 *
 * @param <V>
 */
class CaseInsensitiveMap<V> extends LinkedHashMap<String, V> {
    private HashMap<String, String> keyCasingMap = new HashMap<>();

    CaseInsensitiveMap() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @return the value corresponding to the key, or null if there is no mapping for the given key
     *
     * @implNote the key, expected to be a string, will be treated as case insensitive
     */
    @Override
    public V get(Object key) {
        try {
            return super.get(getCaseSensitiveKey(String.class.cast(key)));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("key was expected to be a String, found " + key.getClass(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * *Additionally, if two keys are considered equal when converted to uppercase, the new key will replace the old key
     * and the new value will replace the old key's mapped value. This is to preserve desired casing when a key set is
     * derived from the map.
     */
    @Override
    public V put(String key, V value) {
        String prevMappedKey = putCaseSensitiveKey(key);
        //if matching but not identical, replace this map's mapping
        if (prevMappedKey != null && !prevMappedKey.equals(key)) {
            V oldValue = super.remove(prevMappedKey);
            super.put(key, value);
            return oldValue;
        }
        return super.put(key, value);
    }

    /**
     * maps a case-less version of the given key to the given key. this method is used to facilitate integrity of the
     * case insensitive map.
     *
     * @param key
     *         the key to associate with a case-less version of the key
     * @return the old mapping if it exists, null otherwise
     */
    private String putCaseSensitiveKey(String key) {
        return keyCasingMap.put(convertKeyToCaseless(key), key);
    }

    /**
     * gets the currently mapped, cased key with the given key, if the key exists in this map. this method is used to
     * derive this map's keys with any given key if the two match when both are case-less.
     *
     * @param key
     *         the given key to derive a matching cased key from this map, null if no match found
     * @return the found, cased key that is currently mapped to a value. null otherwise.
     */
    private String getCaseSensitiveKey(String key) {
        return keyCasingMap.get(convertKeyToCaseless(key));
    }

    /**
     * Converts the key string to a case-less form of the string. Used to keep consistency and integrity with matching.
     *
     * @param key
     *         the key to convert
     * @return a string that represents the key in a case insensitive form
     */
    private String convertKeyToCaseless(String key) {
        return (key == null) ? null : key.toUpperCase();
    }
}
