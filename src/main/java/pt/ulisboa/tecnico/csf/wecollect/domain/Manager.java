package pt.ulisboa.tecnico.csf.wecollect.domain;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToParseXMLException;
import pt.ulisboa.tecnico.csf.wecollect.exception.ImpossibleToRunPythonException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
           // p = Runtime.getRuntime().exec("extras/evtxdump.pyc " + filepath + " > " + CURRENT_LOG_XML);
            p = Runtime.getRuntime().exec("extras/evtxdump.pyc ");
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImpossibleToRunPythonException(e.getMessage());
        }
        BufferedReader inError = new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
        BufferedReader inOut = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
        try {
            System.out.println("ERROR:");
            while ((line = inError.readLine()) != null) {
                System.out.println(line);
            }
            inError.close();
            System.out.println("OUT:");
            while ((line = inOut.readLine()) != null) {
                System.out.println(line);
            }
            inOut.close();
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
        getClassesFromXML();
        DatabaseManager.getInstance().commitNewLogs();


    }
}
