package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;

public class Template {
    public static final String FORM = "KS_SRV_SurveyTemplate";
    public static final int ID = 179;
    public static final int NAME = 700001000;
    public static final int CATALOG = 600000500;
    private final String catalog;
    private final String id;
    private final String name;

    public Template(Entry entry) {
        this.catalog = (String) entry.get(CATALOG).getValue();
        this.id = (String) entry.get(ID).getValue();
        this.name = (String) entry.get(NAME).getValue();
    }

    public String getCatalog() {
        return catalog;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}