package res.common;

public class ProductType implements java.io.Serializable {
    private String prodType;
    private String description;

    public String getProdType() {
        return prodType;
    }

    public String getDescription() {
        return description;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
