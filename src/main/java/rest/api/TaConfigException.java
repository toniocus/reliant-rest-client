package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The Class TaConfigException, is a TaException
 * with errorCode set to {@link OMErrorEnum#OM_CONFIG_ERROR} and
 * errorSource set to {@link OMErrorSourceEnum#OM},
 * , and Spring annotation to use HttpStatus 400 when thrown.
 *
 * @author tonioc
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "OM Configuration error")
public class TaConfigException extends TaException {

    private static final long serialVersionUID = 1L;

    {
        withErrorSource(OMErrorSourceEnum.OM);
        withErrorCode(OMErrorEnum.OM_CONFIG_ERROR.getErrorCode());
    }

    /**
     * Instantiates a new ta unexpected exception.
     */
    protected TaConfigException() {
        super();
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pMessage the message
     */
    public TaConfigException(final String pMessage) {
        super(pMessage);
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pCause the cause
     */
    public TaConfigException(final Throwable pCause) {
        super(pCause);
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pMessage the message
     * @param pCause the cause
     */
    public TaConfigException(final String pMessage, final Throwable pCause) {
        super(pMessage, pCause);
    }

}
