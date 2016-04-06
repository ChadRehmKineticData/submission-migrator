package com.kineticdata.migrator;

import com.bmc.arsys.api.ARServerUser;
import com.kineticdata.migrator.impl.ArsHelper;
import com.kineticdata.migrator.impl.Config;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import static java.lang.String.format;

public class App
{
    private static final String CONFIG_FILE_NAME = "config.yaml";

    public static void main( String[] args ) throws IOException, ParseException {
        Config config = Config.configure(CONFIG_FILE_NAME);
        ARServerUser user = ArsHelper.createUser(config);
        String action = argAt(args, 0);
        if (action == null) {
            System.out.println("An action argument is required: import, export.");
        } else {
            if (action.equals("import")) {
                String directory = argAt(args, 1);
                if (directory != null) {
                    Import.start(config, directory);
                } else {
                    System.out.println("The import action requires a directory argument to be specified.");
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
