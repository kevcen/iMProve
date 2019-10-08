package model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AuralTest {
	//instance variables holding required data for the aural test
	private String audioFile;
	private MediaPlayer auralPlayer;
	private String modulation;
	private double duration;
	//initialise variables and load the audio into a MediaPlayer object
	public AuralTest(String audioFile, String modulation) {
		this.audioFile = audioFile;
		this.modulation = modulation;
		createAuralPlayer();
	}
	
	private void createAuralPlayer() {
		//load audio into MediaPlayer object
		Media media = new Media(getClass().getResource("/resources/audio/"+audioFile).toExternalForm());
		auralPlayer = new MediaPlayer(media);
		//set the duration variable when it is loaded
		auralPlayer.setOnReady(()->duration = auralPlayer.getMedia().getDuration().toMillis());
	}
	
	//getter setter methods
	public MediaPlayer getMediaPlayer() {
		return auralPlayer;
	}
	public String getModulation() {
		return modulation;
	}
	public double getDuration() {
		return duration;
	}
}
