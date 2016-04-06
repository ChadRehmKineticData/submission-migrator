package com.kineticdata.migrator.models.json;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FormAttribute {
    private static final String NAME = "name";
    private static final String VALUES = "values";
    private final String name;
    private final List<String> values;

    public FormAttribute(JSONObject jsonObject) {
        name = (String) jsonObject.get(NAME);
        // parse the json array to populate the values member, we need to cast each object inside
        // of the json array to a String
        List<String> tempValues = new ArrayList<>();
        for (Object o : (JSONArray) jsonObject.get(VALUES))
            tempValues.add((String) o);
        values = ImmutableList.copyOf(tempValues);
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
