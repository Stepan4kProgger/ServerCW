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

    // Getters и setters
}
