package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class LearnUpdateRowController {
	@FXML private TextField fldTerm, fldDef;
	@FXML private AnchorPane apnCardRow;
	private LearnUpdateController updateController = null;
	public void initData(LearnUpdateController c) {
		updateController = c;
	}
	//remove the row
	@FXML
	public void removeCard(ActionEvent e) {
		VBox vbxCards = (VBox) apnCardRow.getParent(); //remove from update interface
		vbxCards.getChildren().remove(apnCardRow);
		//remove controller
		updateController.getControllerMap().remove(apnCardRow);
	}
	
	//if enter is pressed when inside a textfield, then finish - submit the changes
	@FXML
	public void finish(ActionEvent e) {
		updateController.doFinish();
	}
	
	//getter setter methods
	public void setTerm(String term) { //set foreign term
		fldTerm.setText(term);
	}
	public void setDef(String def) { //set definition
		fldDef.setText(def); 
	}
	public String getTerm() {
		return fldTerm.getText();
	}
	public String getDef() {
		return fldDef.getText();
	}
}
