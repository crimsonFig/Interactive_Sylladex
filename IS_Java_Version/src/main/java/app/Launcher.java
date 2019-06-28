package app;
	
import app.ui.*;
import app.core.Sylladex;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author Triston Scallan
 *
 */
public class Launcher extends Application {
	private static Stage stage;
	private static Sylladex sylladex;
	private static GUIManager guiManager;
	private static final Logger LOGGER = LogManager.getLogger(Launcher.class);

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Interactive Sylladex");
		try {
			Launcher.stage = primaryStage;
			Launcher.guiManager = new ClassicStyleLayoutGUIM(primaryStage);
		} catch (IOException e) {
		    LOGGER.error("FXMLLoader failed to read/load resources for the view at start.", e);
            System.exit(-1);
		} catch (NullPointerException e) {
			LOGGER.error("Exception in initializing the UI, null object accessed.", e);
			System.exit(-1);
		} catch(RuntimeException  e) {
			LOGGER.error("Unexpected failure in initializing UI.", e);
			System.exit(-1);
		}
		try {
			Launcher.sylladex = new Sylladex(guiManager.getGuiPropertyMap());
		} catch (RuntimeException e) {
			LOGGER.error("Unexpected failure in initializing Core.", e);
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * @return the stage
	 */
	public static Stage getStage() {
		return stage; 
	}
}
