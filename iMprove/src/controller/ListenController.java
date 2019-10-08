package controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class ListenController {
	//RootController object for changing mutual data and current activity being displayed
	private RootController rootController;
	
	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}
	
	//show the quiz
	@FXML 
	public void playQuiz(ActionEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/listen_quiz.fxml"));
		try {
			root = loader.load();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		ListenQuizController controller = loader.getController();
		controller.initData(rootController);
		rootController.setActivity(root);
		
		root.requestFocus(); //doesn't highlight a random button
	}
}
