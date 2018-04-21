package commandline_utils;

/**
 * @author Triston Scallan
 *
 */
public interface Parser {

	/**
	 * @param command
	 * @param args
	 */
	public abstract void commandSwitch(String command, String...args);
}
