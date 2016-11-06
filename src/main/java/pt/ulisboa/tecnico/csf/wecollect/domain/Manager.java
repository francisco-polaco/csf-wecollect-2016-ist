package pt.ulisboa.tecnico.csf.wecollect.domain;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;
import pt.ulisboa.tecnico.csf.wecollect.domain.event.*;
import pt.ulisboa.tecnico.csf.wecollect.domain.teste.People;
import pt.ulisboa.tecnico.csf.wecollect.domain.teste.Person;
import pt.ulisboa.tecnico.csf.wecollect.exception.DirectoryWithoutFilesException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToCreateWorkingDirException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToParseXMLException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToRunPythonException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.Data;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class Manager {

    private static final String CURRENT_LOG_XML_TEST = "/home/xxlxpto/person.xml";
    private static final String WORKING_DIR = "wdir";

    private String _rootFs;

    private boolean _force = false;

    private static Manager mInstance;

    public static Manager getInstance(){
        if(mInstance == null){
            mInstance = new Manager();
        }
        return mInstance;
    }

    private void processEvtx(String rootFs) throws ImpossibleToRunPythonException{
        System.out.println("Starting to process EVTX.");
        if(rootFs.endsWith("/")){ // cleaning the last slash
            System.out.println("Cleaning path");
            rootFs = rootFs.substring(0, rootFs.length() - 1);
        }

        System.out.println("Checking working folder.");
        checkWorkingFolder();

        // EVTX

        String evtxDirPath = rootFs + "C/Windows/System32/winevt/Logs";

        System.out.println("Getting every filename.");
        ArrayList<File> files = getListOfFilenames(evtxDirPath);

        for (File file: files) {
            Process p;
            try {
                System.out.println("Processing file: " + evtxDirPath + "/" + file.getName());
                p = Runtime.getRuntime().exec("python2 extras/evtxdump.pyc " + evtxDirPath + "/" + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                throw new ImpossibleToRunPythonException(e.getMessage());
            }

            try(BufferedReader inOut = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                String filenameXml = file.getName().replace(".evtx", ".xml");
                PrintWriter pw = new PrintWriter(new FileWriter(WORKING_DIR + "/" + filenameXml));

                while ((line = inOut.readLine()) != null) {
                    if(!line.contains("&lt;") || !line.contains("&gt;") || !line.contains("&apos;") || !line.contains("%apos;"))
                        line = line.replaceAll("&", "");

                    if(line.contains("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\""))
                        line = line.replaceAll("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\"", "");

                    String xmlIdent = xmlIdent(line);
                    pw.write(xmlIdent + line + "\n");
                }

                pw.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private String xmlIdent(String line) {
        String xmlIdent = "";
        if(!(line.startsWith("<Events>") || line.startsWith("</Events>") || line.startsWith("<?xml")) ){
            xmlIdent += "\t";
            if(!(line.startsWith("<Event >") || line.startsWith("</Event>"))){
                xmlIdent += "\t";
                if(line.startsWith("<Data ")){
                    xmlIdent += "\t";
                }
            }
        }
        return xmlIdent;
    }

    private void checkWorkingFolder() {
        if(!(new File(WORKING_DIR).exists())) {
            if (!(new File(WORKING_DIR).mkdir())) {
                throw new ImpossibleToCreateWorkingDirException();
            }
        }
    }

    private ArrayList<File> getListOfFilenames(String dirpath) {
        File dir = new File(dirpath);
        if(dir.listFiles() == null){
            throw new DirectoryWithoutFilesException(dirpath);
        }
        ArrayList<File> files = new ArrayList<>(Arrays.asList(dir.listFiles()));

        for (File f : files) {
            String filename = f.getName();
            if(f.isDirectory() || filename.equals(".") || filename.equals("..") ){
                files.remove(f);
            }
        }
        return files;
    }

    private void getClassesFromXML(){
        Unmarshaller jaxbUnmarshaller;
        People p = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(People.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            p = (People) jaxbUnmarshaller.unmarshal(new File(CURRENT_LOG_XML_TEST));
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new ImpossibleToParseXMLException(e.getMessage());
        }
        /*for(Person pe : p.getPersonArrayList()){
            System.out.println(pe.getName() + " " + pe.getAge());
        }*/
    }

    private void testMarshallToXML(){
        People people = new People();
        ArrayList<Person> p = new ArrayList<>();
        p.add(new Person("bata", 12));
        p.add(new Person("patat", 15));
        people.setPersonArrayList(p);


        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(People.class);

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            //Marshal the employees personArrayList in console
            jaxbMarshaller.marshal(people, System.out);

            //Marshal the employees personArrayList in file
            jaxbMarshaller.marshal(people, new File("/home/xxlxpto/people.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void process(String rootPath)  {
        _rootFs = rootPath;
        if(!_force)
            //processEvtx(filepath);
        try {
            getPackReady();
        } catch (IOException | XPathExpressionException e) {
            e.printStackTrace();
        }finally {
            DatabaseManager.getInstance().disconnect();
        }

    }

    private void getPackReady() throws IOException, XPathExpressionException {
        Pack p = new Pack();
        processComputer(p);
        processUsers(p);


        // Events
        processStartupEvents(p);
        processEventLoggerShutdownEvents(p);
        processLoginEvents(p);
        processLogoutEvents(p);
        processFirewallEvents(p);


        for (Event e : p.getEvents()) {
            System.out.println(e.toString());
            e.commitToDb();
        }
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

    private void processStartupEvents(Pack pack) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4608\"]";
        InputSource inputSource = new InputSource(WORKING_DIR + "/Security.xml");
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for(int i = 0 ; i < nodes.getLength() ; i++) {
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

    private void processEventLoggerShutdownEvents(Pack pack) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"1100\"]";
        InputSource inputSource = new InputSource(WORKING_DIR + "/Security.xml");
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for(int i = 0 ; i < nodes.getLength() ; i++) {
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            Timestamp timestamp = getTimestampFromXML(childNodes);
            pack.addEvent(new ShutdownEvent(timestamp, pack.getComputer().getId()));
        }

    }

    private void processLogoutEvents(Pack pack) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4634\"]";
        InputSource inputSource = new InputSource(WORKING_DIR + "/Security.xml");
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for(int i = 0; i < nodes.getLength() ; i++){
            String sid = "";
            String logonId = "";
            String loginType = "";
            Timestamp timestamp = null;
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            if(childNodes.item(2) == null) continue;
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for(int j = 0 ; j < data.getLength() ; j+=2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserSid")) {
                    if(!(data.item(j).getTextContent().length() > 8)) { // special sids
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    sid = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("TargetLogonId")) {
                    logonId = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("LogonType")) {
                    loginType = data.item(j).getTextContent();
                }

            }
            if(!toSkip) {
                try {
                    pack.addEvent(new LogoutUserEvent(timestamp, pack.getComputer().getId(), Pack.getInstance().getUserIdBySid(sid), sid,
                            new BigInteger(logonId.substring(2), 16).longValue(), Short.parseShort(loginType)));
                }catch (IllegalStateException e){
                    //System.err.println("User id of this logout event was not found.");
                }
            }

        }
    }

    private void processLoginEvents(Pack pack) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/Events/Event/System/EventID[text()=\"4624\"]";
        InputSource inputSource = new InputSource(WORKING_DIR + "/Security.xml");
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);


        for(int i = 0; i < nodes.getLength() ; i++){
            String sid = "";
            String logonId = "";
            String loginType = "";
            Timestamp timestamp = null;
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            if(childNodes.item(2) == null) continue;
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();

            // Timestamp
            timestamp = getTimestampFromXML(childNodes);


            for(int j = 0 ; j < data.getLength() ; j+=2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserSid")) {
                    if(!(data.item(j).getTextContent().length() > 8)) { // special sids
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    sid = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("TargetLogonId")) {
                    logonId = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("LogonType")) {
                    loginType = data.item(j).getTextContent();
                }

            }
            if(!toSkip) {
                try {
                    pack.addEvent(new LoginUserEvent(timestamp, pack.getComputer().getId(), Pack.getInstance().getUserIdBySid(sid), sid,
                            new BigInteger(logonId.substring(2), 16).longValue(), Short.parseShort(loginType)));

                } catch (IllegalStateException e) {
                    //System.err.println("User id of this login event was not found.");
                }
            }
        }
    }

    private void processFirewallEvents(Pack pack){
        String fwDirPath = _rootFs + "/C/Windows/System32/LogFiles/Firewall";
        System.out.println("batata " + fwDirPath);

        ArrayList<File> files = getListOfFilenames(fwDirPath);
        System.out.println("cenoura");

        files.removeIf(f -> !(f.getName().endsWith(".log") || ! f.getName().contains(".log.old")));
        System.out.println("maca");

        Collections.sort((List)files, (Comparator<String>) new FileExtensionComparator());

        System.out.println("Ja demos sort dos files");

        if(files.size() > 1) {
            for (int i = 1; i < files.size(); i++) {
                System.out.println("Firewall log.");
                processFirewallFile(files.get(i), pack);
            }
        }
        if(files.size() > 0)
            processFirewallFile(files.get(0), pack); // most recent file
    }

    private void processFirewallFile(File file, Pack pack) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while((line = br.readLine()) != null){
                if ((line.startsWith("1") || line.startsWith("2"))) // should be enough for some years
                {
                    String[] tokens = line.split(" ");
                    boolean allow = false;
                    if(tokens[2].equals("ALLOW")) allow = true;
                    if(!(tokens[3].equals("TCP") || tokens[3].equals("UDP"))) continue;
                    // TODO timestamp
                    try {
                        pack.addEvent(new FirewallEvent(new Timestamp(0), pack.getComputer().getId(), allow, tokens[3], tokens[4], tokens[5],
                                Integer.parseInt(tokens[6]), Integer.parseInt(tokens[7])));
                    }catch (IllegalArgumentException e){
                        continue; // skip this one
                    }

                }
            }
        }catch (IOException e){
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

        for(int i = 0; i < nodes.getLength() ; i++){
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


            for(int j = 0 ; j < data.getLength() ; j+=2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserName")) {
                    if(data.item(j).getTextContent().equals("defaultuser0")) {
                        // We shall not reveal god to the mundane people
                        toSkip = true;
                        break;
                    }
                    username = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("TargetSid")) {
                    int indexOfLastDash = data.item(j).getTextContent().lastIndexOf("-");
                    sid = data.item(j).getTextContent().substring(indexOfLastDash + 1);
                }
                else if (attributes.item(0).getNodeValue().equals("SubjectUserSid")) {
                    int indexOfLastDash = data.item(j).getTextContent().lastIndexOf("-");

                    if(data.item(j).getTextContent().substring(indexOfLastDash + 1).equals("18")){
                        // God created this account, so with will not reveal that god exists
                        createdBySid = null;
                        createdByUname = null;
                        break;
                    }else {

                        for (User u : userArrayList) {
                            if (u.getUserSid().equals(data.item(j).getTextContent().substring(indexOfLastDash + 1))) {
                                userArrayList.add(new User(sid, username, u, timestamp));
                                toSkip = true;
                                break;
                            }
                        }

                        createdBySid = data.item(j).getTextContent().substring(indexOfLastDash + 1);
                    }
                }
                else if (attributes.item(0).getNodeValue().equals("SubjectUserName")) {
                    createdByUname = data.item(j).getTextContent();
                }

            }
            if(!toSkip) userArrayList.add(new User(sid, username, createdBySid, createdByUname, timestamp));

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
        try (BufferedReader br = new BufferedReader(new FileReader(WORKING_DIR + "/user.xml"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                line = line.replace("\t", "");
                if(line.startsWith("<Computer>")){
                    int index = line.indexOf("<", 10);
                    res.add(0, line.substring(10, index));

                }
                else if(line.startsWith("<Data Name=\"Key\">")){
                    String[] split = line.split("-");
                    res.add(1, split[3] + "-" + split[4] + "-" + split[5] + "-" + split[6]);
                }

                if(res.size() == 2) break; // all info collected
            }
        }
        return res;
    }
}
