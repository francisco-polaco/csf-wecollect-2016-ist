package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Computer;
import pt.ulisboa.tecnico.csf.wecollect.domain.Manager;
import pt.ulisboa.tecnico.csf.wecollect.domain.Pack;
import pt.ulisboa.tecnico.csf.wecollect.domain.User;
import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;

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


    public void commitComputer(Pack pack) throws RegistryAlreadyExistsException {
        Connection con = connectToDB();
        Computer computer = pack.getComputer();

        try (PreparedStatement pstmt = con.prepareStatement("SELECT name, sid FROM computers;")) {
            ResultSet resultSet = pstmt.executeQuery();
            while(resultSet.next()){
                if(resultSet.getString("name").equals(computer.getName()) && resultSet.getString("sid").equals(computer.getSid())) {
                    resultSet.close();
                    pstmt.close();
                    con.close();
                    throw new RegistryAlreadyExistsException(computer.getName(), computer.getSid());
                }
            }
            resultSet.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }

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

        // Inserir os valores na DB
        for(User u : userArrayList){
            try (PreparedStatement pstmt =
                         con.prepareStatement("INSERT INTO users (computer_id, relative_id, username) VALUES (?, ?, ?);")) {
                pstmt.setInt(1, computer.getId());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }

        // Obter os ids para os objectos Java, atribuidos pela DB
        for(User u : userArrayList) {
            try (PreparedStatement pstmt = con.prepareStatement("SELECT id FROM users WHERE computer_id=? AND relative_id=? AND username=?;")) {
                System.out.println(computer.getId());
                pstmt.setInt(1, computer.getId());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
                ResultSet resultSet = pstmt.executeQuery();
                if(resultSet.next()) {
                    u.setId(resultSet.getInt("id"));
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Atribuir os ids corretos dos users que criaram outros users
        for(User u : userArrayList) {
            try (PreparedStatement pstmt = con.prepareStatement("UPDATE users SET created_by=? WHERE id=?;")) {
                if(u.getCreatedById() == null)
                    pstmt.setNull(1, Types.INTEGER);
                else
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
