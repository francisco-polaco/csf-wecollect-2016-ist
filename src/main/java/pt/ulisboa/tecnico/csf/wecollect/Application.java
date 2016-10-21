package pt.ulisboa.tecnico.csf.wecollect;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by xxlxpto on 08-10-2016.
 */
public class Application {

    public static void main(String[] args){
        System.out.println("Hello CSF!");
        Unmarshaller jaxbUnmarshaller = null;
        Person person = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Person.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
             person = (Person) jaxbUnmarshaller.unmarshal(new File("/home/xxlxpto/person.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        System.out.println("Pessoa: " + person.getName() + " Idade: " + person.getAge());
    }
}
