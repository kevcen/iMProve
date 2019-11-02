package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import other.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LearnUpdateController {
    private RootController rootController;
    private LearnController learnController;
    private AnchorPane apnLearn;
    private DAO dao = new DAO();

    //map to refer to the respective controllers when needed
    private ObservableMap<AnchorPane, LearnUpdateRowController> controllerMap = FXCollections.observableHashMap();

    @FXML
    private Text txtNewTitle;
    @FXML
    private VBox vbxCards;
    @FXML
    private ScrollPane scrpnCards;

    public void initData(String title, RootController rootController, LearnController learnController, AnchorPane apnLearn) {
        //initialise data
        txtNewTitle.setText(title);
        this.rootController = rootController;
        this.learnController = learnController;
        this.apnLearn = apnLearn;

        scrpnCards.vvalueProperty().bind(vbxCards.heightProperty()); //automatically scroll the scrollpane to the bottom

        //use title to load all existing cards
        try (
                Connection conn = dao.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT Term, Definition FROM Theory WHERE Title = ? AND Grade <= ?");
        ) {
            //initialise the existing flashcard rows
            stmt.setString(1, title);
            stmt.setInt(2, RootController.getGrade());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String term = rs.getString("Term");
                String definition = rs.getString("Definition");
                addRow(term, definition);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ObservableMap<AnchorPane, LearnUpdateRowController> getControllerMap() {
        return controllerMap;
    }

    public LearnUpdateRowController addRow(String term, String def) {
        LearnUpdateRowController controller = addRow();
        Platform.runLater(() -> {
            controller.setTerm(term);
            controller.setDef(def);
        });
        return controller;
    }

    public LearnUpdateRowController addRow() {
        AnchorPane row = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/learn_update_row.fxml"));
        try {
            row = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LearnUpdateRowController controller = loader.getController();
        controller.initData(this);
        controllerMap.put(row, controller);
        vbxCards.getChildren().add(row);
        return controller;
    }

    //add an extra row when add flashcard button clicked
    @FXML
    public void addFlashcard(ActionEvent e) {
        addRow();
    }

    //finish when finish button clicked
    @FXML
    public void finish(ActionEvent e) {
        doFinish();
    }

    //finish when enter button clicked
    @FXML
    public void kFinish(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER)
            doFinish();
    }

    public void doFinish() {
        try (
                Connection conn = dao.getConnection();
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Theory WHERE Title = ? AND Grade <= ?");
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Theory (Term, Definition, Grade, Title) VALUES (?, ?, ?, ?)");
        ) {
            //delete old terms
            conn.setAutoCommit(false); //doesn't unpredictably commit the changes

            deleteStmt.setString(1, txtNewTitle.getText());
            deleteStmt.setInt(2, RootController.getGrade());
            deleteStmt.executeUpdate();

            //add updated terms/new terms
            boolean termsAdded = false;
            for (LearnUpdateRowController c : controllerMap.values()) {
                if (!(c.getTerm().equals("") || c.getDef().equals(""))) { //not blank space
                    termsAdded = true;
                    stmt.setString(1, c.getTerm());
                    stmt.setString(2, c.getDef());
                    stmt.setInt(3, RootController.getGrade());
                    stmt.setString(4, txtNewTitle.getText());
                    stmt.addBatch();
                }
            }
            if (termsAdded) //if terms WERE added, then execute batch otherwise will cause error on adding nothing
                stmt.executeBatch();

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        //go back to main learn screen
        rootController.setActivity(apnLearn);
        learnController.initSets();
        apnLearn.requestFocus();
    }
}
