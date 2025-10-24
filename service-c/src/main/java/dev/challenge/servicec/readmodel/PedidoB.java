package dev.challenge.servicec.readmodel;

import dev.challenge.common.xls.ExcelCellName;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelCellName("ID do Pedido")
    private Long id;

    @Column(name = "descricao")
    @ExcelCellName("Descrição")
    private String descricao;

    @Column(name = "valor")
    @ExcelCellName("Valor (R$)")
    private BigDecimal valor;

    @Column(name = "id_usuario")
    @ExcelCellName("ID do Usuário")
    private Long idUsuario;

    @Column(name = "nome_entregador")
    @ExcelCellName("Nome do Entregador")
    private String nomeEntregador;

    @Column(name = "telefone_entregador")
    @ExcelCellName("Telefone do Entregador")
    private String telefoneEntregador;
}
