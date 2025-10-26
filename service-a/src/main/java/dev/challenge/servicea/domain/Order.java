package dev.challenge.servicea.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 255)
    private String description;

    @Column(name = "valor", nullable = false, precision = 14, scale = 2)
    private BigDecimal value;

    @Column(name = "id_usuario", nullable = false)
    private Long idUser;

    @Column(name = "id_entregador", nullable = false)
    private Long idDelivery;

    @Column(name = "external_id", nullable = false, unique = true, length = 50)
    private String externalId;

    @Column(name = "external_user_id", nullable = false, length = 50)
    private String externalUserId;
}
