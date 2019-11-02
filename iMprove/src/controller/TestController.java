package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class TestController {
    private RootController rootController;
    @FXML
    private AnchorPane apnWelcome;

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    //load test quiz
    @FXML
    public void playTest(ActionEvent e) {
        AnchorPane root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test_quiz.fxml"));
            root = loader.load();
            TestQuizController controller = loader.getController();
            controller.initData(rootController, apnWelcome);
            if (controller.getEnoughTerms())
                rootController.setActivity(root); //if there are enough terms then you can load quiz
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
