package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Computer;
import pt.ulisboa.tecnico.csf.wecollect.domain.Manager;
import pt.ulisboa.tecnico.csf.wecollect.domain.Pack;
import pt.ulisboa.tecnico.csf.wecollect.domain.User;

import javax.xml.crypto.Data;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class DatabaseManager {

    private Connection connection;

    private static DatabaseManager mInstance;

    public static DatabaseManager getInstance(){
        if(mInstance == null){
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }

    private DatabaseManager(){
        //connection = connectToDB();
    }

    private Connection connectToDB(){
        String url = "jdbc:mysql://localhost:3306/wecollect";
        String username = "root";
        String password = "rootroot";

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

    public void commitNewLogs(Pack pack){
        Connection connection = connectToDB();
        //connection.commit();
    }

    public void query(String query){
        Connection connection = connectToDB();

    }


    public void commitComputer(Pack pack) {
        Connection con = connectToDB();
        Computer computer = pack.getComputer();
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO computers (name, sid) VALUES (?, ?);")) {
            pstmt.setString(1, computer.getName());
            pstmt.setString(2, computer.getSid());
            pstmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void commitUsers(Pack pack) {
        Connection con = connectToDB();
        Computer computer = pack.getComputer();
        ArrayList<User> userArrayList = pack.getUsers();
        for(User u : userArrayList){
            try (PreparedStatement pstmt =
                         con.prepareStatement("INSERT INTO users (computer_id, user_id, username, created_by) VALUES (?, ?, ?, ?);")) {
                pstmt.setString(1, computer.getSid());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
                pstmt.setString(4, u.getCreatedBySid());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                return;
            }finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        try {

            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
