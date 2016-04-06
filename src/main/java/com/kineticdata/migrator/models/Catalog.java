package com.kineticdata.migrator.models;

import com.bmc.arsys.api.Entry;

public class Catalog {
    public static final String FORM = "KS_SRV_Category";
    public static final int NAME = 600000500;
    private final String name;

    public Catalog(Entry entry) {
        this.name = (String) entry.get(NAME).getValue();
    }

    public String getName() {
        return name;
    }
}