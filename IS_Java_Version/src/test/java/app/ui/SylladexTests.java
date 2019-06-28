package app.ui;

import app.Launcher;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

@ExtendWith(ApplicationExtension.class)
class SylladexTests {

    @Start
    void start(Stage stage) {
        new Launcher().start(stage);
    }

    @Test
    @DisplayName("submit textfield and button control test")
    void submitUserCaseTest(FxRobot robot) {
        //ensure that the controls are disabled
        FxAssert.verifyThat("#textInput", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#bInputButton", NodeMatchers.isDisabled());
        //select a modus to enable textfield control
        robot.clickOn("#modusMenuListButton");
        FxAssert.verifyThat("#textInput", NodeMatchers.isEnabled());
        //type into the textfield to enable button control
        robot.clickOn("#textInput");
        robot.type(KeyCode.F,5);
        FxAssert.verifyThat("#bInputButton", NodeMatchers.isEnabled());
    }
}
