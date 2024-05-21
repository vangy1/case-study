package cz.rohlik.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(indexes = {
        @Index(columnList = "quantity", name = "quantity_index")
})
@Data
@NoArgsConstructor
public class ProductInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt = new Date();
    private Long quantity;
    private String description;


    @ManyToOne
    private Product product;
}
