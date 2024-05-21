package cz.rohlik.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "\"order\"")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt = new Date();
    private Double totalPrice;
    private boolean paid;
    private boolean cancelled;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}
