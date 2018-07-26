package com.kineticdata.migrator.impl;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {
    private final String reUsername;
    private final String rePassword;
    private final String reServer;
    private final Integer rePort;
    private final Integer reQueryLimit;
    private final String reQualification;
    private final String ceUrl;
    private final String ceSpaceSlug;
    private final String ceKappSlug;
    private final String ceUsername;
    private final String cePassword;

    public Config(Map<String,Map<String,Object>> map) {
        Map<String,Object> reConfig = map.get("Request RE");
        reServer = (String) reConfig.get("server");
        rePort = (Integer) reConfig.get("port");
        reUsername = (String) reConfig.get("username");
        rePassword = (String) reConfig.get("password");
        reQueryLimit = (Integer) reConfig.get("query_limit");
        reQualification = (String) reConfig.get("qualification");
        Map<String,Object> ceConfig = map.get("Request CE");
        ceUrl = (String) ceConfig.get("url");
        ceSpaceSlug = (String) ceConfig.get("space");
        ceKappSlug = (String) ceConfig.get("kapp");
        ceUsername = (String) ceConfig.get("username");
        cePassword = (String) ceConfig.get("password");
    }

    public String getReUsername() {
        return reUsername;
    }

    public String getRePassword() {
        return rePassword;
    }

    public String getReServer() {
        return reServer;
    }

    public Integer getRePort() {
        return rePort;
    }

    public Integer getReQueryLimit() {
        return reQueryLimit;
    }

    public String getQualification() {
        return reQualification;
    }

    public String getCeUrl() {
        return ceUrl;
    }

    public String getCeSpaceSlug() {
        return ceSpaceSlug;
    }

    public String getCeKappSlug() {
        return ceKappSlug;
    }

    public String getCeUsername() {
        return ceUsername;
    }

    public String getCePassword() {
        return cePassword;
    }

    public static Config configure(String filename) throws IOException {
        Yaml yaml = new Yaml();
        File file = new File(filename);
        Map<String,Map<String,Object>> data;
        if (file.exists()) {
            data = (Map<String,Map<String,Object>>) yaml.load(new FileReader(file));
        } else {
            Scanner scanner = new Scanner(System.in);
            data = new LinkedHashMap<String,Map<String,Object>>() {{
                System.out.println("The following configuration items are used to connect to the " +
                        "Kinetic Request RE environment from which we will export data.");
                put("Request RE", new LinkedHashMap<String,Object>() {{
                    put("server", gets(scanner, "Server"));
                    put("port", Integer.parseInt(gets(scanner, "Port Number")));
                    put("username", gets(scanner, "Username"));
                    put("password", gets(scanner, "Password"));
                    put("query_limit", Integer.parseInt(gets(scanner, "Query Limit")));
                }});
                System.out.println("The following configuration items are used to connect to the " +
                        "Kinetic Request CE environment to which we will import data.");
                put("Request CE", new LinkedHashMap<String,Object>() {{
                    put("url", gets(scanner, "URL"));
                    put("space", gets(scanner, "Space Slug"));
                    put("kapp", gets(scanner, "Kapp Slug"));
                    put("username", gets(scanner, "Username"));
                    put("password", gets(scanner, "Password"));
                }});
            }};
            // save the yaml configuration file
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(yaml.dumpAsMap(data));
            }
        }
        // instantiate and return the Config instance
        return new Config(data);
    }

    private static String gets(Scanner scanner, String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }
}
