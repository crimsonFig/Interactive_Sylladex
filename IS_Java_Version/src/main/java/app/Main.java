package app;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * 
 * @author Triston Scallan
 *
 */
public class Main extends Application {
	private static Stage stage;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			BorderPane root = FXMLLoader.load(getClass().getResource("/view/ISGui.fxml"));
			Scene scene = new Scene(root,1000,700);
			scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Interactive Sylladex");
			primaryStage.show();
		} catch (IOException e) {
		    System.err.print("FXMLLoader failed to load resource for the view at start.\n");
			e.printStackTrace();
            System.exit(-1);
		} catch(RuntimeException e) {
			e.printStackTrace();
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
