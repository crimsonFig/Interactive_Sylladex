package app.ui;

import java.net.URL;

/**
 * @implSpec resource paths must be given as absolute paths, relative to the `/resources` folder.
 */
enum VIEW_RESOURCE {
    CSS_APPLICATION("/view/application.css"),
    FXML_COMPONENT_CLASSIC_CONSOLE_INPUT("/view/classic_style/InputComponent-Console.fxml"),
    FXML_COMPONENT_CLASSIC_CONSOLE_OUTPUT("/view/classic_style/OutputComponent-Console.fxml"),
    FXML_COMPONENT_CLASSIC_SIDEBAR("/view/classic_style/SidebarComponent.fxml"),
    FXML_COMPONENT_SIMPLE_ROOT_BORDERPANE("/view/simple/RootComponent-BorderPane.fxml"),
    FXML_COMPONENT_SIMPLE_DISPLAY_STACKPANE("/view/simple/DisplayComponent-StackPane.fxml"),
    FXML_COMPONENT_SIMPLE_MENUBAR("/view/simple/MenuBarComponent.fxml");

    private String path;

    VIEW_RESOURCE(String viewPath) {
        this.path = viewPath;
    }

    String getExternalPath() {
        return VIEW_RESOURCE.class.getResource(path).toExternalForm();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + this.getExternalPath();
    }

    URL toURL() {
        return VIEW_RESOURCE.class.getResource(path);
    }}
