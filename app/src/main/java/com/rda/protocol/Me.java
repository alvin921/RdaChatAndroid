package com.rda.protocol;

import java.util.ArrayList;

/**
 * Created by mingangwang on 2016/7/21.
 */
public class Me extends Person {
    private ArrayList<String> TopicList = new ArrayList<String>();

    public Me(String id, String name, String type){
        super(id,name,type);
    }

    public Me setTopicList(ArrayList<String> list) { this.TopicList = list; return this; }
    public boolean addTopic(String topic) {
        return this.TopicList.add(topic);
    }
    public boolean hasTopic(String topic) {
        return this.TopicList.contains(topic);
    }
    public boolean delTopic(String topic) {
        return this.TopicList.remove(topic);
    }

    public String getTopic(int pos) {
        return this.TopicList.get(pos);
    }
    public int numTopic() {
        return this.TopicList.size();
    }
}
