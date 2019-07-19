package rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import rest.client.models.ModelPerson;

/**
 * The Class TaHttpAdapterAbstract.
 */

@RestController
public class ReliantDemoRestMore {

    private static Logger log = LoggerFactory.getLogger(ReliantDemoRestMore.class);
    private JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    @RequestMapping(value = {"/posts/ack"}, method = {RequestMethod.POST}, produces = "application/json")
    @ResponseBody
    public JsonNode ack(@RequestBody final JsonNode body) {
        return this.jsonFactory.objectNode()
                .put("status", "Received")
                .set("request", body);
    }

    @RequestMapping(value = {"/person/{name}"}, method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public ModelPerson getPerson(@PathVariable("name") final String name) {

        return new ModelPerson(name)
                .addAddr("Superi", 2019)
                .addAddr("Echeverria", 3361);
    }

    @RequestMapping(value = {"/person"}, method = {RequestMethod.POST}, produces = "application/json")
    @ResponseBody
    public JsonNode storePerson(@RequestBody final ModelPerson person) {

        System.out.println("Received Person: " + person.toString());
        return this.jsonFactory.objectNode().put("status", "OK");

    }

}