package other;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.ucanaccess.jdbc.UcanaccessConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
    private static final String dbDir = "C://iMProve";
    private static final String dbName = "improveDB.accdb";
    private static final String dbUrl = "jdbc:ucanaccess://" + dbDir + "//" + dbName;
    private static ObservableList<Connection> allConnections = FXCollections.observableArrayList();

    public DAO() { //constructor - called when object is made
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot load ucanaccess driver");
            e.printStackTrace();
        }
        File directory = new File(dbDir);
        if (!directory.exists()) //create directory if not already
            directory.mkdir();
        File database = new File(dbDir + "//" + dbName);
        if (!database.exists()) { //copy the database file into user's file system - if not already
            try {
                Files.copy(DAO.class.getResourceAsStream(dbName), database.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void closeConnections() {
        try {
            if (!allConnections.isEmpty()) {
                ((UcanaccessConnection) allConnections.get(0)).unloadDB(); //unload resources on ucanaccess driver
                allConnections.clear(); //no more open connections after all closed + resources unloaded
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        File database = new File(dbDir + "//" + dbName);
        try {
            Files.copy(DAO.class.getResourceAsStream(dbName), database.toPath(), StandardCopyOption.REPLACE_EXISTING); //replace the file system's database to a fresh new one
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() { //create a connection to the database
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //add to collection to later clear
        allConnections.add(conn);
        return conn;
    }
}
