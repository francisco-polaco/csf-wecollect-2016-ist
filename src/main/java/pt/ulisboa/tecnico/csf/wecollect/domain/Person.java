package pt.ulisboa.tecnico.csf.wecollect.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by xxlxpto on 21-10-2016.
 */
@XmlRootElement(name = "person")
public class Person {
    private String mName;

    private int mAge;

    public Person(String mName, int mAge) {
        this.mName = mName;
        this.mAge = mAge;
    }

    public Person() {
    }

    public String getName() {
        return mName;
    }

    @XmlElement
    public void setName(String mName) {
        this.mName = mName;
    }

    public int getAge() {
        return mAge;
    }

    @XmlElement
    public void setAge(int mAge) {
        this.mAge = mAge;
    }

}
