package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import other.DAO;

public class ReviewController {
	private RootController rootController;
	//stores data for charts
	private XYChart.Series<Number, Number> dailySeries = new XYChart.Series<>();
	private XYChart.Series<String, Number> monthlySeries = new XYChart.Series<>();
	private XYChart.Series<Number, Number> yearlySeries = new XYChart.Series<>();
	//dao object for accessing the database
	private DAO dao = new DAO();
	//UI object
	@FXML private AnchorPane apnDaily, apnMonthly, apnYearly;
	@FXML private Text txtProgress;
	
	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}
	public void initialize() { //called automatically after fxml injection
		
		Platform.runLater(()-> {
			//change title text
			txtProgress.setText(RootController.getName() + "'s All-Time Progress");
			//set the graph data and display it
			getScores();
			setDailyGraph();
			setMonthlyGraph();
			setYearlyGraph();
		});
	}
	//reusable methods to get the date more efficiently and cleanly
	public int getDate() {
		Calendar now = Calendar.getInstance();
		return now.get(Calendar.DAY_OF_MONTH);
	}
	public String getMonth(int num) { //num = -1 for NOW month, 0 <= num <= 11 for inputs
		Calendar now = Calendar.getInstance();
		String month = null;
		switch(num==-1 ? now.get(Calendar.MONTH) : num) {
			case 0: month = "Jan";
				break;
			case 1: month = "Feb";
				break;
			case 2: month = "Mar";
				break;
			case 3: month = "Apr";
				break;
			case 4: month = "May";
				break;
			case 5: month = "Jun";
				break;
			case 6: month = "Jul";
				break;
			case 7: month = "Aug";
				break;
			case 8: month = "Sep";
				break;
			case 9: month = "Oct";
				break;
			case 10: month = "Nov";
				break;
			case 11: month = "Dec";
				break;
		}
		return month;
	}
	public int getYear() {
		Calendar now = Calendar.getInstance();
		return now.get(Calendar.YEAR);
	}
	//daily graph 
	public void setDailyGraph() {
		//set axes and customise
		final NumberAxis xAxis = new NumberAxis(1, 31, 1);
		final NumberAxis yAxis = new NumberAxis(0, 10, 1);
		xAxis.setLabel("Day of month");	
		xAxis.setMinorTickVisible(false);
		xAxis.setAutoRanging(false);
		yAxis.setLabel("Hi-score");
		yAxis.setMinorTickVisible(false);
		yAxis.setAutoRanging(false);
		
		//the line chart object, axes specified in parameters
		final LineChart<Number, Number> dailyChart = new LineChart<>(xAxis, yAxis);
		//customise chart
		dailyChart.setTitle(getMonth(-1) + " Progress");
		dailyChart.setMaxSize(680, 440);
		dailyChart.setMinSize(680, 440);
		dailyChart.setPrefSize(680, 440);
		dailyChart.setLegendVisible(false);
		//add data for chart
		dailyChart.getData().add(dailySeries);
		//add chart to interface
		apnDaily.getChildren().add(dailyChart);
	}
	//monthly graph
	public void setMonthlyGraph() {
		final CategoryAxis xAxis = new CategoryAxis(FXCollections.observableArrayList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
		final NumberAxis yAxis = new NumberAxis(0, 10, 1);
		xAxis.setLabel("Month in year");
		xAxis.setAutoRanging(false);
		yAxis.setLabel("Average score");
		yAxis.setMinorTickLength(0.25);
		yAxis.setAutoRanging(false);
		final LineChart<String, Number> monthlyChart = new LineChart<>(xAxis, yAxis);
		monthlyChart.setTitle(getYear() + " Progress");
		monthlyChart.setMaxSize(680, 440);
		monthlyChart.setMinSize(680, 440);
		monthlyChart.setPrefSize(680, 440);
		monthlyChart.setLegendVisible(false);
		monthlyChart.getData().add(monthlySeries);
		apnMonthly.getChildren().add(monthlyChart);
	}
	//yearly graph
	public void setYearlyGraph() {
		final NumberAxis xAxis = new NumberAxis(2015, 2050, 1);
		final NumberAxis yAxis = new NumberAxis(0, 10, 1);
		xAxis.setAutoRanging(false);
		xAxis.setMinorTickVisible(false);
		xAxis.setLabel("Year");
		yAxis.setLabel("Average score");
		yAxis.setMinorTickLength(0.25);
		yAxis.setAutoRanging(false);
		final LineChart<Number, Number> yearlyChart = new LineChart<>(xAxis, yAxis);
		yearlyChart.setTitle("Yearly Progress");
		yearlyChart.setMaxSize(680, 440);
		yearlyChart.setMinSize(680, 440);
		yearlyChart.setPrefSize(680, 440);
		yearlyChart.setLegendVisible(false);
		yearlyChart.getData().add(yearlySeries);
		apnYearly.getChildren().add(yearlyChart);
	}
	//get the data for the graphs first
	public void getScores() {
		//set series data
		try(
			Connection conn = dao.getConnection();
			PreparedStatement dailyStmt = conn.prepareStatement("SELECT Highscore, ScoreDay FROM Score WHERE ScoreMonth = ? AND ScoreYear = ?");
			PreparedStatement monthlyStmt = conn.prepareStatement("SELECT Highscore, ScoreMonth FROM Score WHERE ScoreYear = ? ORDER BY ScoreMonth");
			PreparedStatement yearlyStmt = conn.prepareStatement("SELECT Highscore, ScoreYear FROM Score ORDER BY ScoreYear");
		) {
			//daily graph
			Calendar now = Calendar.getInstance();
			dailyStmt.setInt(1, now.get(Calendar.MONTH)+1);
			dailyStmt.setInt(2, getYear());
			ResultSet dailyRs = dailyStmt.executeQuery();
			while(dailyRs.next()) {
				XYChart.Data<Number, Number> dayPoint = new XYChart.Data<>(dailyRs.getInt("ScoreDay"), dailyRs.getInt("Highscore"));
				dailySeries.getData().add(dayPoint);
			}
			
			//monthly graph
			monthlyStmt.setInt(1, getYear());
			ResultSet monthlyRs = monthlyStmt.executeQuery();
			String lastMonth = "";
			double mTotal = 0;
			double mFreq = 0;
			while(monthlyRs.next()) {
				if(getMonth(monthlyRs.getInt("ScoreMonth")-1).equals(lastMonth)) {
					mTotal += monthlyRs.getInt("Highscore");
					mFreq++;
				} else {
					if(!lastMonth.equals("")) { //after the first, if the month changes, add the last month's avg scores to the graph
						XYChart.Data<String, Number> monthPoint = new XYChart.Data<>(lastMonth, mTotal/mFreq); //total/frequency is the average score of the month
						monthlySeries.getData().add(monthPoint);
					}
					//reintialize variables (including first score)
					lastMonth = getMonth(monthlyRs.getInt("ScoreMonth")-1); //method is 0 indexed, db is 1 indexed so -1
					mTotal = monthlyRs.getInt("Highscore");
					mFreq = 1;
				}
			}
			//at the end, add the final last month (since it won't change to another month after the last month)
			XYChart.Data<String, Number> finalMonthPoint = new XYChart.Data<>(lastMonth, mTotal/mFreq);
			monthlySeries.getData().add(finalMonthPoint);
			
			//yearly graph
			ResultSet yearlyRs = yearlyStmt.executeQuery();
			int lastYear = -1;
			double yTotal = 0;
			double yFreq = 0;
			while(yearlyRs.next()) {
				if(yearlyRs.getInt("ScoreYear") == lastYear) {
					yTotal += yearlyRs.getInt("Highscore");
					yFreq++;
				} else {
					if(lastYear != -1) { //after the first, if the year changes, add the last year's avg scores to the graph
						XYChart.Data<Number, Number> yearPoint = new XYChart.Data<>(lastYear, yTotal/yFreq); //total/frequency is the average score of the year
						yearlySeries.getData().add(yearPoint);
					}
					//reintialize variables (including first score)
					lastYear = yearlyRs.getInt("ScoreYear"); //method is 0 indexed, db is 1 indexed so -1
					yTotal = yearlyRs.getInt("Highscore");
					yFreq = 1;
				}
			}
			//at the end, add the final last month (since it won't change to another year after the last year)
			XYChart.Data<Number, Number> finalYearPoint = new XYChart.Data<>(lastYear, yTotal/yFreq);
			yearlySeries.getData().add(finalYearPoint);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
