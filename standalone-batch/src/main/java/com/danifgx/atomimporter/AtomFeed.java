package com.danifgx.atomimporter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Represents an Atom feed.
 * This class is used to parse the atom files.
 */
@JacksonXmlRootElement(localName = "feed")
public class AtomFeed {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "updated")
    private String updated;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entry")
    private List<AtomEntry> entries;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public List<AtomEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<AtomEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "AtomFeed{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", updated='" + updated + '\'' +
                ", entries=" + (entries != null ? entries.size() : 0) +
                '}';
    }
}