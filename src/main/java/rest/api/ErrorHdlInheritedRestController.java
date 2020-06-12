package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@RestController
@RequestMapping("eh2")
public class ErrorHdlInheritedRestController extends ErrorHdlBaseController {

    @Override
    @ExceptionHandler({ TaException.class, TaCheckedException.class})
    public ResponseEntity<JsonNode> handleException(final OMError ex) {

        return ResponseEntity.status(getHttpStatusFromException((Exception) ex).orElse(HttpStatus.INTERNAL_SERVER_ERROR)).body(
            JsonNodeFactory.instance.objectNode()
                .put("taErrorSoruce", ex.getErrorSource())
                .put("taErrorCode", ex.getErrorCode())
                .put("taErrorMsg", ex.getErrorMessage())
            );
    }

    @GetMapping("/unexpected")
    public ResponseEntity<String> unexpected() {
        throw new TaUnexpectedException("This is a configuration error");
    }

    @GetMapping("/checked")
    public ResponseEntity<String> checked() throws TaCheckedException {
        throw new TaCheckedException("This is a checked error")
            .withErrorSource(OMErrorSourceEnum.OM)
            .withErrorCode("CHECKED_ERROR");
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
