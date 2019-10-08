package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import com.jfoenix.controls.JFXButton;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class RootController {
	//UI objects
	@FXML private Label lblName;
	@FXML private AnchorPane apnRoot, apnActivity;
	@FXML private StackPane spnLearn, spnTest, spnListen, spnReview;
	@FXML private MaterialDesignIconView icnLearn, icnTest, icnListen, icnReview;
	@FXML private JFXButton btnLearn, btnTest, btnListen, btnReview;
	@FXML private Text txtTime, txtWelcome;
	@FXML private VBox vbxActivities;
	//important data to be stored
	private static int grade;
	private static String name;
	private static String theme;
	private boolean greeting;
	//loaded by java fx application thread so the ui objects have been loaded and we can change its properties
	public void initialize() {
		Platform.runLater(()->{
			//get properties
			Properties settingsProps = getProperties();
			String storedTheme = settingsProps.getProperty("theme", "Default Chamber");
			greeting = "true".equals(settingsProps.getProperty("greeting", "true"));
			//set theme
			setCSS(storedTheme);
			
			//set greeting
			if(greeting) {
				int hour = Integer.parseInt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH")));
				String greeting;
				if(hour>=5 & hour<12)
					greeting = "Good Morning,";
				else if(hour>=12 & hour<18)
					greeting = "Good Afternoon,";
				else if(hour>=18 & hour<23)
					greeting = "Good Evening,";
				else
					greeting = "Good Night,";
				txtWelcome.setText(greeting);
			}
			
			//take off listen section if not grade 8
			if(grade!=8) {
				removeListen();
			}
			apnRoot.requestFocus();
			//automatically update time
		    startTime();
		});
	}
	//methods to display/remove the listen activity
	public void removeListen() {
		vbxActivities.getChildren().remove(spnListen);
	}
	public void addListen() {
		if(!vbxActivities.getChildren().contains(spnListen))
			vbxActivities.getChildren().add(spnListen);
	}
	//get the settings
	public Properties getProperties() {
		final String path = "C:\\iMProve\\settings.properties";
		Properties settingsProps = new Properties();
		
		try {
			File propertiesFile = new File(path);
			propertiesFile.createNewFile(); //will create new file if does not exist, if exists then no-op
			
			settingsProps.load(new FileInputStream(propertiesFile));
		} catch(IOException ex) {ex.printStackTrace();}
		
		return settingsProps;
	}
	//set the theme (css styling of the system)
	private void setCSS(String theme) {
		RootController.theme = theme;
		String fileName = "resources/themes/";
		switch(theme) {
			case "Future Sea": fileName += "future_sea.css";
				break;
			case "Default Chamber": fileName += "default_chamber.css";
				break;
			case "Rose Petal": fileName += "rose_petal.css";
				break;
			case "The Panda Special": fileName += "panda_special.css";
		}
		apnRoot.getScene().getStylesheets().add(fileName);
	}
	//initial data set by GradeController
	public void initData(String name, int grade) {
		Platform.runLater(()-> {
			lblName.setText(name);
			txtWelcome.setText(txtWelcome.getText() + (greeting ? " " + name + "." : ""));
		});
		RootController.grade = grade;
		RootController.name = name;
	}
	//getter and setter methods for the instance variables
	public static int getGrade() {
		return grade;
	}
	public static String getName() {
		return name;
	}
	public static String getTheme() {
		return theme;
	}
	public static void setGrade(int grade) {
		RootController.grade = grade;
	}
	public void setName(String name) {
		RootController.name = name;
		lblName.setText(name);
	}
	public static void setTheme(String theme) {
		RootController.theme = theme;
	}
	//change the activity displayed on the UI
	public void setActivity(Node root) {
		apnActivity.getChildren().clear();
		apnActivity.getChildren().add(root);
	}
	//change the colours of the navigation bar
	public void changeNavigationColours(StackPane spn, MaterialDesignIconView icn, JFXButton btn) {
		for(StackPane pane: Arrays.asList(spnLearn, spnTest, spnListen, spnReview)) {
			if(!pane.equals(spn))
				pane.setStyle("-fx-background-color: " + getColourCode(0));
			else
				pane.setStyle("-fx-background-color: " + ((theme.equals("Default Chamber") | theme.equals("Future Sea")) ? getColourCode(1): "transparent"));
		}
		for(MaterialDesignIconView icon: Arrays.asList(icnLearn, icnTest, icnListen, icnReview)) {
			if(!icon.equals(icn)) {
				icon.setStyle("-fx-fill: " + getColourCode(4));
			} else {
				icon.setStyle("-fx-fill: " + getColourCode(3));
			}
		}
		for(JFXButton button: Arrays.asList(btnLearn, btnTest, btnListen, btnReview)) {
			if(!button.equals(btn)) 
				button.setStyle("-fx-text-fill: " + ((theme.equals("Default Chamber") | theme.equals("Future Sea")) ? getColourCode(1): "")); //doesnt change text colour if not default or crystal
			else
				button.setStyle("-fx-text-fill: " + getColourCode(2));
		}
	}
	//a method providing the HEX colour codes to make the code more reusable
	public static String getColourCode(int option) {
		String colourCode = "";
		switch(theme) {
			case "Default Chamber": 
				switch(option) {
					case 0: colourCode = "#001421"; //unselected background
						break;
					case 1: colourCode = "#c18303"; //selected background/ unselected text
						break;
					case 2: colourCode = "#ffffff"; //selected text
						break;
					case 3: colourCode = "#daa34b"; //selected icon
						break;
					case 4: colourCode = "#262b1a"; //unselected icon 
						break;
				}
				break;
			case "Future Sea": 
				switch(option) {
					case 0: colourCode = "#041e23";
						break;
					case 1: colourCode = "#5bc6b4";
						break;
					case 2: colourCode = "#ffffff";
						break;
					case 3: colourCode = "#77d6c9";
						break;
					case 4: colourCode = "#1f3a3d";
						break;
				}
				break;
			case "Rose Petal":
				switch(option) {
					case 0: colourCode = "transparent";
						break;
					case 1: colourCode = "#ce71c2";
						break;
					//dont need to change selected activities
				}
				break;
			case "The Panda Special": 
				switch(option) {
					case 0: colourCode = "transparent";
						break;
					case 1: colourCode = "#3d3d3d";
						break;
					//as above
				}
				break;
		}
		return colourCode;
	}
	//start the timer which automatically updates every second for more accuracy
	public void startTime() {
		Timeline dateUpdater = new Timeline(
				new KeyFrame(Duration.seconds(0), event -> txtTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")))),
				new KeyFrame(Duration.seconds(1))); //duration 0 to do update time straight away and then seconds 1 to wait
		dateUpdater.setCycleCount(Animation.INDEFINITE);
		dateUpdater.play();
	}
	
	//display the respective activity
	@FXML
	public void goLearn(ActionEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/learn.fxml"));
		try {
			root = loader.load();
		} catch(IOException ioe) {ioe.printStackTrace();}
		LearnController controller = loader.getController();
		controller.setRootController(this);
		Platform.runLater(()->changeNavigationColours(spnLearn, icnLearn, btnLearn));
		setActivity(root);
	}
	@FXML
	public void goTest(ActionEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test.fxml"));
		try {
			root = loader.load();
		} catch(IOException ioe) {ioe.printStackTrace();}
		TestController controller = loader.getController();
		controller.setRootController(this);
		changeNavigationColours(spnTest, icnTest, btnTest);
		setActivity(root);
	}
	@FXML
	public void goListen(ActionEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/listen.fxml"));
		try {
			root = loader.load();
		} catch(IOException ioe) {ioe.printStackTrace();}
		ListenController controller = loader.getController();
		controller.setRootController(this);
		changeNavigationColours(spnListen, icnListen, btnListen);
		setActivity(root);
	}
	@FXML
	public void goReview(ActionEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/review.fxml"));
		try {
			root = loader.load();
		} catch(IOException ioe) {ioe.printStackTrace();}
		ReviewController controller = loader.getController();
		controller.setRootController(this);
		changeNavigationColours(spnReview, icnReview, btnReview);
		setActivity(root);
	}
	@FXML
	public void goSettings(MouseEvent e) {
		AnchorPane root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settings.fxml"));
		try {
			root = loader.load();
		} catch(IOException ex) {ex.printStackTrace();}
		SettingsController controller = loader.getController();
		controller.setRootController(this);
		changeNavigationColours(null, null, null); //null doesn't equal any of the objects of the activities so all will turn blue (unselected)
		setActivity(root);
	}
	
	//close the system
	@FXML
	public void close(MouseEvent e) {
		System.exit(0);
	}


}
