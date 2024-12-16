package Hibernate.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "types_of_product")
public class TypesOfProduct {
    @Id
    @Column(name = "prod_type", length = 32, nullable = false)
    private String prodType;

    @Column(name = "description", length = 128)
    private String description;

    public String getProdType() {
        return prodType;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getters и setters
}
