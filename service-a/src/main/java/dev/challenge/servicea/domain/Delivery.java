package dev.challenge.servicea.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entregador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "telefone", nullable = false, length = 40)
    private String phone;
}
