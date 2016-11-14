package pt.ulisboa.tecnico.csf.wecollect.domain.database;

import pt.ulisboa.tecnico.csf.wecollect.domain.Computer;
import pt.ulisboa.tecnico.csf.wecollect.domain.Pack;
import pt.ulisboa.tecnico.csf.wecollect.domain.User;
import pt.ulisboa.tecnico.csf.wecollect.domain.event.*;
import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;


public class DatabaseManager {

    private Connection connection = null;

    private static DatabaseManager mInstance;

    public static DatabaseManager getInstance(){
        if(mInstance == null){
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }


    private Connection connectToDB(){
        if(connection != null) return connection;
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

        Connection localConnection;

        try {
            localConnection = DriverManager.getConnection(url, connectionProps);
            System.out.println("Database connected!");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        connection = localConnection;
        return localConnection;
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

    public void commitComputer(Pack pack, boolean force) throws RegistryAlreadyExistsException {
        Connection con = connectToDB();
        Computer computer = pack.getComputer();

        // Verificar se este computador ja foi loggado na DB
        if(!force) {
            try (PreparedStatement pstmt = con.prepareStatement("SELECT name, sid FROM computers;")) {
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString("name").equals(computer.getName()) && resultSet.getString("sid").equals(computer.getSid())) {
                        resultSet.close();
                        pstmt.close();
                        throw new RegistryAlreadyExistsException(computer.getName(), computer.getSid());
                    }
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            while(resultSet.next()) computer.setId(resultSet.getInt("id"));
            resultSet.close();
        }catch (SQLException e) {
            e.printStackTrace();
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

    public void commitFirewallEvents(FirewallEvent firewallEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO fwlogs (timestamp, computer_id, allowed, protocol, src_ip, src_port, dst_ip, dst_port) VALUES (?, ?, ?, ?, INET6_ATON(?), ?, INET6_ATON(?), ?);")){
            pstmt.setTimestamp(1, firewallEvent.getTimestamp());
            pstmt.setInt(2, Pack.getInstance().getComputer().getId());
            pstmt.setBoolean(3, firewallEvent.isAllowed());
            pstmt.setString(4, firewallEvent.getProtocol());
            pstmt.setString(5, firewallEvent.getSourceIp());
            pstmt.setInt(6, firewallEvent.getSourcePort());
            pstmt.setString(7, firewallEvent.getDestIp());
            pstmt.setInt(8, firewallEvent.getDestPort());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void commitLoginEvents(LoginUserEvent loginUserEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO logons (user_id, logon_id, login_type, timestamp) VALUES (?, ?, ?, ?);")){
            commitLogEvents(pstmt, loginUserEvent.getUserId(), loginUserEvent.getLoginId(), loginUserEvent.getLoginType(), loginUserEvent.getTimestamp());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void commitLogoutEvents(LogoutUserEvent logoutUserEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO logoffs (user_id, logon_id, login_type, timestamp) VALUES (?, ?, ?, ?);")){
            commitLogEvents(pstmt, logoutUserEvent.getUserId(), logoutUserEvent.getLoginId(), logoutUserEvent.getLoginType(), logoutUserEvent.getTimestamp());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void commitLogEvents(PreparedStatement pstmt, int userId, long loginId, short loginType, Timestamp timestamp) throws SQLException {
        pstmt.setInt(1, userId);
        pstmt.setLong(2, loginId);
        pstmt.setShort(3, loginType);
        pstmt.setTimestamp(4, timestamp);
        pstmt.executeUpdate();
    }

    public void commitPasswordChangesUserEvent(PasswordChangesUserEvent passwordChangesUserEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pwchanges (user_id, timestamp, changed_by) VALUES (?, ?, ?);")){
            pstmt.setInt(1, passwordChangesUserEvent.getUserId());
            pstmt.setTimestamp(2, passwordChangesUserEvent.getTimestamp());
            pstmt.setInt(3, passwordChangesUserEvent.getChangedBy());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void commitAppAccessEvent(AppAccessEvent appAccessEvent){
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO appsaccesses (user_id, timestamp, name) VALUES (?, ?, ?);")){
            pstmt.setInt(1, appAccessEvent.getUserId());
            pstmt.setTimestamp(2, appAccessEvent.getTimestamp());
            pstmt.setString(3, appAccessEvent.getAppId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void commitUpdateEvents(UpdateEvent updateEvent) {
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO updates (timestamp, computer_id, name) VALUES (?, ?, ?);")){
            pstmt.setTimestamp(1, updateEvent.getTimestamp());
            pstmt.setInt(2, updateEvent.getComputerId());
            pstmt.setString(3, updateEvent.getUpdateTitle());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void disconnect(){
        System.out.println("Disconnecting from Database.");
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
        System.out.println("Disconnected from Database.");

    }
}
