package com.kineticdata.migrator.impl;

import java.io.File;
import java.util.Arrays;

import static java.lang.String.format;

public class Utils {

    public static File createDirectory(File parent, String... paths) {
        // split the arguments into first/rest because this is a recursive function
        String first = paths[0];
        String[] rest = Arrays.copyOfRange(paths, 1, paths.length);
        // create the directory file instance for the current argument, note that if a parent file
        // was passed we use that in the constructor, there will be no parent for the first
        // recursive call
        File directory = parent == null ? new File(first) : new File(parent, first);
        // check to see if the directory already exists, if it does and it is not a file raise an
        // error, if it does not exist we attempt to create it and raise an error if that fails
        if (directory.exists()) {
            if (!directory.isDirectory())
                throw new RuntimeException(format("Expected %s to be a directory.", directory.getPath()));
        } else {
            if (!directory.mkdir())
                throw new RuntimeException(format("Unable to create directory %s.", directory.getPath()));
        }
        // if rest is empty we return the current directory otherwise we return the result of the
        // the recursive call made with rest
        return (rest.length == 0) ? directory : createDirectory(directory, rest);
    }

    public static File createDirectory(String... paths) {
        return createDirectory(null, paths);
    }
}
