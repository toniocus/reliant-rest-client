package rest.api;

import java.util.Optional;

/**
 * The Interface OMError.
 *
 * @author tonioc
 */
public interface OMError {

    /**
     * Gets the error source, the error source is the Legacy/OM that provides the
     * error code.
     *
     * @return the error source
     */
    String getErrorSource();

    /**
     * Gets the error code, OM error codes should start with <b>OM_</b>.
     *
     * @return the code
     */
    String getErrorCode();

    /**
     * Gets the message.
     *
     * @return the message
     */
    String getErrorMessage();


    /**
     * Gets the friendly message, a friendly message is a message that <b>might</b> be used
     * for UI, being friendly for a CRM operator.
     *
     * @return the message
     */
    Optional<String> getFriendlyMessage();

}
