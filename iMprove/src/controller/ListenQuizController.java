package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import model.AuralTest;
import other.DAO;

public class ListenQuizController {
	//important instance variables
	private RootController rootController;
	private DAO dao = new DAO();
	private AuralTest currentTest = null;
	private boolean answered = false;
	private boolean finished = false;
	private boolean fading = false;
	//UI objects
	@FXML private JFXButton btnRelMaj, btnRelMin, btnSub, btnDom, btnAgain;
	@FXML private MaterialDesignIconView icnPlayPause;
	@FXML private JFXSlider sldSeek;
	@FXML private AnchorPane apnFirstPlay, apnQuiz;
	
	public void initData(RootController rootController) {
		this.rootController = rootController;

		//style the slider dependent on the theme
		switch(RootController.getTheme()) {
			case "Default Chamber": sldSeek.getStylesheets().add("resources/styles/default-slider.css");
				break;
			case "Future Sea": sldSeek.getStylesheets().add("resources/styles/future-slider.css");
				break;
			case "Rose Petal": sldSeek.getStylesheets().add("resources/styles/rose-slider.css");
				break;
			case "The Panda Special": sldSeek.getStylesheets().add("resources/styles/panda-slider.css");
				break;
				
		}
		
		getAuralTest();
		syncAudio();
	}
	//get a random aural test to be used
	private void getAuralTest() {
		try (
			Connection conn = dao.getConnection();
			PreparedStatement cntStmt = conn.prepareStatement("SELECT COUNT(*) FROM Aural");
			ResultSet cntRs = cntStmt.executeQuery();
			PreparedStatement rndStmt = conn.prepareStatement("SELECT File, Modulation FROM Aural");
			ResultSet rndRs = rndStmt.executeQuery();
		) {
			int length = 0; //the upper bound for random row
			if(cntRs.next())length = cntRs.getInt(1); 
			int randomRow = (new Random()).nextInt(length); //floor for lower bound to generate 'length' number of integers (inc 0, exc end)
			
			for(int i = 0; i<=randomRow; i++)rndRs.next();//go to random row <= to move past zeroth row
			
			currentTest = new AuralTest(rndRs.getString("File"), rndRs.getString("Modulation"));
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	//sync the slider with the audio's position in time
	private void syncAudio() {
		     
		currentTest.getMediaPlayer().currentTimeProperty().addListener(new InvalidationListener() { //to auto sync the slider with the media player being played
			@Override
			public void invalidated(Observable o) {
				Duration currentDuration = currentTest.getMediaPlayer().getCurrentTime();
				Duration totalDuration = Duration.millis(currentTest.getDuration());
				sldSeek.setDisable(totalDuration.isUnknown()); //disable slider if the music has not been correctly loaded yet
				if(!sldSeek.isDisable() && totalDuration.greaterThan(Duration.ZERO)&& !sldSeek.isValueChanging()) { 
					double percentageIn = (currentDuration.toMillis()/totalDuration.toMillis()) * 100;
					sldSeek.setValue(percentageIn);
					if(!answered)sldSeek.setDisable(true); //disable
				}
			}
		});
		
		//pause when finished so it doesn't automatically play when slider is moved back
		currentTest.getMediaPlayer().setOnEndOfMedia(()-> {
			finished = true;
			currentTest.getMediaPlayer().pause();
			icnPlayPause.setGlyphName("PLAY");
		});
		
	}
	//automatically stops after first play
	public void bindAutoStop() {
		AnchorPane apnActivity = (AnchorPane) apnQuiz.getParent();
		ObservableList<Node> childrenList = apnActivity.getChildren();
		childrenList.addListener((Change<? extends Node> c) -> {
			while(c.next()) {
				if(c.wasRemoved()) {
					for(Node child: c.getRemoved()) {
						if(child.equals(apnQuiz)) //quiz removed
							currentTest.getMediaPlayer().stop(); //stop the music
					}
				}
			}
		});
	}
	//allow user to seek through the audio when answered question
	public void allowSeeking() {
		sldSeek.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable o) {
				if(sldSeek.isValueChanging()) { //if the slider is truly moving
					Duration newDuration = Duration.millis(currentTest.getDuration()).multiply(sldSeek.getValue()/100.0);
					currentTest.getMediaPlayer().seek(newDuration);
					if(sldSeek.getValue()>=98) {
						finished = true;
						currentTest.getMediaPlayer().pause();
						icnPlayPause.setGlyphName("PLAY");
					} else {
						finished = false;
					}
				}
			}
		});
		
