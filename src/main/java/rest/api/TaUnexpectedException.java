package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The Class TaUnexpectedException, is a TaException
 * with errorCode set to {@link OMErrorEnum#OM_UNEXPECTED} and
 * errorSource set to {@link OMErrorSourceEnum#OM}, and
 * Spring annotation to use HttpStatus 500 when thrown.
 *
 * @author tonioc
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Unexpected Error")
public class TaUnexpectedException extends TaException {

    private static final long serialVersionUID = 1L;

    {
        withErrorSource(OMErrorSourceEnum.OM);
        withErrorCode(OMErrorEnum.OM_UNEXPECTED.getErrorCode());
    }

    /**
     * Instantiates a new ta unexpected exception.
     */
    protected TaUnexpectedException() {
        super();
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pMessage the message
     */
    public TaUnexpectedException(final String pMessage) {
        super(pMessage);
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pCause the cause
     */
    public TaUnexpectedException(final Throwable pCause) {
        super(pCause);
    }

    /**
     * Instantiates a new ta unexpected exception.
     *
     * @param pMessage the message
     * @param pCause the cause
     */
    public TaUnexpectedException(final String pMessage, final Throwable pCause) {
        super(pMessage, pCause);
    }

}
