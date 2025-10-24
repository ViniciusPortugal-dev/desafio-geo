package dev.challenge.servicec.readmodel;

import dev.challenge.common.xls.ExcelCellName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelCellName("ID do Usuário")
    private Long id;

    @Column(name = "nome")
    @ExcelCellName("Nome")
    private String nome;

    @Column(name = "email")
    @ExcelCellName("E-mail")
    private String email;
}
