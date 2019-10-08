package controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import other.DAO;

public class SettingsController {
	//ui objects
	@FXML private JFXComboBox<String> cbxTheme;
	@FXML private JFXComboBox<Integer> cbxGrade;
	@FXML private JFXTextField fldName;
	@FXML private JFXToggleButton tglGreeting;
	@FXML private AnchorPane apnSettings;
	//instance variables
	private RootController rootController;
	private File propertiesFile;
	private Properties settingsProps = new Properties();
	private DAO dao = new DAO();
	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}
	//fx application threads
	public void initialize() {
		
		Platform.runLater(() -> {
			//initialise the comboboxes
			cbxTheme.getItems().addAll("Default Chamber", "Future Sea", "Rose Petal");
			cbxGrade.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8);
			
			//set initial values
			fldName.setText(RootController.getName());
			cbxGrade.setValue(RootController.getGrade());
			loadValues();
			
			apnSettings.requestFocus(); //no highlighted control
		});
		
		fldName.textProperty().addListener((o, ov, nv) -> {
			rootController.setName(nv);
		});
		
		
	}
	//load initial values from settings file (properties)
	public void loadValues() {
		final String path = "C:\\iMProve\\settings.properties";
		//create file
		try {
			propertiesFile = new File(path);
			propertiesFile.createNewFile(); //if file already exists, does nothing 
		} catch(IOException ex) {ex.printStackTrace();}
		//properties
		try (InputStream in = Files.newInputStream(propertiesFile.toPath())) {
			settingsProps.load(in);
			
			//get the properties
			String theme = settingsProps.getProperty("theme", "Default Chamber");
			boolean greeting = "true".equals(settingsProps.getProperty("greeting", "true")); //if true is what is stored, return true; if stored value not equal to true, return false
			boolean panda = "true".equals(settingsProps.getProperty("panda unlocked", "false"));
			
			//set control values
			if(panda)cbxTheme.getItems().add("The Panda Special");
			cbxTheme.setValue(theme);
			tglGreeting.setSelected(greeting);
		} catch(IOException ex) {
			ex.printStackTrace();
		} 
	}
	
	@FXML
	public void changeTheme(ActionEvent ae) {
		//code to change theme
		setTheme(cbxTheme.getValue());
	}
	//panda theme easter egg button clicked
	@FXML
	public void pandaTheme(MouseEvent me) {
		settingsProps.setProperty("panda unlocked", "true");
		if(!cbxTheme.getItems().contains("The Panda Special"))cbxTheme.getItems().add("The Panda Special");
		setTheme("The Panda Special");
	}
	//change the ui for the theme
	public void setTheme(String theme) {
		cbxTheme.setValue(theme); //for panda theme
		//change CSS
		Scene rootScene = apnSettings.getScene();
		rootScene.getStylesheets().clear();
		rootScene.getStylesheets().add(getCSS(theme));
		//change properties
		settingsProps.setProperty("theme", theme);
		saveProperties("theme changed");
		//change RootController properties and reset the colours of the navigation bar
		RootController.setTheme(theme);
		rootController.changeNavigationColours(null, null, null);
	}
	
	
	@FXML
	public void changeGrade(ActionEvent ae) {
		//code to change grade
		RootController.setGrade(cbxGrade.getValue());
		//remove listen if lower than 8
		if(cbxGrade.getValue()<8) {
			rootController.removeListen();
		} else {
			rootController.addListen();
		}
	}
	@FXML
	public void changeGreeting(ActionEvent ae) {
		//update properties for next time welcome
		settingsProps.setProperty("greeting", String.valueOf(tglGreeting.isSelected()));
		saveProperties("greeting toggled");
	}
	//reset all the data in the system
	@FXML
	public void resetData(ActionEvent ae) {
		Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
		confirmAlert.setHeaderText("Are you sure you want to reset your data?");
		Optional<ButtonType> result = confirmAlert.showAndWait();
		if(result.isPresent() && result.get() == ButtonType.OK) {
			DAO.closeConnections();
			dao.reset(); //reset database
			settingsProps.clear(); //reset settings
			saveProperties("reset");
			
			
			cbxTheme.getItems().remove("The Panda Special");
			tglGreeting.setSelected(true);
			setTheme("Default Chamber");
			cbxTheme.setValue("Default Chamber");
		}	
	}
	//get css file name of the theme
	private String getCSS(String theme) {
		String fileName = "resources/themes/";
		switch(theme) {
			case "Future Sea": fileName += "future_sea.css";
				break;
			case "Default Chamber": fileName += "default_chamber.css";
				break;
			case "Rose Petal": fileName += "rose_petal.css";
				break;
			case "The Panda Special": fileName += "panda_special.css";
				break;
		}
		return fileName;
	}
	//permanently make the changes to the properties
	private void saveProperties(String comment) {
		try (OutputStream out = Files.newOutputStream(propertiesFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING)){
			settingsProps.store(out, comment);
			out.close();
		} catch(IOException ex) {ex.printStackTrace();}
	}
}
