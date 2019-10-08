package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import model.Feedback;
import other.DAO;

public class TestResultsController {
	//array of different comments, the index correlates the the score
	private String[] comments = new String[] {
			"Try learning the terms first!", 
			"Have a peak at the learn section.", 
			"Remember, there is the learn section just for you!", 
			"It's fine, just learn from it!", 
			"Practice makes perfect", 
			"You can do better next time!", 
			"Don't be shy to keep going!", 
			"Not bad!", 
			"Keep going!", 
			"Almost there!", 
			"Wow, you did it!"
	};
	//other important variables
	private LinkedList<Feedback> feedbackList;
	private ObservableList<String> correctTerms;
	private RootController rootController;
	private int result;
	private DAO dao = new DAO();
	//ui objects
	@FXML private Text txtScore, txtComment;
	@FXML private VBox vbxFeedbacks;
	public void initData(RootController rc, LinkedList<Feedback> fbl, int result, ObservableList<String> ct) {
		rootController = rc; //set rootcontroller for activity changes
		feedbackList = fbl; //list of feedback
		this.result = result; //take the result of the test
		txtScore.setText("You scored " + result + "/10!");
		txtComment.setText(comments[result]); //a comment depending on the result
		correctTerms = ct;
		
		//add the feedback to the vbox
		for(Feedback feedback: feedbackList) {
			Text txtFeedback = new Text();
			txtFeedback.setFont(Font.font("Century Gothic", FontPosture.REGULAR, 16));
			txtFeedback.setText("For " + feedback.getQuestion() + ", it's not "+ feedback.getWrong() + " but it is " + feedback.getCorrect()+".\n");
			vbxFeedbacks.getChildren().add(txtFeedback);
		}
		
		// store into sql
		updateDB();
	}
	public void updateDB() {
		try(
			Connection conn = dao.getConnection();
			PreparedStatement checkKStmt = conn.prepareStatement("SELECT K_Coefficient FROM Theory Where Term = ?"); //check the current k coefficient
			PreparedStatement higherKStmt = conn.prepareStatement("UPDATE Theory SET K_Coefficient = K_Coefficient + 1 WHERE Term = ?"); //for correct answers
			PreparedStatement lowerKStmt = conn.prepareStatement("UPDATE Theory SET K_Coefficient = K_Coefficient - 1 WHERE Term = ?"); //for wrong answers
				
			//record score for review section
			PreparedStatement oldScoreStmt = conn.prepareStatement("SELECT Highscore FROM Score WHERE ScoreDay = ? AND ScoreMonth = ? AND ScoreYear = ?");
			PreparedStatement insScoreStmt = conn.prepareStatement("INSERT INTO Score (ScoreDay, ScoreMonth, ScoreYear, Highscore) VALUES (?, ?, ?, ?)");
			PreparedStatement updScoreStmt = conn.prepareStatement("UPDATE Score SET Highscore = ? WHERE ScoreDay = ? AND ScoreMonth = ? AND ScoreYear = ?");
		) {
			conn.setAutoCommit(false);
			//changes to K_Coefficient to adapt for next test
			for(String term: correctTerms) { 
				checkKStmt.setString(1, term);
				ResultSet checkRs = checkKStmt.executeQuery();
				int K_Coefficient = 0;
				if(checkRs.next())K_Coefficient = checkRs.getInt("K_Coefficient");
				
				if(K_Coefficient < 1) {
					higherKStmt.setString(1, term);
					higherKStmt.addBatch();
				}
			}
			if(correctTerms.size()>0)
				higherKStmt.executeBatch(); //larger than zero otherwise batch update won't work
			
			
			for(Feedback wrongTerms: feedbackList) { //all terms answered wrongly will be noted for next term (by lowering the K_Coefficient)
				lowerKStmt.setString(1, wrongTerms.getQuestion());
				lowerKStmt.addBatch();
			}
			if(feedbackList.size()>0)lowerKStmt.executeBatch();
			
			
			//add highscore into db
			//check old score if it has improved
			int[] date = getDate();
			oldScoreStmt.setInt(1, date[0]);
			oldScoreStmt.setInt(2, date[1]);
			oldScoreStmt.setInt(3, date[2]);
			ResultSet oldScoreRs = oldScoreStmt.executeQuery();
			int hiscore = -1;
			if(oldScoreRs.next())hiscore = oldScoreRs.getInt("Highscore"); //if a score exists for this date, take it for current the hiscore
			if(hiscore==-1) { //score doesn't already exist
				insScoreStmt.setInt(1, date[0]);
				insScoreStmt.setInt(2, date[1]);
				insScoreStmt.setInt(3, date[2]);
				insScoreStmt.setInt(4, result);
				insScoreStmt.executeUpdate();
			} else if(result > hiscore) { //if score exists and actually has improved
				updScoreStmt.setInt(1, result);
				updScoreStmt.setInt(2, date[0]);
				updScoreStmt.setInt(3, date[1]);
				updScoreStmt.setInt(4, date[2]);
				updScoreStmt.executeUpdate();
			}
			
			//commit the changes to the database
			conn.commit();
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	//a method to make it easier to access the current date
	public int[] getDate() {
		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DAY_OF_MONTH);
		int month = now.get(Calendar.MONTH) + 1; //zero indexed, +1 to make january 1, feb 2, ...
		int year = now.get(Calendar.YEAR);
		
		return new int[] {day, month, year};
	}
	@FXML public void playAgain(ActionEvent e) {
		//go back to starting screen
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test.fxml"));
		try {
			root = loader.load();
		} catch(IOException ioe) {ioe.printStackTrace();}
		TestController controller = loader.getController();
		controller.setRootController(rootController);
		rootController.setActivity(root);
		
		root.requestFocus(); //so doesn't randomly highlight a button
	}
}
