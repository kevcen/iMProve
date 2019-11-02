package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import model.Feedback;
import model.TheoryQuestion;
import other.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class TestQuizController {
    //important data variables/structures
    private RootController rootController;
    private DAO dao = new DAO();
    private Random rand = new Random();
    private int questionIndex = 0;
    private LinkedList<TheoryQuestion> theoryQuestionList = new LinkedList<>();
    private LinkedList<Feedback> feedbackList = new LinkedList<>();
    private ObservableList<String> correctTerms = FXCollections.observableArrayList();
    private boolean questionAnswered = false;
    //UI objects
    @FXML
    private Text txtQuestion;
    @FXML
    private JFXButton btnOk, btnOne, btnTwo, btnThree, btnFour;
    @FXML
    private JFXProgressBar prbProgress;
    @FXML
    private AnchorPane apnQuiz;
    private AnchorPane welcomePane;
    private boolean enoughTerms = false;

    //collection of the multiple choice answer buttons
    private JFXButton[] answerButtons;

    public boolean getEnoughTerms() {
        return enoughTerms;
    }

    public void initData(RootController rootController, AnchorPane welcomePane) {
        this.rootController = rootController;
        this.welcomePane = welcomePane;
        answerButtons = new JFXButton[]{btnOne, btnTwo, btnThree, btnFour};

        //collect questions from database
        getTheoryQuestions();
    }

    public void getTheoryQuestions() {
        try (
                Connection conn = dao.getConnection();
                PreparedStatement priorityCntStmt = conn.prepareStatement("SELECT COUNT(*) FROM Theory WHERE Grade <= ? AND K_Coefficient < 0");
                PreparedStatement priorityStmt = conn.prepareStatement("SELECT Term, Definition FROM Theory WHERE Grade <= ? AND K_Coefficient < 0 ORDER BY K_Coefficient ASC");
                PreparedStatement cntStmt = conn.prepareStatement("SELECT COUNT(*) FROM Theory WHERE Grade <= ?");
                PreparedStatement rndStmt = conn.prepareStatement("SELECT Term, Definition FROM Theory WHERE Grade <= ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ) {
            //execute queries
            cntStmt.setInt(1, RootController.getGrade());
            ResultSet cntRs = cntStmt.executeQuery();
            rndStmt.setInt(1, RootController.getGrade());
            ResultSet rndRs = rndStmt.executeQuery();
            priorityCntStmt.setInt(1, RootController.getGrade());
            ResultSet priorityCntRs = priorityCntStmt.executeQuery();
            priorityStmt.setInt(1, RootController.getGrade());
            ResultSet priorityRs = priorityStmt.executeQuery();

            //prioritise the low k_coefficient terms (highly answered wrong)
            int priorityLength = 0;
            if (priorityCntRs.next()) priorityLength = priorityCntRs.getInt(1); //number of high priority terms

            //get length to use for upper bound of random num for filled answers
            int length = 0;
            if (cntRs.next()) length = cntRs.getInt(1); //upperbound of random

            if (length < 4) { //if not enough terms
                Alert notEnoughTerms = new Alert(AlertType.ERROR, "hi", ButtonType.OK);
                notEnoughTerms.setHeaderText("There is not enough terms to take a test");
                notEnoughTerms.setContentText("Try resetting the system in settings or adding more flashcard sets in the Learn section.");
                notEnoughTerms.showAndWait();
            } else {
                enoughTerms = true;
                //get priority terms
                if (priorityLength > 0) {
                    for (int i = 0; i < (priorityLength >= 10 ? 10 : priorityLength); i++) { //if there are more than 10 priority terms, only take the top 10 highest priority (lowest k_coefficient), otherwise take as much as you can
                        String question = "";
                        String[] answers = new String[4];
                        int answerIndex = rand.nextInt(4);
                        if (priorityRs.next()) {
                            answers[answerIndex] = priorityRs.getString("Definition");
                            question = priorityRs.getString("Term");
                        }
                        int[] usedRows = new int[2]; //only need to store 2 since there is 1 from the actual answer and the last 1 is determined from the other 3
                        int u = 0; //index of the usedRows
                        //fill other answers
                        for (int j = 0; j < 4; j++) { //four answers (3 random 1 already selected high priority one)
                            if (j == answerIndex)
                                continue;
                            int randomRow;
                            String answer;
                            do {
                                randomRow = rand.nextInt(length);
                                for (int k = 0; k <= randomRow; k++) { //move pointer to the random row; <= because you want to call next() once to move past zeroth row
                                    rndRs.next();
                                }
                                answer = rndRs.getString("Definition"); //get the string representation of the answer
                                rndRs.beforeFirst(); //back to start of query result
                            } while (randomRow == usedRows[0] || randomRow == usedRows[1] || answer.equals(answers[answerIndex])); //keep looping until you get a unique answer
                            if (u < 2) usedRows[u++] = randomRow; //store randomRow variable for next answer check
                            answers[j] = answer;
                        }
                        //add question
                        theoryQuestionList.add(new TheoryQuestion(question, answers, answerIndex));
                    }
                }

                //get all the rest of the terms and select the randomly
                for (int questionNumber = 0; questionNumber < 10 - priorityLength; questionNumber++) { //create (ten - already filled) questions
                    int answerIndex = rand.nextInt(4);
                    String question = "";
                    String[] answers = new String[4];
                    //get four random answers + one matching foreign term (question)

                    int[] usedRows = new int[2]; //only need to store 2 since there is 1 from the actual answer and the last 1 is determined from the other 3
                    int u = 0; //index of the usedRows

                    for (int i = 0; i < 4; i++) { //four random answers

                        if (i == 0) { //get the correct answer
                            int randomRow = rand.nextInt(length);
                            for (int j = 0; j <= randomRow; j++) { //move pointer to the random row; <= because you want to call next() at least once to go past zeroth row
                                rndRs.next();
                            }
                            answers[answerIndex] = rndRs.getString("Definition");
                            question = rndRs.getString("Term");
                            rndRs.beforeFirst(); //reset pointer for next random answer
                        } else {
                            int randomRow;
                            String answer;
                            do {
                                randomRow = rand.nextInt(length);
                                System.err.println("randomRow: " + randomRow);
                                for (int j = 0; j <= randomRow; j++) {
                                    rndRs.next();
                                }
                                answer = rndRs.getString("Definition");
                                System.err.println("answer: " + answer);
                                rndRs.beforeFirst();
                            } while (randomRow == usedRows[0] || randomRow == usedRows[1] || answer.equals(answers[answerIndex])); //until unique answer
                            System.err.println(Arrays.toString(usedRows) + " " + randomRow);
                            if (u < 2) usedRows[u++] = randomRow; //store randomRow variable for next answer check
                            if (i != answerIndex) answers[i] = answer;
                            else
                                answers[0] = answer; //since we used i = 0 for the correct answer, we do the opposite for the i=0 answer

                            System.err.println(Arrays.toString(answers));
                        }
                    }
                    //add question
                    theoryQuestionList.add(new TheoryQuestion(question, answers, answerIndex));
                }

                Platform.runLater(() -> loadQuestion());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void loadQuestion() {
        TheoryQuestion theoryQuestion = theoryQuestionList.get(questionIndex);

        //question
        txtQuestion.setText(questionIndex % 2 == 0 ?
                "What is the definition of " + theoryQuestion.getQuestion() + "?" :
                "What does " + theoryQuestion.getQuestion() + " mean?");

        //answers
        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(theoryQuestion.getAnswer(i));
        }

        apnQuiz.requestFocus(); //so it doesn't highlight a random button
    }

    public void answerQuestion(int answer) {
        TheoryQuestion currentQuestion = theoryQuestionList.get(questionIndex);
        int correctAnswer = currentQuestion.getAnswerIndex();
        boolean pandaTheme = RootController.getTheme().equals("The Panda Special");
        //change colours used for feedback
        answerButtons[answer].setStyle("-fx-background-color: #6f0f0f; -fx-text-fill: white;" + (pandaTheme ? "-fx-background-image: null" : "")); //answered button is red
        answerButtons[correctAnswer].setStyle("-fx-background-color: #0cb794; -fx-text-fill: white;" + (pandaTheme ? "-fx-background-image: null" : "")); //if your answer was right, it will override the colour to green


        //if wrong, note down for feedback
        if (answer == correctAnswer)
            correctTerms.add(currentQuestion.getQuestion());
        else
            feedbackList.add(new Feedback(currentQuestion.getQuestion(), currentQuestion.getAnswer(answer), currentQuestion.getAnswer(correctAnswer)));
        //button to move on
        btnOk.setVisible(true);
    }

    //when user clicks on an answer
    @FXML
    public void giveAnswer(ActionEvent e) {
        if (questionAnswered == false) {
            questionAnswered = true;
            JFXButton clickedButton = (JFXButton) e.getSource();

            for (int i = 0; i < 4; i++) { //check which button is the answered button and then answer the question with its index
                if (clickedButton.equals(answerButtons[i]))
                    answerQuestion(i);
            }
        }
    }

    //when OK button is clicked
    @FXML
    public void nextQuestion(ActionEvent e) {
        if (++questionIndex == 10) {
            loadResults();
        } else {
            questionAnswered = false;
            prbProgress.setProgress((questionIndex) / 10.0); //increase progress
            btnOk.setVisible(false);//hide ok

            //make all answer boxes white again with orange text
            for (JFXButton btn : answerButtons) {
                String addPanda = RootController.getTheme().equals("The Panda Special") ? "; -fx-background-image: url('resources/img/panda-background1.png');" : "";
                btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill:" + RootController.getColourCode(1) + addPanda);
            }

            loadQuestion(); //load new question with incremented questionIndex


        }
    }

    //load the results
    public void loadResults() {
        AnchorPane root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test_results.fxml"));
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        TestResultsController controller = loader.getController();
        controller.initData(rootController, feedbackList, 10 - feedbackList.size(), correctTerms);
        rootController.setActivity(root);

        root.requestFocus(); //so doesn't randomly highlight a button
    }
}
