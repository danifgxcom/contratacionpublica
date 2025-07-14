package com.danifgx.contratacionpublica.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Class representing the root element of an Atom feed.
 * This is used for parsing the .atom files.
 */
@Data
@JacksonXmlRootElement(localName = "feed")
public class AtomFeed {

    @JacksonXmlProperty(localName = "author")
    private AtomAuthor author;

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "updated")
    private LocalDateTime updated;

    @JacksonXmlProperty(localName = "link")
    private AtomLink link;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entry")
    private List<AtomEntry> entries;

    /**
     * Class representing a link in an Atom feed.
     */
    @Data
    public static class AtomLink {
        @JacksonXmlProperty(isAttribute = true, localName = "href")
        private String href;

        @JacksonXmlProperty(isAttribute = true, localName = "rel")
        private String rel;
    }

    /**
     * Class representing an author in an Atom feed.
     */
    @Data
    public static class AtomAuthor {
        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "uri")
        private String uri;

        @JacksonXmlProperty(localName = "email")
        private String email;
    }
}