		//change UI in FX application thread
		Platform.runLater(()->{
			sldSeek.setDisable(false);
			//slider now includes its thumb to click on
			sldSeek.getStylesheets().add("resources/styles/slider-enable.css");
			icnPlayPause.setVisible(true);
		});
	}
	
	//play or pause the music
	@FXML
	public void playPause(MouseEvent e) {
		if(icnPlayPause.getGlyphName().equals("PLAY")) {
			if(finished) {
				currentTest.getMediaPlayer().seek(currentTest.getMediaPlayer().getStartTime());
				finished = false;
			}
			currentTest.getMediaPlayer().play();
			icnPlayPause.setGlyphName("PAUSE");
		} else {
			currentTest.getMediaPlayer().pause();
			icnPlayPause.setGlyphName("PLAY");
		}
		
	}
	//first play clicked
	@FXML
	public void firstPlay(ActionEvent e) {
		bindAutoStop();
		//fade
		if(!fading) {
			fading = true;
			FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), apnFirstPlay);
			fadeOut.setFromValue(1.0);
			fadeOut.setToValue(0.0);
			fadeOut.play();
			Timeline invisible = new Timeline(new KeyFrame(Duration.seconds(0)), new KeyFrame(Duration.seconds(1), event ->{
				apnFirstPlay.setVisible(false);
				apnQuiz.requestFocus();
			}));
			invisible.play();
			currentTest.getMediaPlayer().play();
		}
	}
	//when answered question
	@FXML
	public void answerQuestion(ActionEvent e) {
		if(!answered) { //if hasn't been answered yet - this will make clicks to other buttons not change their colour
			answered =  true;
			boolean pandaTheme = RootController.getTheme().equals("The Panda Special");
			//make input red
			Button inputBtn = (Button) e.getSource();
			inputBtn.setStyle("-fx-background-color: #6f0f0f; -fx-text-fill: white;" +(pandaTheme? "-fx-background-image: null;": ""));
			
			//make correct answer green - correct input will be overrided green also       
			switch(currentTest.getModulation()) {
				case "Relative Major": btnRelMaj.setStyle("-fx-background-color: #0cb794; -fx-text-fill: white;"+(pandaTheme? "-fx-background-image: null;": ""));
					break;
				case "Relative Minor": btnRelMin.setStyle("-fx-background-color: #0cb794; -fx-text-fill: white;"+(pandaTheme? "-fx-background-image: null;": ""));
					break;
				case "Dominant": btnDom.setStyle("-fx-background-color: #0cb794; -fx-text-fill: white;"+(pandaTheme? "-fx-background-image: null;": ""));
					break;
				case "Subdominant": btnSub.setStyle("-fx-background-color: #0cb794; -fx-text-fill: white;"+(pandaTheme? "-fx-background-image: null;": ""));
					break;
			}
			btnAgain.setVisible(true);//show play again
			allowSeeking(); //let the reflection and correction begin
		}
	}
	
	//when user clicks 'play again'
	@FXML
	public void playAgain(ActionEvent e) {
		currentTest.getMediaPlayer().stop(); //stop the music if it's still playing
		
		//load again the quiz page
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/listen_quiz.fxml"));
		try {
			root = loader.load();
		} catch(IOException ex) {ex.printStackTrace();}
		ListenQuizController controller = loader.getController();
		controller.initData(rootController);
		rootController.setActivity(root);
		
		root.requestFocus(); //so doesn't focus on a button
	}
	
}
