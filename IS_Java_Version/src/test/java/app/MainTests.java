package app;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class MainTests {
    private Stage stage;

    @Start
    private void onStart(Stage stage) {
        new Main().start(stage);
        this.stage = Main.getStage();
    }

    @Test
    void view_is_shown(FxRobot fxRobot) {
        Assertions.assertTrue(stage.isShowing());
        FxAssert.verifyThat(stage.getScene().getRoot(), Node::isVisible);
    }
}