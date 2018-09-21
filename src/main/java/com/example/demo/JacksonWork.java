package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.io.DefaultResourceLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class JacksonWork {

    /**
     * Constructor.
     */
    public JacksonWork() {
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        new JacksonWork().jsonpath();
    }

    /**
     * Run.
     *
     * @throws Exception the exception
     */
    public void run() throws Exception {

        JsonNode node = readJson("json/jsonFind.json");

        System.out.println("Object ----------------------------");
        System.out.println(node.findParent("att01"));
        System.out.println(node.findParents("att01"));

        System.out.println(node.findValue("att04"));
        System.out.println(node.findValues("att04"));

        JsonNode list = node.path("list");

        System.out.println("List ----------------------------");
        System.out.println(list.findParent("att01"));
        System.out.println(list.findParents("att01"));

        System.out.println(list.findValue("att04"));
        System.out.println(list.findValues("att04"));

        JsonNodeFactory factory = JsonNodeFactory.instance;

        // Setting a missing node as a node value will generate
        // a BAD JSON !!!!!!!!!!!!!
        JsonNode mnode = factory.objectNode().set("missing", MissingNode.getInstance());
        String strangeJson = mnode.toString();
        System.out.println("BAD JSON:" + strangeJson);

    }

    /**
     * Jsonpath.
     *
     * @throws Exception the exception
     */
    protected void jsonpath() throws Exception {

        JsonNode node = readJson("json/person.json");

        Configuration config = new ConfigurationBuilder()
                .jsonProvider(new JsonpathProvider())
                .build();

        DocumentContext doc = JsonPath.using(config).parse(node);

        ArrayNode read = doc.read("$..[?(@.postalCode)]", ArrayNode.class);

        AtomicInteger count = new AtomicInteger(100);

        read.forEach(n -> {

            ((ObjectNode) n).put("postalCode", count.getAndAdd(100));
        });

        System.out.println(doc.jsonString());
        System.out.println(node);

        System.out.println("SON IGUALES: " + (node.toString().equals(doc.jsonString())));
        System.out.println("SON IGUALES: " + (node.equals(doc.json())));

    }


    // ============================================================================
    //   ### - Utility
    // ============================================================================

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Read json.
     *
     * @param resourceName the resource name
     * @return the json node
     * @throws JsonProcessingException the json processing exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private JsonNode readJson(final String resourceName) throws JsonProcessingException, IOException {

        try (InputStream is = new DefaultResourceLoader()
                .getResource("classpath:" + resourceName)
                .getInputStream()) {

            return this.mapper.readTree(is);

        }

    }

}
