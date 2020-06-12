package rest.api;

/**
 * Very general error codes to be used.
 *
 * @author tonioc
 *
 */
public enum OMErrorEnum {

    OM_UNEXPECTED("Unexpected error happend during OM processing"),

    OM_DATA_INCONSISTENCY("Data provided to OrchItem has inconsistencies that do not allow processing"),

    OM_IOERROR("Error trying to connect to a local or extenal resource"),

    OM_TIMEOUT("While waiting for processing/message a timeout was triggered"),

    OM_NOT_FOUND("A controlled not found by OM, typically used in callbacks to inform correlationId not available"),

    OM_LEGACY_UNEXPECTED("Unexpected error arrived connecting with Legacy"),

    OM_LEGACY_DATA_INCONSISTENCY("Data provided by legacy is inconsistent with expected values"),

    OM_CONFIG_ERROR("Errors in OM catalogue");

    private String message;

    /**
     * Instantiates a new OM error enum.
     *
     * @param message the description or message.
     */
    OMErrorEnum(final String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return name();
    }

    public String getErrorDescription() {
        return this.message;
    }

}
