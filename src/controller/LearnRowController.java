package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import other.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class LearnRowController {
    private RootController rootController;
    private LearnController learnController;
    private AnchorPane apnLearn;
    private DAO dao = new DAO();

    @FXML
    private Text txtTitle;
    @FXML
    private AnchorPane apnSetRow;

    public void initData(String title, RootController rc, LearnController lc, AnchorPane apn) {
        txtTitle.setText(title);
        rootController = rc;
        learnController = lc;
        apnLearn = apn;
    }

    //play the flashcards when play button clicked
    @FXML
    public void playFlashcards(MouseEvent e) {
        AnchorPane root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/learn_flashcards.fxml"));
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        LearnFlashcardController controller = loader.getController();
        controller.initData(txtTitle.getText(), rootController, apnLearn);
        controller.initKeys();
        rootController.setActivity(root);

    }

    //edit the flashcard set
    @FXML
    public void editSet(MouseEvent e) {
        AnchorPane root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/learn_update.fxml"));
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        LearnUpdateController controller = loader.getController();
        controller.initData(txtTitle.getText(), rootController, learnController, apnLearn);
        rootController.setActivity(root);
        root.requestFocus();
    }

    //delete the flashcard set
    @FXML
    public void deleteSet(MouseEvent e) {
        //confirm first
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        confirmAlert.setHeaderText("Are you sure you want to delete this set?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.get() == ButtonType.YES) {
            //delete from database
            try (
                    Connection conn = dao.getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM Theory WHERE Title = ?");
            ) {
                conn.setAutoCommit(false);
                stmt.setString(1, txtTitle.getText());
                stmt.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            //delete from interface
            VBox vbxCards = (VBox) apnSetRow.getParent();
            vbxCards.getChildren().remove(apnSetRow);
        }
    }
}
