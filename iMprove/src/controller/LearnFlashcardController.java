package controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import model.Flashcard;
import other.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LearnFlashcardController {
    private ObservableList<Flashcard> flashcardList = FXCollections.observableArrayList();
    private DAO dao = new DAO();
    private int flashcardIndex = 0; //will start at first card
    private boolean questionVisible = true;
    private AnchorPane apnLearn;
    private RootController rootController;
    @FXML
    private Text txtSetTitle, txtTerm, txtDef;
    @FXML
    private JFXButton btnLeft, btnRight;
    @FXML
    private AnchorPane apnFlashcards;

    public void initData(String title, RootController rootController, AnchorPane apnLearn) {
        //set values
        this.apnLearn = apnLearn;
        this.rootController = rootController;

        //get the cards relating to that title
        try (
                Connection conn = dao.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT Term, Definition FROM Theory Where Title = ? AND Grade <= ?");
        ) {
            stmt.setString(1, title);
            stmt.setInt(2, RootController.getGrade());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String foreignTerm = rs.getString("Term");
                String definition = rs.getString("Definition");
                flashcardList.add(new Flashcard(foreignTerm, definition));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            txtSetTitle.setText(title);
            loadFlashcard();
        });
    }

    public void initKeys() { //arrow key input to use flashcards
        apnFlashcards.setOnKeyPressed(new EventHandler<KeyEvent>() { //random variable to refer to the scene
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                        flip();
                        break;
                    case DOWN:
                        flip();
                        break;
                    case LEFT:
                        prev();
                        break;
                    case RIGHT:
                        next();
                        break;
                    case SPACE:
                        flip();
                        break;
                }
                event.consume();
            }
        });
    }

    public void loadFlashcard() {
        //load the flashcard with the new flashcardIndex
        Flashcard currentCard = flashcardList.get(flashcardIndex);
        txtTerm.setText(currentCard.getTerm());
        txtDef.setText(currentCard.getDefinition());

        //show foreign term first
        if (!questionVisible) {
            txtTerm.setVisible(true);
            txtDef.setVisible(false);
            questionVisible = true; //turn true
        }

        apnFlashcards.requestFocus();
    }

    public void flip() {
        txtTerm.setVisible(!questionVisible); //the term visibility will always become the opposite of what it was before
        txtDef.setVisible(questionVisible); //vice versa for the definition
        questionVisible = !questionVisible;
    }

    public void next() {
        flashcardIndex = (flashcardIndex + 1) % flashcardList.size(); //loop around the list like a circular queue
        loadFlashcard();
    }

    public void prev() {
        flashcardIndex--;
        if (flashcardIndex < 0) flashcardIndex = flashcardList.size() - 1; //go to last part of list if it goes below 0
        loadFlashcard();
    }

    //flip card
    @FXML
    public void flipCard(MouseEvent e) {
        flip();
    }

    //next/prev card
    @FXML
    public void moveCard(ActionEvent e) {
        JFXButton clickedButton = (JFXButton) e.getSource();
        if (clickedButton.equals(btnLeft))
            prev();
        if (clickedButton.equals(btnRight))
            next();
    }

    @FXML
    public void goBack(MouseEvent e) {
        rootController.setActivity(apnLearn);
        apnLearn.requestFocus();
    }

}
