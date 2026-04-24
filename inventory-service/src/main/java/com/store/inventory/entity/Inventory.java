package com.store.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private Integer available;

    @Version
    private Long version;
}
