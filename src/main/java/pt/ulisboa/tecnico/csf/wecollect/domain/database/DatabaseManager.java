package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Computer;
import pt.ulisboa.tecnico.csf.wecollect.domain.Pack;
import pt.ulisboa.tecnico.csf.wecollect.domain.User;
import pt.ulisboa.tecnico.csf.wecollect.domain.event.ShutdownEvent;
import pt.ulisboa.tecnico.csf.wecollect.domain.event.StartupEvent;
import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;

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
//        String username = "root";
        String username = "wecollect";
//        String password = "rootroot";
        String password = "eDVBmspXvnX5u78F";
//        String host = "localhost";
        String host = "lis.pt.bernardocordeiro.eu";

        String url = "jdbc:mysql://" + host + ":3306/wecollect";

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        System.out.println("Connecting database...");

        Connection connection;

        try {
            connection = DriverManager.getConnection(url, connectionProps);
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

        // Verificar se este computador ja foi loggado na DB
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

        // Inserir dados do computador na DB
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO computers (name, sid) VALUES (?, ?);")) {
            pstmt.setString(1, computer.getName());
            pstmt.setString(2, computer.getSid());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Obter o id atribuido pela DB para atribuir no objecto JAVA
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
                         con.prepareStatement("INSERT INTO users (computer_id, relative_id, username, timestamp) VALUES (?, ?, ?, ?);")) {
                pstmt.setInt(1, computer.getId());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
                pstmt.setTimestamp(4, u.getCreatedOn());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }

        // Obter os ids para os objectos Java, atribuidos pela DB
        for(User u : userArrayList) {
            try (PreparedStatement pstmt = con.prepareStatement("SELECT id FROM users WHERE computer_id=? AND relative_id=? AND username=? AND timestamp=?;")) {
                pstmt.setInt(1, computer.getId());
                pstmt.setString(2, u.getUserSid());
                pstmt.setString(3, u.getUsername());
                pstmt.setTimestamp(4, u.getCreatedOn());
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
            System.out.println("Closing connection!");
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void commitStartupEvents(StartupEvent startupEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO startups (timestamp, computer_id) VALUES (?, ?);")){
            pstmt.setTimestamp(1, startupEvent.getTimestamp());
            pstmt.setInt(2, Pack.getInstance().getComputer().getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void commitShutdownEvents(ShutdownEvent shutdownEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO shutdowns (timestamp, computer_id) VALUES (?, ?);")){
            pstmt.setTimestamp(1, shutdownEvent.getTimestamp());
            pstmt.setInt(2, Pack.getInstance().getComputer().getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
