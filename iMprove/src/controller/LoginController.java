package controller;

import java.io.IOException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import other.Main;

public class LoginController {
	//the UI objects
	@FXML private JFXTextField fldName;
	@FXML private Label lblWelcome;
	@FXML private AnchorPane apnLoginRoot;
	@FXML private JFXButton btnLogin;
	
	public void initialize() {
		//bind fldName
		fldName.textProperty().addListener((o, ov, nv) -> {
			//only show the login button if there is text inside (self validates the input) and also change the display text
			if(nv.isEmpty()) {
				btnLogin.setVisible(false);
				lblWelcome.setText("Welcome.");
			} else {
				btnLogin.setVisible(true);
				lblWelcome.setText("Welcome, "+nv+".");
			}
		});
		
		//bind enter key to accept login
		apnLoginRoot.setOnKeyPressed(e -> {
			if(e.getCode()==KeyCode.ENTER) {
				doLogin();
			}
		});
		
	}
	@FXML public void login(ActionEvent ae) { //clicked by 'login'
		doLogin();
	}
	private void doLogin() {
		if(!fldName.getText().isEmpty()) { //if there is text
			//load the grade selection screen
			AnchorPane root = null;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grade_select.fxml"));
			try {
				root = loader.load();
			} catch(IOException e) {e.printStackTrace();}
			GradeController controller = loader.getController();
			controller.setName(fldName.getText());
			
			Scene gradeScene = new Scene(root);
			gradeScene.setFill(null);
			Main.getStage().setScene(gradeScene);
		}
		
	}
}
