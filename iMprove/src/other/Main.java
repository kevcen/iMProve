package other;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private static Stage mainStage;

    public static void main(String[] args) { //called by jvm
        launch(args);
    }

    public static Stage getStage() { //static getter method to return the stage (window) of the application
        return mainStage;
    }

    @Override
    public void start(Stage stage) throws Exception { //called by launch(args)
        loadFonts();
        mainStage = stage;

        //show login screen
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene loginScene = new Scene(root);
        loginScene.setFill(null);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(loginScene);
        stage.setTitle("iMProve");
        stage.show();
    }

    public void loadFonts() { //load fonts with random size, doesn't matter
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/28DaysLater.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/ALBA__.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/ALBAM_.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/AKBAS_.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/BRADHITC.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/GOTHIC.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/GOTHICB.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/GOTHICBI.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/GOTHICI.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/SweetlyBroken.ttf"), 14);

    }
}
