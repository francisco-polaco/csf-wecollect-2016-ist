package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Computer;
import pt.ulisboa.tecnico.csf.wecollect.domain.Manager;
import pt.ulisboa.tecnico.csf.wecollect.domain.Pack;
import pt.ulisboa.tecnico.csf.wecollect.domain.User;

import javax.xml.crypto.Data;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

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
        Properties connectionProps = new Properties();
        connectionProps.put("user", "root");
        connectionProps.put("password", "rootroot");

        /*try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

        System.out.println("Connecting database...");

        Connection connection;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wecollect", connectionProps);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement pstmt = con.prepareStatement("SELECT id FROM computers WHERE name=? AND sid=?;")) {
            pstmt.setString(1, computer.getName());
            pstmt.setString(2, computer.getSid());
            ResultSet resultSet = pstmt.executeQuery();
            if(resultSet.next()) computer.setId(resultSet.getInt("id"));
            resultSet.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                System.out.println("Closing connection!");
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
                         con.prepareStatement("INSERT INTO users (computer_id, relative_id, username) VALUES (?, ?, ?);")) {
                pstmt.setInt(1, computer.getId());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
               // pstmt.setString(4, u.getCreatedBySid());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }
        try (PreparedStatement pstmt = con.prepareStatement("SELECT id FROM users;")) {
            ResultSet resultSet = pstmt.executeQuery();
            int i = 0;
            while(resultSet.next()) {
                userArrayList.get(i++).setId(resultSet.getInt("id"));
            }
            resultSet.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        for(User u : userArrayList) {
            try (PreparedStatement pstmt = con.prepareStatement("UPDATE users SET created_by=? WHERE id=?;")) {
                pstmt.setInt(1, u.getCreatedById());
                pstmt.setInt(2, u.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
