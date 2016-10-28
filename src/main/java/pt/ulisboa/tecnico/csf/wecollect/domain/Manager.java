package pt.ulisboa.tecnico.csf.wecollect.domain;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ulisboa.tecnico.csf.wecollect.domain.teste.People;
import pt.ulisboa.tecnico.csf.wecollect.domain.teste.Person;
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

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class Manager {

    private static final String CURRENT_LOG_XML = "currentLog.xml";
    private static final String CURRENT_LOG_XML_TEST = "/home/xxlxpto/person.xml";
    private static Manager mInstance;

    public static Manager getInstance(){
        if(mInstance == null){
            mInstance = new Manager();
        }
        return mInstance;
    }

    private void processEvtx(String filepath) throws ImpossibleToRunPythonException{
        String line;
        Process p;

        try {
            System.err.println(filepath);
            p = Runtime.getRuntime().exec("extras/evtxdump.pyc " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImpossibleToRunPythonException(e.getMessage());
        }

        try(BufferedReader inOut = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {

            PrintWriter pw = new PrintWriter(new FileWriter(CURRENT_LOG_XML));
            while ((line = inOut.readLine()) != null) {

                if(!line.contains("&lt;") || !line.contains("&gt;") || !line.contains("&apos;") || !line.contains("%apos;"))
                    line = line.replaceAll("&", "");

                if(line.contains("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\""))
                    line = line.replaceAll("xmlns=\"http://schemas.microsoft.com/win/2004/08/events/event\"", "");
                pw.write(line + "\n");
            }
            pw.close();

        }catch (IOException e){
            e.printStackTrace();
        }

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
        processEvtx(filepath);
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


        String computerId = getComputerId();
        Computer c = new Computer();
        c.setSid(computerId);
        System.out.println(computerId);

        String computerName = getComputerName();
        c.setName(computerName);
        System.out.println(computerName);

        p.setComputer(c);







/*
        XPath xpath = XPathFactory.newInstance().newXPath();
        String responseStatus = xpath.evaluate("/*//*[local-name()='ResponseStatus']/text()", document);
        System.out.println("-> " + responseStatus);

*/




        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//Version";
        InputSource inputSource = new InputSource(CURRENT_LOG_XML);
        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
        System.out.println("BATATA");
        if(nodes.getLength() > 0){
            System.out.println("Doce");
            for (int i = 0 ; i < nodes.getLength() ; i++) {
                Node n = nodes.item(i);
                System.out.println("OLA: " + n.toString());
            }
        }
        //<Data Name="Key"
    }


    /* This way of doing stuff is just idiot, we need to implement xpath */

    private String getComputerId() throws IOException {
        // plz that this xml parse works
        String computerId = "";
        try (BufferedReader br = new BufferedReader(new FileReader(CURRENT_LOG_XML))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                if(line.startsWith("<Data Name=\"Key\">")){
                    String[] split = line.split("-");
                    computerId = split[3] + "-" + split[4] + "-" + split[5] + "-" + split[6];

                }
            }
        }
        return computerId;
    }

    private String getComputerName() throws IOException {
        // plz that this xml parse works
        String computerName = "";
        try (BufferedReader br = new BufferedReader(new FileReader(CURRENT_LOG_XML))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                if(line.startsWith("<Computer>")){
                    int index = line.indexOf("<", 10);
                    computerName = line.substring(10, index);

                }
            }
        }
        return computerName;
    }
}
