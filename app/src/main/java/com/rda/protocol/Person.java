package com.rda.protocol;

import java.util.ArrayList;

/**
 * Created by mingangwang on 2016/7/21.
 */
public class Person {
    private String ID;
    private String Name;
    private String Type;

    public static final String PERSON_TYPE_CHILD                    = "D";
    public static final String PERSON_TYPE_GUARDER                  = "C";

    public Person(){}

    public Person(String id, String name, String type){
        ID = id;
        Name = name;
        Type = type;
    }

    public Person setID(String id) { this.ID = id; return this; }
    public Person setName(String name) { this.Name = name; return this; }
    public Person setType(String type) { this.Type = type; return this; }

    public String getID() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public String getType() {
        return Type;
    }
}
