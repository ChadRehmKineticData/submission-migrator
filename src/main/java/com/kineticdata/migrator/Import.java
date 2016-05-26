package com.kineticdata.migrator;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.kineticdata.migrator.impl.Config;
import com.kineticdata.migrator.models.json.Form;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class Import {
    private static final String TEMPLATE_NAME_ATTR = "re template name";
    private static final String QUESTION_MAP_ATTR = "re to ce question map";

    public static void start(Config config, String directory) throws IOException, ParseException {
        // get the specified data directory and validate that it exists and is a directory, also
        // get the submissions and template files from that directory
        File dataDir = newDirectory(directory);
        File submissionsCsv = newFile(dataDir, App.SUBMISSION_CSV_FILE);
        File templateYaml = newFile(dataDir, App.TEMPLATE_YAML_FILE);

        // prepare the ids file, we create it if it does not already exist and we instantiate a
        // reader and writer for this file
        File idsFile = new File(directory, App.ID_CSV_FILE);
        idsFile.createNewFile();
        BufferedReader idsReader = new BufferedReader(new FileReader(idsFile));
        PrintWriter idsWriter = new PrintWriter(new BufferedWriter(new FileWriter(idsFile, true)));
        boolean checkForExisting = true;

        // get the necessary information from the template yaml file, this includes the original
        // template name which we will use to find the correct form slug for importing, it also
        // tells us what the question types were so we can handle checkbox and attachment values
        // appropriate when importing
        Yaml yaml = new Yaml();
        HashMap<String,Object> templateData = (HashMap<String, Object>) yaml.load(new FileReader(templateYaml));
        String originalName = (String) templateData.get("template_name");
        Map<String,String> questionTypes = ((List<Map>) templateData.get("questions")).stream()
                .collect(Collectors.toMap(
                        questionMap -> (String) questionMap.get("name"),
                        questionMap -> (String) questionMap.get("type")));

        Form form = getForms(config).stream()
                .filter(f -> f.getAttribute(TEMPLATE_NAME_ATTR) != null &&
                             f.getAttribute(TEMPLATE_NAME_ATTR).getValues().contains(originalName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find form with original name of " + originalName));

        String questionMapString = Optional.ofNullable(form.getAttribute(QUESTION_MAP_ATTR))
            .map(formAttribute -> formAttribute.getValues().get(0))
            .orElseThrow(() -> new RuntimeException("Form did not configure question map attribute."));

        // the keys of the map below are the question names in RE and the values of the map are the
        // field names in CE
        // note that we cast to a map of string to objects because JSON values can be more than just
        // strings, when we use this value later we have to explicitly cast it which will raise an
        // exception, if we blindly cast that to a string here its possible we can insert a
        // non-string value into string collections
        Map<String,Object> questionMap = (Map<String,Object>) parse(questionMapString);
        try (CSVParser csvParser = new CSVParser(new FileReader(submissionsCsv), CSVFormat.DEFAULT)) {
            List<CSVRecord> records = csvParser.getRecords();
            CSVRecord header = records.get(0);
            for (CSVRecord record : records.subList(1, records.size())) {
                // build a map that represents the current submission, the values from the header
                // used as the keys in the map
                Map<String,String> submission = IntStream.range(0, header.size())
                        .mapToObj(Integer::valueOf)
                        .collect(Collectors.toMap(header::get, record::get));
                // compute the values list to be saved by iterating through the question map and
                // getting the value from the submission above.  Note that there is some special
                // processing necessary for attachment and checkbox fields
                Map<String,String> values = new HashMap<>();
                for(Map.Entry<String,Object> entry: questionMap.entrySet()) {
                    String questionName = entry.getKey();
                    String fieldName = (String) entry.getValue();
                    String valueString = submission.get(questionName);
                    if (questionTypes.get(questionName).equals("Attachment")) {
                        valueString = Strings.isNullOrEmpty(valueString)
                                ? "[]"
                                : uploadFile(config, dataDir, form.getSlug(), submission.get("Instance Id"), valueString);
                    } else if (questionTypes.get(questionName).equals("Checkbox")) {
                        valueString = Strings.isNullOrEmpty(valueString)
                                ? "[]"
                                : JSONArray.toJSONString(Arrays.asList(valueString.split("\\s*,\\s*")));
                    }
                    values.put(fieldName, valueString);
                }
                // compute the core state which is derived from two columns in the old submission
                String coreState = "Completed".equals(submission.get("Status"))
                        ? "Closed".equals(submission.get("Request Status")) ? "Closed" : "Submitted"
                        : "Draft";
                Map<String,Object> data = new HashMap<String,Object>() {{
                    put("values", values);
                    put("coreState", coreState);
                    put("createdAt", submission.get("Created At").replace("Z", ".000Z"));
                    put("createdBy", submission.get("Submitter"));
                    put("updatedAt", submission.get("Updated At").replace("Z", ".000Z"));
                    put("updatedBy", submission.get("Submitter"));
                    if (!coreState.equals("Draft")) {
                        put("submittedAt", submission.get("Submitted At").replace("Z", ".000Z"));
                        put("submittedBy", submission.get("Submitter"));
                    }
                    if (coreState.equals("Closed")) {
                        put("closedAt", submission.get("Closed At").replace("Z", ".000Z"));
                        put("closedBy", submission.get("Submitter"));
                    }
                }};

                // Here we will check for existing submission ids in the ids file.  Note that we do
                // this until we get a null line back from the file (indicating that there are no
                // more ids).  Once we get a null read we set the flag to false so that we do not
                // attempt to read from the file again and we close the file.  NOTE that this
                // precaution is important because we will start writing new ids to this file and we
                // do not want to accidentally read those here.
                String existingId = null;
                if (checkForExisting) {
                    existingId = idsReader.readLine();
                    if (existingId == null) {
                        checkForExisting = false;
                        idsReader.close();
                    }
                }

                // If there was not an existing id we make the PATCH call to create a new one and
                // get the id from the response and write that to the ids file.  If there was an
                // existing id we PATCH to that to update the existing submission and we do not need
                // to parse the response.
                if (existingId == null) {
                    String response = patchJson(config, submissionsPath(config, form.getSlug()), data);
                    JSONObject responseObject = (JSONObject) parse(response);
                    JSONObject submissionObject = (JSONObject) responseObject.get("submission");
                    String submissionId = (String) submissionObject.get("id");
                    idsWriter.println(submissionId);
                } else {
                    patchJson(config, submissionsPath(config, form.getSlug(), existingId), data);
                }
            }
        }
        idsWriter.close();
    }

    public static List<Form> getForms(Config config) {
        String json = get(config, kappPath(config) + "/forms?include=attributes");
        JSONObject formsObject = (JSONObject) parse(json);
        JSONArray formsArray = (JSONArray) formsObject.get("forms");

        List<Form> forms = new ArrayList<>();
        for (Object o : formsArray)
            forms.add(new Form((JSONObject) o));
        return forms;
    }

    public static String uploadFile(Config config, File dataDir, String formSlug,
                                    String submissionId, String attachmentId) {
        File attachmentDir = newDirectory(dataDir, App.ATTACHMENT_DIR, submissionId, attachmentId);

        File[] files = attachmentDir.listFiles();
        if (files.length != 1)
            throw new RuntimeException("Expected to be exactly one file in attachment dir");

        File attachment = files[0];
        return postFile(config, config.getCeUrl() + "/" + config.getCeSpaceSlug() + "/" +
                config.getCeKappSlug() + "/" + formSlug + "/files", attachment);
    }

    public static String get(Config config, String url) {
        HttpGet httpGet = new HttpGet(url);
        return request(config, httpGet);
    }

    public static String patchJson(Config config, String url, Map<String,Object> data) {
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setEntity(new StringEntity(JSONObject.toJSONString(data), ContentType.APPLICATION_JSON));
        return request(config, httpPatch);
    }

    public static String postFile(Config config, String url, File file) {
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(httpEntity);
        return request(config, httpPost);
    }

    public static String request(Config config, HttpUriRequest httpRequest) {
        // set the basic auth header given the config object
        String credentials = config.getCeUsername() + ":" + config.getCePassword();
        byte[] encodedBytes = Base64.encodeBase64(credentials.getBytes());
        httpRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedBytes));
        // make the request
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpRequest)) {
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                throw new RuntimeException(format("Got %s response: %s",
                        httpResponse.getStatusLine().getStatusCode(),
                        EntityUtils.toString(httpResponse.getEntity())));
            } else {
                return EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (IOException e ) {
            throw new RuntimeException(e);
        }
    }

    public static String submissionsPath(Config config, String formSlug) {
        return apiPath(config) + "/kapps/" + config.getCeKappSlug() + "/forms/" + formSlug + "/submissions";
    }

    public static String submissionsPath(Config config, String formSlug, String id) {
        return apiPath(config) + "/submissions/" + id;
    }

    public static String kappPath(Config config) {
        return apiPath(config) + "/kapps/" + config.getCeKappSlug();
    }

    public static String apiPath(Config config) {
        return config.getCeUrl() + "/" + config.getCeSpaceSlug() + "/app/api/v1";
    }

    public static Object parse (String json) {
        JSONParser jsonParser = new JSONParser();
        try {
            return jsonParser.parse(json);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static File newDirectory(String path) {
        return newDirectory(new File(path));
    }

    public static File newDirectory(File parent, String... paths) {
        return newDirectory(new File(parent, Joiner.on(File.separator).join(paths)));
    }

    public static File newDirectory(File file) {
        if (!file.exists()) {
            throw new RuntimeException(format("The directory %s does not exist.", file.getPath()));
        } else if (!file.isDirectory()) {
            throw new RuntimeException(format("The path %s is not a directory.", file.getPath()));
        } else {
            return file;
        }
    }

    public static File newFile(File parent, String path) {
        return newFile(new File(parent, path));
    }

    public static File newFile(File file) {
        if (!file.exists()) {
            throw new RuntimeException(format("The file %s does not exist.", file.getPath()));
        } else {
            return file;
        }
    }
}
