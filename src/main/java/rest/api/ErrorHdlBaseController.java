package rest.api;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class ErrorHdlBaseController {

    private JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    @ExceptionHandler({ TaException.class, TaCheckedException.class})
    public ResponseEntity<JsonNode> handleException(final OMError ex) {

        return ResponseEntity.status(getHttpStatusFromException((Exception) ex).orElse(HttpStatus.INTERNAL_SERVER_ERROR)).body(
            this.jsonFactory.objectNode()
                .put("errorSoruce", ex.getErrorSource())
                .put("errorCode", ex.getErrorCode())
                .put("errorMsg", ex.getErrorMessage())
            );
    }

    @ExceptionHandler({ Exception.class})
    public ResponseEntity<JsonNode> handleException(final Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                this.jsonFactory.objectNode()
                    .put("errorSoruce", "om")
                    .put("errorCode", "unexpected")
                    .put("errorMsg", ex.getMessage())
                );
    }

    protected Optional<HttpStatus> getHttpStatusFromException(final Exception ex) {

        ResponseStatus rStatus = ex.getClass().getAnnotation(ResponseStatus.class);

        if (rStatus != null) {
            return Optional.ofNullable(rStatus.code());
        }

        return Optional.empty();
    }
}
