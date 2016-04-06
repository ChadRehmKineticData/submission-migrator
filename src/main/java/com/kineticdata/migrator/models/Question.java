package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;

import java.util.Arrays;
import java.util.List;

public class Question {
    public static final String FORM = "KS_SRV_SurveyQuestion";
    public static final int ID = 179;
    public static final int NAME = 700001833;
    public static final int TYPE = 700000002;
    public static final int LIST_TYPE = 700005010;
    public static final int TEMPLATE_ID = 700000850;
    public static final int PARENT_QUESTION_ID = 700000855;
    public static final int ORDER = 700001200;
    private static final String TYPE_LIST = "List";
    private static final List<String> LIST_TYPES = Arrays.asList(
            null, null, "List Box", "Checkbox", "Radio Button");
    private final String id;
    private final String name;
    private final String type;
    private final Integer listType;

    public Question(Entry entry) {
        this.id = (String) entry.get(ID).getValue();
        this.name = (String) entry.get(NAME).getValue();
        this.type = (String) entry.get(TYPE).getValue();
        this.listType = (Integer) entry.get(LIST_TYPE).getValue();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return TYPE_LIST.equals(type) ? LIST_TYPES.get(listType) : type;
    }
}