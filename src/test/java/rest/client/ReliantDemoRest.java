package rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * The Class TaHttpAdapterAbstract.
 */

@RestController
public class ReliantDemoRest {

    private static Logger log = LoggerFactory.getLogger(ReliantDemoRest.class);
    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    @RequestMapping(value = {"/ok"}, method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public JsonNode ok() {
        return this.jsonFactory.objectNode().put("status", "OK");
    }

    @RequestMapping(value = {"/empty"}, method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> empty() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/timeout/{seconds}"}, method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public JsonNode timeout(@PathVariable("seconds") final int timeout) throws InterruptedException {
        Thread.sleep(timeout*1000);
        return this.jsonFactory.objectNode().put("status", "OK").put("timeout", timeout + "s");
    }

    @RequestMapping(value = {"/noJson"}, method = {RequestMethod.GET})
    @ResponseBody
    public String noJson() throws InterruptedException {
        return "<root><data>This is not a JSON</data></root>";
    }

    @RequestMapping(value = {"/noJsonError"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> noJsonError() throws InterruptedException {
        return ResponseEntity.badRequest()
                .body("<root><data>This is not a JSON</data></root>");
    }

    @RequestMapping(value = {"/status100"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> status100() throws InterruptedException {
        return ResponseEntity.status(HttpStatus.CONTINUE)
                .body("<root><data>Continue</data></root>");
    }

    @RequestMapping(value = {"/status102"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> status102() throws InterruptedException {
        return ResponseEntity.status(HttpStatus.PROCESSING)
                .body("<root><data>Processing</data></root>");
    }

    @RequestMapping(value = {"/status300"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> status300() throws InterruptedException {
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .body("<root><data>Redirect</data></root>");
    }

}