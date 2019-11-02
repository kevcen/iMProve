package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import other.Main;

import java.io.IOException;

public class GradeController {
    private String name;
    @FXML
    private Text txtSelectGrade;

    public void setName(String name) { //input the user's name
        this.name = name;
        Platform.runLater(() -> txtSelectGrade.setText("Select Your Grade, " + name + "."));
    }

    @FXML
    public void close(ActionEvent e) { //close system
        System.exit(0);
    }

    @FXML
    public void gradeSelect(ActionEvent e) { //load next part of system after grade selected
        //get grade
        Button selectedButton = (Button) e.getSource();
        int grade = Integer.parseInt(selectedButton.getText());
        //load root gui and set some initial values
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/root.fxml"));
        AnchorPane root = null;
        try {
            root = loader.load();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        RootController rootController = loader.getController();
        rootController.initData(name, grade);

        Scene rootScene = new Scene(root);
        rootScene.setFill(null);
        Main.getStage().setScene(rootScene);
    }

}
