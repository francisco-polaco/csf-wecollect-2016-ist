package pt.ulisboa.tecnico.csf.wecollect.core;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;
import pt.ulisboa.tecnico.csf.wecollect.core.event.*;
import pt.ulisboa.tecnico.csf.wecollect.exception.DirectoryWithoutFilesException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToCreateWorkingDirException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToRunPythonException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Manager {

    private static final String WORKING_DIR = "wdir";

    private String _rootFs;

    private boolean _force = false;

    private static Manager mInstance;

    public static Manager getInstance() {
        if (mInstance == null) {
            mInstance = new Manager();
        }
        return mInstance;
    }

    public void process(String windowsDirPath) {
        _rootFs = windowsDirPath;
        if (!_force) {
            processEvtx(windowsDirPath);
        }

        try {
            getPackReady();
        } catch (IOException | XPathExpressionException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void clearTmp() {
        try {
            Files.deleteIfExists(Paths.get(WORKING_DIR));
        } catch (DirectoryNotEmptyException e) {
            File wdir = new File(WORKING_DIR);
            if (wdir.listFiles() != null) {
                ArrayList<File> files = new ArrayList<>(Arrays.asList(wdir.listFiles()));
                for (File f : files) {
                    if (!f.delete()) {
                        System.err.println(f.getName() + " was not deleted!");
                    }
                }
            }
            boolean delete = wdir.delete();
            if (!delete) System.err.println("Working Directory not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processEvtx(String rootFs) throws ImpossibleToRunPythonException {

        System.out.println("Starting to process EVTX.");
        if (rootFs.endsWith("/")) { // cleaning the last slash
            System.out.println("Cleaning path");
            rootFs = rootFs.substring(0, rootFs.length() - 1);
        }

        System.out.println("Checking working folder.");
        checkWorkingFolder();

        String evtxDirPath = rootFs + "/System32/winevt/Logs";
        ArrayList<File> evtxFiles = prepareAndGetEvtx(evtxDirPath);

        for (File file : evtxFiles) {
            Process p;
            try {
                System.out.println("Processing file: " + file.getAbsolutePath());
                p = Runtime.getRuntime().exec("python2 extras/evtxdump.pyc " + file.getAbsolutePath());
            } catch (IOException e) {
                try {
                    // If user 'python' alias instead of 'python2'. Depending on the installation
                    p = Runtime.getRuntime().exec("python extras/evtxdump.pyc " + file.getAbsolutePath());
                } catch (IOException e2) {
                    e2.printStackTrace();
                    throw new ImpossibleToRunPythonException(e2.getMessage());
                }
            }

            getXMLReadyForParse(file.getName(), p);
            try {
                System.out.println("Exitcode: " + p.waitFor());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getXMLReadyForParse(String evtxFilename, Process p) {
        try (BufferedReader inOut = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            //boolean hadErrorOccur = false;
            String line;
            String filenameXml = evtxFilename.replace(".evtx", ".xml").replace(" ", "");
            System.out.println("Writing: " + filenameXml);
            PrintWriter pw = new PrintWriter(new FileWriter(WORKING_DIR + "/" + filenameXml));
            while ((line = inOut.readLine()) != null) {
                //hadErrorOccur = true;
                if (!line.contains("&lt;") || !line.contains("&gt;") ||
                        !line.contains("&apos;") || !line.contains("%apos;"))
                    line = line.replaceAll("&", "");

                if (line.contains("\0")) line = line.replaceAll("\0", "");

                if (line.contains("\1")) line = line.replaceAll("\1", "");

                if (line.contains("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\""))
                    line = line.replaceAll("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\"", "");

                String xmlIdent = xmlIdent(line);
                pw.write(xmlIdent + line + "\n");
            }
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<File> prepareAndGetEvtx(String evtxDirPath) {
        ArrayList<File> files = new ArrayList<>();

        File security = new File(evtxDirPath + "/Security.evtx");
        if (security.exists()) files.add(security);
        File user = new File(evtxDirPath + "/Microsoft-Windows-User Profile Service%4Operational.evtx");
        if (user.exists()) files.add(user);
        File updates = new File(evtxDirPath + "/Microsoft-Windows-WindowsUpdateClient%4Operational.evtx");
        if (updates.exists()) files.add(updates);
        File appAccess = new File(evtxDirPath + "/Microsoft-Windows-TWinUI%4Operational.evtx");
        if (appAccess.exists()) files.add(appAccess);
        File wifi = new File(evtxDirPath + "/Microsoft-Windows-WLAN-AutoConfig%4Operational.evtx");
        if (wifi.exists()) files.add(wifi);
        File devices = new File(evtxDirPath + "/Microsoft-Windows-DeviceSetupManager%4Admin.evtx");
        if (devices.exists()) files.add(devices);

        for (File f : files) {
            try {
                Files.copy(Paths.get(f.getAbsolutePath()),
                        Paths.get(WORKING_DIR + "/" + f.getName().replace(" ", "").replace("%4", "").replace("-", "")), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getListOfFiles(WORKING_DIR);
    }

    private String xmlIdent(String line) {
        String xmlIdent = "";
        if (!(line.startsWith("<Events>") || line.startsWith("</Events>") || line.startsWith("<?xml"))) {
            xmlIdent += "\t";
            if (!(line.startsWith("<Event >") || line.startsWith("</Event>"))) {
                xmlIdent += "\t";
                if (line.startsWith("<Data ")) {
                    xmlIdent += "\t";
                }
            }
        }
        return xmlIdent;
    }

    private void checkWorkingFolder() {
        File f;
        if (!((f = new File(WORKING_DIR)).exists())) {
            if (!(f.mkdir())) {
                throw new ImpossibleToCreateWorkingDirException();
            }
        } else { // to avoid reusing xml
            System.out.println("Temporary Directory already exists, deleting it.");
            f = null;
            clearTmp();
            checkWorkingFolder();
        }
    }

    private ArrayList<File> getListOfFiles(String dirpath) {
        File dir = new File(dirpath);
        if (dir.listFiles() == null) {
            throw new DirectoryWithoutFilesException(dirpath);
        }
        ArrayList<File> files = new ArrayList<>(Arrays.asList(dir.listFiles()));

        for (File f : files) {
            String filename = f.getName();
            if (f.isDirectory() || filename.equals(".") || filename.equals("..")) {
                files.remove(f);
            }
        }
        return files;
    }

    private void getPackReady() throws IOException, XPathExpressionException {
        Pack p = new Pack();

        processComputer(p);
        processUsers(p);

        // Check first if XML files exist. If yes, process each event

        File xmlSecurity = new File(WORKING_DIR + "/Security.xml");
        if (xmlSecurity.exists() && !xmlSecurity.isDirectory()) processStartupEvents(p, xmlSecurity.getPath());
        if (xmlSecurity.exists() && !xmlSecurity.isDirectory()) processEventLoggerShutdownEvents(p, xmlSecurity.getPath());
        if (xmlSecurity.exists() && !xmlSecurity.isDirectory()) processLoginEvents(p, xmlSecurity.getPath());
        if (xmlSecurity.exists() && !xmlSecurity.isDirectory()) processLogoutEvents(p, xmlSecurity.getPath());
        if (xmlSecurity.exists() && !xmlSecurity.isDirectory()) processPasswordChangesUserEvents(p, xmlSecurity.getPath());

        File xmlUpdate = new File(WORKING_DIR + "/MicrosoftWindowsWindowsUpdateClientOperational.xml");
        if (xmlUpdate.exists() && !xmlUpdate.isDirectory()) processUpdates(p, xmlUpdate.getPath());

        File xmlDevices = new File(WORKING_DIR + "/MicrosoftWindowsDeviceSetupManagerAdmin.xml");
        if (xmlDevices.exists() && !xmlDevices.isDirectory()) processDevices(p, xmlDevices.getPath());

        File xmlWifi = new File(WORKING_DIR + "/MicrosoftWindowsWLANAutoConfigOperational.xml");
        if (xmlWifi.exists() && !xmlWifi.isDirectory()) processWifi(p, xmlWifi.getPath());

        File xmlAppAccesses = new File(WORKING_DIR + "/MicrosoftWindowsTWinUIOperational.xml");
        if (xmlAppAccesses.exists() && !xmlAppAccesses.isDirectory()) processAppAccessesEvents(p, xmlAppAccesses.getPath());

        processFirewallEvents(p);

        p.forceCommitToDb();

    }

    private void processComputer(Pack p) throws IOException {
        // Computer
        Computer c = new Computer();

        ArrayList<String> computerDetails = getComputerDetails();
        c.setSid(computerDetails.get(1));
        c.setName(computerDetails.get(0));
        p.setComputer(c);

        boolean force = _force;
        _force = true;
        DatabaseManager.getInstance().commitComputer(p, force);
    }

    private void processUsers(Pack p) throws XPathExpressionException {
        // Users

        ArrayList<User> userArrayList = getUsers();
        p.setUsers(userArrayList);

        DatabaseManager.getInstance().commitUsers(p);
    }

    private void processStartupEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4608\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            // Don't ask me, we are doing a travel through the tree :D
            String timestampString = childNodes.item(0).getChildNodes().item(14).getAttributes().getNamedItem("SystemTime").getTextContent();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(timestampString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

            pack.addEvent(new StartupEvent(timestamp, pack.getComputer().getId()));
        }
    }

    private void processEventLoggerShutdownEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"1100\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            Timestamp timestamp = getTimestampFromXML(childNodes);
            pack.addEvent(new ShutdownEvent(timestamp, pack.getComputer().getId()));
        }

    }

    private void processPasswordChangesUserEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(xmlPath);

        // The Subject attempted to reset the password of the Target
        String otherUserAttempted = "/Events/Event/System/EventID[text()=\"4724\"]";
        NodeList otherUserAttemptedNodes = (NodeList) xpath.evaluate(otherUserAttempted, inputSource, XPathConstants.NODESET);

        // The user attempted to change his/her own password
        String userAttempted = "/Events/Event/System/EventID[text()=\"4723\"]";
        NodeList userAttemptedNodes = (NodeList) xpath.evaluate(userAttempted, inputSource, XPathConstants.NODESET);

        NodeList allNodes[] = {otherUserAttemptedNodes, userAttemptedNodes};

        for (NodeList eachNode : allNodes) {
            processEachTypePwdChange(eachNode, pack);
        }
    }

    private void processEachTypePwdChange(NodeList nodes, Pack pack) throws XPathExpressionException {
        for (int i = 0; i < nodes.getLength(); i++) {
            String sid = "";
            String changedBy = "";

            Timestamp timestamp = null;
            boolean toSkip = false;

            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            if (childNodes.item(2) == null) continue;
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();
            // Timestamp
            timestamp = getTimestampFromXML(childNodes);

            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                // The Subject attempted to reset the password of the Target
                // So, changeBy Subject User
                if (attributes.item(0).getNodeValue().equals("TargetSid")) {
                    if (!(data.item(j).getTextContent().length() > 8)) {
                        toSkip = true;
                        break;
                    }
                    // Get the SID of the user that password will change
                    sid = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("SubjectUserSid")) {
                    // Get the SID of the user that change password, the Subject
                    changedBy = data.item(j).getTextContent();
                }
            }
            if (!toSkip) {
                try {
                    pack.addEvent(new PasswordChangesUserEvent(timestamp, pack.getComputer().getId(), Pack.getInstance().getUserIdBySid(sid), sid,
                            Pack.getInstance().getUserIdBySid(changedBy)));
                } catch (IllegalStateException e) {
                    System.err.println("PwdChange: User id of this event was not found.");
                }
            }
        }
    }

    private void processUpdates(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"41\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();

            // TAG EventData
            if (childNodes.item(2) == null) continue;
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            Timestamp timestamp = getTimestampFromXML(childNodes);

            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("updateTitle")) {
                    String updateTitle = data.item(j).getTextContent();
                    pack.addEvent(new UpdateEvent(timestamp, pack.getComputer().getId(), updateTitle));
                }
            }
        }
    }

    private void processDevices(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"112\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            String deviceName = "";
            String containerId = "";
            int taskCount = 0;
            int propertyCount = 0;
            int workTime = 0;
            Timestamp timestamp = null;
            boolean toSkip = false;

            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();

            // TAG EventData
            if (childNodes.item(2) == null) continue;
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("Prop_DeviceName")) {
                    if ((data.item(j).getTextContent().equals("NULL"))) {
                        toSkip = true;
                        break;
                    }
                    deviceName = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("Prop_ContainerId")) {
                    if ((data.item(j).getTextContent().equals("{00000000-0000-0000-ffff-ffffffffffff}"))) {
                        toSkip = true;
                        break;
                    }
                    containerId = data.item(j).getTextContent().replace("{", "").replace("}", "");
                } else if (attributes.item(0).getNodeValue().equals("Prop_TaskCount")) {
                    taskCount = Integer.parseInt(data.item(j).getTextContent());
                } else if (attributes.item(0).getNodeValue().equals("Prop_PropertyCount")) {
                    propertyCount = Integer.parseInt(data.item(j).getTextContent());
                } else if (attributes.item(0).getNodeValue().equals("Prop_WorkTime_MilliSeconds")) {
                    workTime = Integer.parseInt(data.item(j).getTextContent());
                }

            }
            if (!toSkip)
                pack.addEvent(new DeviceEvent(timestamp, pack.getComputer().getId(), deviceName, containerId, taskCount, propertyCount, workTime));
        }
    }

    private void processWifi(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(xmlPath);

        // Wifi network connected
        String wifiConnect = "/Events/Event/System/EventID[text()=\"8001\"]";
        NodeList wifiConnectNodes = (NodeList) xpath.evaluate(wifiConnect, inputSource, XPathConstants.NODESET);

        // Wifi network disconnected
        String wifiDisconnected = "/Events/Event/System/EventID[text()=\"8003\"]";
        NodeList wifiDisconnectNodes = (NodeList) xpath.evaluate(wifiDisconnected, inputSource, XPathConstants.NODESET);

        processEachWifiType(wifiConnectNodes, pack, true);
        processEachWifiType(wifiDisconnectNodes, pack, false);
    }

    private void processEachWifiType(NodeList nodes, Pack pack, Boolean isConnect) throws XPathExpressionException {
        for (int i = 0; i < nodes.getLength(); i++) {
            Timestamp timestamp = null;
            String ssid = "";

            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);

            // TAG EventData
            if (childNodes.item(2) == null) continue;
            NodeList data = childNodes.item(2).getChildNodes();

            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("SSID")) {
                    ssid = data.item(j).getTextContent();
                    pack.addEvent(new WifiEvent(timestamp, pack.getComputer().getId(), ssid, isConnect));
                }
            }
        }

    }

    private void processLogoutEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4647\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            String sid = "";
            String logonId = "";
            String loginType = "";
            Timestamp timestamp = null;
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            if (childNodes.item(2) == null) continue;
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserSid")) {
                    if (!(data.item(j).getTextContent().length() > 8)) { // special sids
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    sid = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("TargetLogonId")) {
                    logonId = data.item(j).getTextContent();
                }

            }
            if (!toSkip) {
                try {
                    loginType = "2";
                    pack.addEvent(new LogoutUserEvent(timestamp, pack.getComputer().getId(), Pack.getInstance().getUserIdBySid(sid), sid,
                            new BigInteger(logonId.substring(2), 16).longValue(), Short.parseShort(loginType)));
                } catch (IllegalStateException e) {
                    System.err.println("LogoutEvents: User id of this event was not found.");
                }
            }

        }
    }

    private void processLoginEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4624\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            String sid = "";
            String logonId = "";
            String loginType = "";
            Timestamp timestamp = null;
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            if (childNodes.item(2) == null) continue;
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserSid")) {
                    if (!(data.item(j).getTextContent().length() > 8)) { // special sids
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    sid = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("TargetLogonId")) {
                    logonId = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("LogonType")) {
                    loginType = data.item(j).getTextContent();
                }

            }
            if (!toSkip) {
                try {
                    pack.addEvent(new LoginUserEvent(timestamp, pack.getComputer().getId(), Pack.getInstance().getUserIdBySid(sid), sid,
                            new BigInteger(logonId.substring(2), 16).longValue(), Short.parseShort(loginType)));

                } catch (IllegalStateException e) {
                    System.err.println("LoginEvents: User id of this event was not found.");
                }
            }
        }
    }

    private void processAppAccessesEvents(Pack pack, String xmlPath) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"5950\"]";
        InputSource inputSource = new InputSource(xmlPath);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for (int i = 0; i < nodes.getLength(); i++) {
            String appId = "";

            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();

            // Timestamp
            Timestamp timestamp = getTimestampFromXML(childNodes);

            // TAG System
            if (childNodes.item(0) == null) continue;
            String sid = childNodes.item(0).getChildNodes().item(26).getAttributes().getNamedItem("UserID").getTextContent();

            // TAG EventData
            if (childNodes.item(2) == null) continue;
            NodeList data = childNodes.item(2).getChildNodes();
            NamedNodeMap attributes = data.item(0).getAttributes();

            // Get App ID
            if (attributes.item(0).getNodeValue().equals("AppId")) {
                appId = data.item(0).getTextContent();
            }

            try {
                pack.addEvent(new AppAccessEvent(
                        timestamp,
                        pack.getComputer().getId(),
                        Pack.getInstance().getUserIdBySid(sid),
                        sid,
                        appId ));

            } catch (IllegalStateException e) {
                System.err.println("AppAccessesEvents: User id of this event was not found.");
            }

        }
    }

    private void processFirewallEvents(Pack pack) {
        String fwDirPath = _rootFs + "/System32/LogFiles/Firewall";
        ArrayList<File> files = getListOfFiles(fwDirPath);
        files.removeIf(f -> !(f.getName().endsWith(".log") || !f.getName().contains(".log.old")));
        Collections.sort((List) files, new FileExtensionComparator());

        if (files.size() > 1) {
            for (int i = 1; i < files.size(); i++) {
                System.out.println("Firewall log.");
                processFirewallFile(files.get(i), pack);
            }
        }
        if (files.size() > 0)
            processFirewallFile(files.get(0), pack); // most recent file
    }

    private void processFirewallFile(File file, Pack pack) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if ((line.startsWith("1") || line.startsWith("2"))) // should be enough for some years
                {
                    String[] tokens = line.split(" ");
                    boolean allow = false;
                    if (tokens[2].equals("ALLOW")) allow = true;
                    if (!(tokens[3].equals("TCP") || tokens[3].equals("UDP"))) continue;
                    // TODO timestamp
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date parsedDate = null;
                    try {
                        parsedDate = dateFormat.parse(tokens[0] + " " + tokens[1]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

                    pack.addEvent(new FirewallEvent(timestamp, pack.getComputer().getId(), allow, tokens[3], tokens[4], tokens[5],
                                Integer.parseInt(tokens[6]), Integer.parseInt(tokens[7])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FileExtensionComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.substring(o1.lastIndexOf('.')).compareTo(o2.substring(o2.lastIndexOf('.')));
        }
    }

    private ArrayList<User> getUsers() throws XPathExpressionException {
        ArrayList<User> userArrayList = new ArrayList<>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4720\"]";
        InputSource inputSource = new InputSource(WORKING_DIR + "/Security.xml");
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            String username = "";
            String sid = "";
            String createdBySid = "";
            String createdByUname = "";
            Timestamp timestamp = null;
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for (int j = 0; j < data.getLength(); j += 2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserName")) {
                    if (data.item(j).getTextContent().equals("defaultuser0")) {
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    username = data.item(j).getTextContent();
                } else if (attributes.item(0).getNodeValue().equals("TargetSid")) {
                    int indexOfLastDash = data.item(j).getTextContent().lastIndexOf("-");
                    sid = data.item(j).getTextContent().substring(indexOfLastDash + 1);
                } else if (attributes.item(0).getNodeValue().equals("SubjectUserSid")) {
                    int indexOfLastDash = data.item(j).getTextContent().lastIndexOf("-");

                    if (data.item(j).getTextContent().substring(indexOfLastDash + 1).equals("18")) {
                        // God created this account, so with will not reveal that god exists
                        createdBySid = null;
                        createdByUname = null;
                        break;
                    } else {

                        for (User u : userArrayList) {
                            if (u.getUserSid().equals(data.item(j).getTextContent().substring(indexOfLastDash + 1))) {
                                userArrayList.add(new User(sid, username, u, timestamp));
                                toSkip = true;
                                break;
                            }
                        }

                        createdBySid = data.item(j).getTextContent().substring(indexOfLastDash + 1);
                    }
                } else if (attributes.item(0).getNodeValue().equals("SubjectUserName")) {
                    createdByUname = data.item(j).getTextContent();
                }

            }
            if (!toSkip) userArrayList.add(new User(sid, username, createdBySid, createdByUname, timestamp));

        }
        return userArrayList;
    }

    private Timestamp getTimestampFromXML(NodeList childNodes) {
        // Don't ask me, we are doing a travel through the tree :D
        String timestampString = childNodes.item(0).getChildNodes().item(14).getAttributes().getNamedItem("SystemTime").getTextContent();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(timestampString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Timestamp(parsedDate.getTime());
    }

    /**
     * @return Arraylist with, in index 0, the computer's name and, in index 1, Computer id
     * @throws IOException
     */
    private ArrayList<String> getComputerDetails() throws IOException {
        // plz that this xml parse works
        /* This way of doing stuff is just idiot, we need to implement xpath */
        /* I am starting to believe that xpath is just horrible */
        ArrayList<String> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(WORKING_DIR + "/MicrosoftWindowsUserProfileServiceOperational.xml"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                line = line.replace("\t", "");
                if (line.startsWith("<Computer>")) {
                    int index = line.indexOf("<", 10);
                    res.add(0, line.substring(10, index));

                } else if (line.startsWith("<Data Name=\"Key\">")) {
                    String[] split = line.split("-");
                    res.add(1, split[3] + "-" + split[4] + "-" + split[5] + "-" + split[6]);
                }

                if (res.size() == 2) break; // all info collected
            }
        }
        return res;
    }
}
