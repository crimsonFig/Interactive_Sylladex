package app.model;

import commandline_utils.Searcher;

import java.util.LinkedHashMap;

public class SyllCommandMap extends LinkedHashMap<String, Runnable> {
    private static final long serialVersionUID = 1L;
    //TODO: refactor to delegation pattern - create `CaseInsensitiveMap` that this class can delegate to
    //      this would be done to allow an explicit setter/constructor to remove need for a throwable in put.
    //      would combine both command map class and put behavior as composition fields used in delegation methods.

    /**
     * {@inheritDoc}
     * <br><br>
     * *Additionally, enforces case-insensitive keys by preventing multiple case-insensitive matching keys from being
     * added to the map. Current enforcement procedure is to throw an exception.
     *
     * @return the runnable corresponding to the command, or null if there is no mapping for the given command
     *
     * @throws IllegalArgumentException
     *         if the key matches a mapped key when case-insensitive but is not equal as Strings.
     */
    @Override
    public Runnable put(String key, Runnable value) {
        String mappedKey = Searcher.caseInsensitiveKeySearch(this, key);
        if (mappedKey != null && !mappedKey.equals(key)) {
            throw new IllegalArgumentException("key matches an existing, mapped key while not being equal to the key");
        }
        return super.put(key, value);
    }

    /**
     * {@inheritDoc}
     *
     * @return the runnable corresponding to the command, or null if there is no mapping for the given command
     *
     * @implNote the key, expected to be a string, will be treated as case insensitive, returning the first
     *         match
     */
    @Override
    public Runnable get(Object key) {
        return super.get(Searcher.caseInsensitiveKeySearch(this, ((String) key)));
    }
}
