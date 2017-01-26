package com.kineticdata.migrator;

import com.bmc.arsys.api.ARServerUser;
import com.kineticdata.migrator.impl.ArsHelper;
import com.kineticdata.migrator.impl.Config;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;

public class App
{
    public static final String CONFIG_FILE = "config.yaml";
    public static final String SUBMISSION_CSV_FILE = "submissions.csv";
    public static final String ID_CSV_FILE = "ids.csv";
    public static final String TEMPLATE_YAML_FILE = "template.yaml";
    public static final String ATTACHMENT_DIR = "attachments";
    public static final String VERSION = "1.0.1";

    public static void main( String[] args ) throws IOException, ParseException {
        Config config = Config.configure(CONFIG_FILE);
        ARServerUser user = ArsHelper.createUser(config);
        String action = argAt(args, 0);
        if (action == null) {
            System.out.println("An action argument is required: import, export.");
        } else {
            if (action.equals("import")) {
                for (String arg : Arrays.copyOfRange(args, 1, args.length)) {
                    Import.start(config, arg);
                }
            } else if (action.equals("export")) {
                String catalog = argAt(args, 1);
                String template = argAt(args, 2);
                if (catalog != null) {
                    if (template != null) {
                        Export.export(config, user, catalog, template);
                    } else {
                        Export.export(config, user, catalog);
                    }
                } else {
                    Export.export(config, user);
                }
            } else if (action.equals("version")) {
                System.out.println(VERSION);
            } else {
                System.out.println(format("Invalid action argument specified: %s", action));
            }
        }
    }

    /**
     * Helper method that safely accesses the argument at the given index.  If the index would be
     * out-of-bounds we return null.
     *
     * @param args
     * @param i
     * @return
     */
    private static String argAt(String[] args, int i) {
        return (args.length > i) ? args[i] : null;
    }
}
