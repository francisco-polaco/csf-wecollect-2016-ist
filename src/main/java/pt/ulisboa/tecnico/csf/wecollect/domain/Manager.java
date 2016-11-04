package pt.ulisboa.tecnico.csf.wecollect.domain;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class Manager {

    private static final String CURRENT_LOG_XML = "currentLog.xml";
    private static final String CURRENT_LOG_XML_TEST = "/home/xxlxpto/person.xml";
    private static final String WORKING_DIR = "wdir";

    private static Manager mInstance;

    public static Manager getInstance(){
        if(mInstance == null){
            mInstance = new Manager();
        }
        return mInstance;
    }

    private void processEvtx(String dirpath) throws ImpossibleToRunPythonException{
        System.out.println("Starting to process EVTX.");
        if(dirpath.endsWith("/")){ // cleaning the last slash
            System.out.println("Cleaning path");
            dirpath = dirpath.substring(0, dirpath.length() - 1);
        }

        System.out.println("Getting every filename.");
        ArrayList<File> files = getListOfFilenames(dirpath);

        System.out.println("Checking working folder.");
        checkWorkingFolder();

        for (File file: files) {
            Process p;
            try {
                System.out.println("Processing file: " + dirpath + "/" + file.getName());
                p = Runtime.getRuntime().exec("python2 extras/evtxdump.pyc " + dirpath + "/" + file.getName());
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

    public void process(String filepath)  {
        //processEvtx(filepath);
        try {
            getPackReady();
        } catch (IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        /*getClassesFromXML();
        DatabaseManager.getInstance().commitNewLogs();*/


    }

    private void getPackReady() throws IOException, XPathExpressionException {
        Pack p = new Pack();


        Computer c = new Computer();

        ArrayList<String> computerDetails = getComputerDetails();
        c.setSid(computerDetails.get(1));
        System.out.println(computerDetails.get(1));
        c.setName(computerDetails.get(0));
        System.out.println(computerDetails.get(0));

        p.setComputer(c);

        DatabaseManager.getInstance().commitComputer(p);

        ArrayList<User> userArrayList = getUsers();
        p.setUsers(userArrayList);

        for (User u: userArrayList) {
            System.out.println(u);
        }
        //<Data Name="Key"
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
            boolean toSkip = false;


            // TAG Event
            NodeList childNodes = nodes.item(i).getParentNode().getParentNode().getChildNodes();
            // TAG EventData
            NodeList data = childNodes.item(2).getChildNodes();
            for(int j = 0 ; j < data.getLength() ; j+=2) {
                NamedNodeMap attributes = data.item(j).getAttributes();

                if (attributes.item(0).getNodeValue().equals("TargetUserName")) {
                    username = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("TargetSid")) {
                    sid = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("SubjectUserSid")) {
                    for (User u: userArrayList) {
                        if(u.getUserSid().equals(data.item(j).getTextContent())){
                            userArrayList.add(new User(sid, username, u));
                            toSkip = true;
                            break;
                        }
                    }
                    createdBySid = data.item(j).getTextContent();
                }
                else if (attributes.item(0).getNodeValue().equals("SubjectUserName")) {
                    createdByUname = data.item(j).getTextContent();
                }

            }
            if(!toSkip) userArrayList.add(new User(sid, username, createdBySid, createdByUname));

        }
        return userArrayList;
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
