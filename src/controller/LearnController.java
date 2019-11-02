package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import other.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LearnController {
    private RootController rootController;
    private ObservableSet<String> titleSet = FXCollections.observableSet(); //only one unique title
    private DAO dao = new DAO();
    @FXML
    private VBox vbxSets;
    @FXML
    private AnchorPane apnLearn;

    public LearnController() {
        //pack the vbox with existing sets
        initSets();
    }

    public void initSets() {
        titleSet.clear();
        Platform.runLater(() -> {
            vbxSets.getChildren().clear();
        }); //clear all children nodes first so no repeats of sets occur

        try (
                Connection conn = dao.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT Title FROM Theory WHERE Grade <= ?");
        ) {
            stmt.setInt(1, RootController.getGrade());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("Title");
                if (!"".equals(title))
                    titleSet.add(title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            for (String s : titleSet) {
                addRow(s);
            }
        });
    }

    // add a flashcard set row on the UI
    public void addRow(String title) {
        AnchorPane row = null;
        FXMLLoader rowLoader = new FXMLLoader(getClass().getResource("/view/learn_row.fxml"));
        try {
            row = rowLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LearnRowController rowController = rowLoader.getController();
        rowController.initData(title, rootController, this, apnLearn);
        vbxSets.getChildren().add(row);
    }

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    public void createSet(MouseEvent e) {
        //get the title
        TextInputDialog titleDialog = new TextInputDialog("");
        titleDialog.setTitle("Title for your set");
        titleDialog.setHeaderText("Please provide a title for your new flashcard set");
        Optional<String> result = titleDialog.showAndWait();
        if (result.isPresent() && !result.get().equals("")) {
            AnchorPane root = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/learn_update.fxml"));
            try {
                root = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            LearnUpdateController controller = loader.getController();

            //add initial few flashcards
            for (int i = 0; i < 5; i++) controller.addRow();

            controller.initData(result.get(), rootController, this, apnLearn);
            rootController.setActivity(root);
            root.requestFocus();
        } else {
            apnLearn.requestFocus();
        }

    }

}
