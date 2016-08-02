package com.rda.protocol;

import java.util.ArrayList;

/**
 * Created by mingangwang on 2016/7/21.
 */



public class Topic {
    private String Name;
    private String Creator;
    private ArrayList<Member> List = new ArrayList<Member>();

    public class Member {
        private String    ID;
        private String    Name;
        private String    Type;

        public Member(String id, String name, String type){
            ID = id;
            Name = name;
            Type = type;
        }

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

    public Topic(String name, String creator){
        Name = name;
        Creator = creator;
    }

    public String getName() {
        return Name;
    }
    public String getCreator() {
        return Creator;
    }

    public Member newMember(String id, String name, String type) {
        Member m = new Member(id, name, type);
        return m;
    }

    public boolean addMember(String id, String name, String type) {
        Member m = new Member(id, name, type);
        return this.List.add(m);
    }

    public boolean addMember(Member m) {
        return this.List.add(m);
    }
    public boolean hasMember(String id) {
        int size = List.size();
        for(int i = 0; i < size; i++){
            Member m = getMember(i);
            if(id.equals(m.getID())){
                return true;
            }
        }
        return false;
    }
    public boolean delMember(String id) {
        int size = numMember();
        for(int i = 0; i < size; i++){
            Member m = getMember(i);
            if(id.equals(m.getID())){
                List.remove(i);
                return true;
            }
        }
        return false;
    }

    public Member getMember(int pos) {
        return this.List.get(pos);
    }
    public int numMember() {
        return this.List.size();
    }
}
