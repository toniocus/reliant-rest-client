package rest.api;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class TaException, a basic class for all Exceptions with
 * Error Handling attributes.
 */
public class TaCheckedException extends Exception implements OMError {

    private static final long serialVersionUID = 1L;

    // We cannot use OMError inside TaException
    // because Tomcat classloader does not know about OMError.
    private String errorCode;
    private String errorMessage;
    private String errorSource;
    private String friendlyMessage;


    /**
     * Instantiates a new ta exception.
     *
     * <P>We need a default constructor in case we annotate
     * a extended one with HttpStatus spring annotation.
     */
    protected TaCheckedException() {
        super("No message specified");
    }

    /**
     * Instantiates a new ta common runtime exception.
     *
     * @param pMessage the message
     */
    public TaCheckedException(final String pMessage) {
        super(pMessage);
    }

    /**
     * Instantiates a new ta common runtime exception.
     *
     * @param pCause the cause
     */
    public TaCheckedException(final Throwable pCause) {
        super(pCause);
    }

    /**
     * Instantiates a new ta common runtime exception.
     *
     * @param pMessage the message
     * @param pCause the cause
     */
    public TaCheckedException(final String pMessage, final Throwable pCause) {
        super(pMessage, pCause);
    }

    @Override
    public String getMessage() {
        return String.format("[%s] %s", getErrorCodeOrDefaultOMCode(), super.getMessage());
    }

    /**
     * Sets only the error code, {@link TaCheckedException#getMessage()} will be used as ErrorMessage/Description.
     *
     * @param errorCode the error code
     * @return the ta exception
     */
    public TaCheckedException withErrorCode(final String errorCode) {
        withError(errorCode, null);
        return this;
    }

    /**
     * Sets only the error code, {@link TaCheckedException#getMessage()} will be used as ErrorMessage/Description.
     *
     * @param errorCode the error code
     * @return the ta exception
     */
    public TaCheckedException withErrorCode(final OMErrorEnum errorCode) {
        withError(errorCode.getErrorCode(), null);
        return this;
    }

    /**
     * Sets de errorCode and errorMessage, use the errorMessage in case the
     * {@link TaCheckedException#getMessage()} is not enough or correct for reporting error
     * purposes.
     *
     * @param errorCode the error code
     * @param errorMessage the error description
     * @return the ta exception
     */
    public TaCheckedException withError(final String errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Returns the errorCode set in withError methods or {@link OMErrorSourceEnum#OM}
     * if not present, the error source is telling me where the errorCode comes from.
     *
     * @return the error source as Optional.
     */
    @Override
    public String getErrorSource() {
        return (StringUtils.isBlank(this.errorSource) ? OMErrorSourceEnum.OM.getCode() : this.errorSource);
    }

    /**
     * With error source, if not provided or unknown {@link OMErrorSourceEnum#OM} will be used,
     * the error source is telling me where the errorCode comes from.
     *
     * @param errorSource the error source
     * @return the ta exception
     */
    public TaCheckedException withErrorSource(final OMErrorSourceEnum errorSource) {
        this.errorSource = errorSource.getCode();
        return this;
    }


    /**
     * Calls {@link #getErrorCodeOrDefaultOMCode()} if you want a default.
     *
     * @return the error code as optional.
     */
    @Override
    public String getErrorCode() {
        return getErrorCodeOrDefaultOMCode();
    }

    /**
     * Gets the error code or default OM code {@link OMErrorEnum#OM_UNEXPECTED}.
     *
     * @return the error code or default OM code
     */
    public String getErrorCodeOrDefaultOMCode() {
        return (this.errorCode == null ? OMErrorEnum.OM_UNEXPECTED.getErrorCode() : this.errorCode);
    }

    /**
     * Gets the error message or the {@link TaCheckedException#getMessage()}
     * it error message was not set.
     *
     * @return the error description
     */
    @Override
    public String getErrorMessage() {

        if (StringUtils.isBlank(this.errorMessage)) {
            return super.getMessage();  // gets the super so code is not provided in the message.
        }

        return this.errorMessage;
    }

    /**
     * Gets the user msg, a friendly error message when the error description
     * is too complex for an UI, might not be necessary.
     *
     * @return the user msg
     */
    @Override
    public Optional<String> getFriendlyMessage() {
        return Optional.ofNullable(this.friendlyMessage);
    }

    /**
     * Sets the user msg, a friendly error message when the error description
     * is too complex for an UI, might not be necessary.
     *
     * @param friendlyMsg the userMsg to set
     * @return the ta exception
     */
    public TaCheckedException withFriendlyMessage(final String friendlyMsg) {
        this.friendlyMessage = friendlyMsg;
        return this;
    }
}
