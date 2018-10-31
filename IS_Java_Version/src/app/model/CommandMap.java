package app.model;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import javafx.util.Pair;

/**
 * A mapping between a modus' command name and it's associated functional 
 * lambda and command description. Modifying this list may cause undefinable 
 * behavior within the sylladex and modus.
 * @author Triston Scallan
 *
 */
public class CommandMap extends LinkedHashMap<String, Pair<Consumer<String[]>, String>> {
	private static final long serialVersionUID = 1L;
	/**
	 * constant value that determines an unmatched command
	 */
	public static final String CMD_ERR = null;
	
	/**
	 * @param command 
	 * @param args
	 */
	public void command(String command, String[] args) {
		this.get(command).getKey().accept(args);
	}
	/**
	 * @param command 
	 * @return
	 */
	public String desc(String command) {
		return this.get(command).getValue();
	}
}
