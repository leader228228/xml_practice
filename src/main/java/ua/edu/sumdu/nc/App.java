package ua.edu.sumdu.nc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXParseException;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class App {

    static {
        configureLogger();
        LOGGER = Logger.getLogger(App.class.getSimpleName());
    }

    private static final Logger LOGGER;
    public static void main( String[] args ) throws SAXParseException {
        configureLogger();
        if (!validate(args)) {
            printRequirements();
            return;
        }
        File input = new File(args[0]);
        File output = new File(args[1]);
        XMLCorrector xmlCorrector = new XMLStudentsGradeCorrectorImpl(input, output);
        try {
            xmlCorrector.correct();
        } catch (Exception e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error(e);
            }
        }
    }

    private static void configureLogger() {
        try {
            System.setProperty("logfile", new File(new File(App.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParentFile(), "log.log").getAbsolutePath());
            PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  The method validates the input string. The requirements are below:
     *      1) No spaces in filepathes
     *      2) Existing files
     *      3) Only 2 filepathes
     * */
    private static boolean validate(String [] args) {
        if (args.length != 2) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : args) {
                    stringBuilder.append(s);
                }
                LOGGER.error("Wrong input format: " + stringBuilder);
            }
            printRequirements();
        }
        File input = new File(args[0]);
        if (!input.isFile()) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("Unable to find the file denoted by the " + input.getAbsolutePath());
            }
            return false;
        }
        File output = new File(args[1]);
        try {
            if (!output.isFile() && !output.createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("Unable to create a file", e);
            }
            return false;
        }
        return true;
    }

    private static void printRequirements() {
        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("The requirements for the input parameters are below:\n" +
                    "       1) No spaces in filepathes\n" +
                    "       2) Existing files\n" +
                    "       3) Only 2 filepathes");
        }
    }
}