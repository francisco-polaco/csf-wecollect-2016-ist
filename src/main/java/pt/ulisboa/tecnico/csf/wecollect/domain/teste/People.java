package pt.ulisboa.tecnico.csf.wecollect.domain.teste;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by xxlxpto on 21-10-2016.
 */
@XmlRootElement(name = "people")
public class People {
    ArrayList<Person> personArrayList = new ArrayList<>();

    public ArrayList<Person> getPersonArrayList() {
        return personArrayList;
    }

    @XmlElement(name = "person")
    public void setPersonArrayList(ArrayList<Person> list) {
        this.personArrayList = list;
    }
}
