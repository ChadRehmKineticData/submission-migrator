package com.kineticdata.migrator.models.json;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Form {
    private static final String NAME = "name";
    private static final String SLUG = "slug";
    private static final String ATTRIBUTES = "attributes";
    private final String name;
    private final String slug;
    private final List<FormAttribute> attributes;

    public Form(JSONObject jsonObject) {
        name = (String) jsonObject.get(NAME);
        slug = (String) jsonObject.get(SLUG);
        // get the attributes property and for each one instantiate a FormAttribute object
        List<FormAttribute> tempAttributes = new ArrayList<>();
        for (Object o : (JSONArray) jsonObject.get(ATTRIBUTES))
            tempAttributes.add(new FormAttribute((JSONObject) o));
        // set the attributes member to an immutable copy of the attributes constructed above
        attributes = ImmutableList.copyOf(tempAttributes);
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public List<FormAttribute> getAttributes() {
        return attributes;
    }

    public FormAttribute getAttribute(String attributeName) {
        return attributes.stream()
                .filter(formAttribute -> formAttribute.getName().equals(attributeName))
                .findFirst().orElse(null);
    }
}
