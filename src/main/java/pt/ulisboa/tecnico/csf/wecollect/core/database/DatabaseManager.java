package pt.ulisboa.tecnico.csf.wecollect.core.database;

import org.apache.commons.dbutils.DbUtils;
import pt.ulisboa.tecnico.csf.wecollect.core.Computer;
import pt.ulisboa.tecnico.csf.wecollect.core.Pack;
import pt.ulisboa.tecnico.csf.wecollect.core.User;
import pt.ulisboa.tecnico.csf.wecollect.core.event.*;
import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;


public class DatabaseManager {

    private Connection connection = null;

    private static DatabaseManager mInstance;

    public DatabaseManager() {
        connection = connectToDB();
    }

    public static DatabaseManager getInstance(){
        if(mInstance == null){
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }

    public static String username;
    public static String password;
    public static String hostname;

    private Connection connectToDB(){
        if(connection != null) return connection;
        else {

            String url;
            if(hostname.contains(":")) url = "jdbc:mysql://" + hostname + "/wecollect";
            else url = "jdbc:mysql://" + hostname + ":3306/wecollect";

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
            return localConnection;
        }
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
                        DbUtils.close(resultSet);
                        DbUtils.close(pstmt);
                        throw new RegistryAlreadyExistsException(computer.getName(), computer.getSid());
                    }
                }
                DbUtils.close(resultSet);
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
            DbUtils.close(resultSet);
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
                DbUtils.close(resultSet);
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

    public void commitWifiEvents(WifiEvent wifiEvent) {
        Connection conn = connectToDB();
        // Check if it is wifi connect or disconnect
        if (wifiEvent.getConnect()) {
            // If Wifi event is a connection to a Access Point
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO wificonnects (timestamp, computer_id, ssid) VALUES (?, ?, ?);")){
                pstmt.setTimestamp(1, wifiEvent.getTimestamp());
                pstmt.setInt(2, wifiEvent.getComputerId());
                pstmt.setString(3, wifiEvent.getSsid());
                pstmt.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        } else {
            // If Wifi event is a disconnection to a Access Point
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO wifidisconnects (timestamp, computer_id, ssid) VALUES (?, ?, ?);")){
                pstmt.setTimestamp(1, wifiEvent.getTimestamp());
                pstmt.setInt(2, wifiEvent.getComputerId());
                pstmt.setString(3, wifiEvent.getSsid());
                pstmt.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public void commitDeviceEvent(DeviceEvent deviceEvent) {
        Connection conn = connectToDB();
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO devices (timestamp, computer_id, name, container_id, task_count, property_count, work_time) VALUES (?, ?, ?, unhex(replace(?,'-','')), ?, ?, ?);")){
            pstmt.setTimestamp(1, deviceEvent.getTimestamp());
            pstmt.setInt(2, deviceEvent.getComputerId());
            pstmt.setString(3, deviceEvent.getName());
            pstmt.setString(4, deviceEvent.getContainerId());
            pstmt.setInt(5, deviceEvent.getTaskCount());
            pstmt.setInt(6, deviceEvent.getPropertyCount());
            pstmt.setInt(7, deviceEvent.getWorkTime());
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void disconnect(){
        System.out.println("Disconnecting from Database.");
        try {
            DbUtils.close(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
        System.out.println("Disconnected from Database.");

    }
}
