package res.common;

public class Product implements java.io.Serializable, StringConvertible, Clone<Product> {
    private int id;
    private String prodType;
    private String name;
    private String manufacturer;
    private int amount;
    private float price;

    public void setId(int id) {
        this.id = id;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getProdType() {
        return prodType;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getAmount() {
        return amount;
    }

    public float getPrice() {
        return price;
    }

    public Product() {
    }

    public Product(Product prod) {
        id = prod.id;
        prodType = prod.prodType;
        name = prod.name;
        manufacturer = prod.manufacturer;
        amount = prod.amount;
        price = prod.price;
    }

    @Override
    public Product makeClone() {
        return new Product(this);
    }

    @Override
    public String toQueryString() {
        return "'" + prodType + "', '" + name + "', '" + manufacturer + "', " + amount + ", " + price;
    }
}
