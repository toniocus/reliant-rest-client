package rest.client.models;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class ModelPerson {

    private String name;
    private List<ModelAddr> addresses = new ArrayList<>();

    protected ModelPerson() {
    }

    public ModelPerson(final String pName) {
        super();
        this.name = pName;
    }

    public String getName() {
        return this.name;
    }

    public List<ModelAddr> getAddresses() {
        return this.addresses;
    }

    public void setName(final String pName) {
        this.name = pName;
    }

    public void setAddresses(final List<ModelAddr> pAddresses) {
        this.addresses = pAddresses;
    }

    public ModelPerson addAddr(final String calle, final Integer numero) {
        this.addresses.add(new ModelAddr(calle, numero));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ModelPerson [name=").append(this.name).append(", addresses=").append(this.addresses)
                .append("]");
        return builder.toString();
    }


}
