package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class DatabaseManager {


    private static DatabaseManager mInstance;

    public static DatabaseManager getInstance(){
        if(mInstance == null){
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }

    private Connection connectToDB(){
        String url = "jdbc:mysql://localhost:3306/javabase";
        String username = "java";
        String password = "password";

        System.out.println("Connecting database...");

        Connection connection;

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected!");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        return connection;
    }

    public void emptyEntries(){
        Connection connection = connectToDB();
    }

    public void commitNewLogs(){
        Connection connection = connectToDB();

    }

    public void query(String query){
        Connection connection = connectToDB();

    }


}
