package com.danifgx.atomimporter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Component for reading and parsing .atom files.
 */
@Component
public class AtomFileReader {

    private final XmlMapper xmlMapper;

    public AtomFileReader() {
        this.xmlMapper = new XmlMapper();
        // Configure the mapper for handling XML namespaces
        this.xmlMapper.setDefaultUseWrapper(false);
        // Register the JavaTimeModule to handle Java 8 date/time types
        this.xmlMapper.registerModule(new JavaTimeModule());
        // Configure the mapper to ignore unknown properties
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Read and parse an .atom file into an AtomFeed object.
     *
     * @param file the .atom file to read
     * @return the parsed AtomFeed object, or null if an error occurs
     */
    public AtomFeed readAtomFile(File file) {
        try {
            return xmlMapper.readValue(file, AtomFeed.class);
        } catch (IOException e) {
            System.err.println("Error reading .atom file: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}