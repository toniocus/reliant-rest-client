package rest.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@RestController
@RequestMapping("eh")
public class ErrorHdlRestController extends ErrorHdlBaseController {

    @GetMapping("/unexpected")
    public ResponseEntity<String> unexpected() {
        throw new TaUnexpectedException("This is a configuration error");
    }

    @GetMapping("/data")
    public ResponseEntity<String> data() {
        throw new TaDataException("This is a data error");
    }


    @GetMapping("/config")
    public ResponseEntity<String> error() {
        throw new TaConfigException("This is a configuration error");
    }

    @GetMapping("/error")
    public ResponseEntity<String> runtime() {
        throw new RuntimeException("This is a runtime error");
    }

}
