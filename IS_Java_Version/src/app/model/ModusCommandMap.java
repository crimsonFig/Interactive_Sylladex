package app.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import commandline_utils.Searcher;
import javafx.util.Pair;
import modus.ModusBuffer;

/**
 * A mapping between a modus' command name and it's associated functional 
 * lambda and command description. Modifying this list may cause undefinable 
 * behavior within the sylladex and modus.
 * @author Triston Scallan
 *
 */
public class ModusCommandMap extends LinkedHashMap<String, Pair<BiConsumer<String[], ModusBuffer>, String>> {
	private static final long serialVersionUID = 1L;
	/**
	 * constant value that determines an unmatched command
	 */
	public static final String CMD_ERR = null;
	
	/**
	 * @param command
	 * @param args
	 * @param modusBuffer
	 */
	public void command(String command, String[] args, ModusBuffer modusBuffer) {
		this.get(Searcher.caseInsensitiveKeySearch(this, command)).getKey().accept(args, modusBuffer);
	}
	/**
	 * @param command 
	 * @return
	 */
	public String desc(String command) {
		return this.get(command).getValue();
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
