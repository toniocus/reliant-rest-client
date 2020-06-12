package rest.api;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * The Enum OMErrorSourceEnum, contains constants and codes for error source,
 * it also serves as legacy constants.
 *
 * @author tonioc
 */
public enum OMErrorSourceEnum {

    COMPTEL(""),
    OM(""),
    HUAWEI(""),
    VMINET(""),
    MEC(""),
    DATAPOWER(""),
    SFDC,
    NUM_MOVIL,
    NRED,
    CSG(""),
    NOKIA;

    private String sourceCode;

    /**
     * Constructor, using name as code.
     */
    OMErrorSourceEnum() {
        this(null);
    }

    /**
     * Instantiates a new OM error source enum.
     *
     * @param pSourceCode the source code
     */
    OMErrorSourceEnum(final String pSourceCode) {
        this.sourceCode = pSourceCode;
    }

    /**
     * Gets the provide code or if empty the Enum name().
     *
     * @return the code
     */
    public String getCode() {
        return (StringUtils.isNotBlank(this.sourceCode) ? this.sourceCode : name());
    }

    /**
     * Find by code, if not found or code is null/empty an Optional.empty() will be returned.
     *
     * @param code the code
     * @return the optional
     */
    public static Optional<OMErrorSourceEnum> findByCode(final String code) {

        if (StringUtils.isEmpty(code)) {
            return Optional.empty();
        }

        for (OMErrorSourceEnum e : OMErrorSourceEnum.values()) {

            if (code.equals(e.getCode())) {
                return Optional.of(e);
            }

        }

        return Optional.empty();

    }

}
