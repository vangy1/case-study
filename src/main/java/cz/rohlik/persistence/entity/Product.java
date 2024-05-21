package cz.rohlik.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt = new Date();
    private String name;
    private Long inventory;
    private Double price;
    private boolean deleted;

    @OneToMany(mappedBy = "product")
    private List<ProductInventory> productInventories;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;
}
