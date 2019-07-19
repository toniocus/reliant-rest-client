package rest.client.models;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class ModelAddr {

    private String calle;
    private Integer numero;

    protected ModelAddr() {
    }


    public ModelAddr(final String pCalle, final Integer pNumero) {
        super();
        this.calle = pCalle;
        this.numero = pNumero;
    }

    public String getCalle() {
        return this.calle;
    }

    public Integer getNumero() {
        return this.numero;
    }

    public void setCalle(final String pCalle) {
        this.calle = pCalle;
    }

    public void setNumero(final Integer pNumero) {
        this.numero = pNumero;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ModelAddr [calle=").append(this.calle).append(", numero=").append(this.numero)
                .append("]");
        return builder.toString();
    }


}
