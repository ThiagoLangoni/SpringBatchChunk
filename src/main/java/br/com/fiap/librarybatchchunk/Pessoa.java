package br.com.fiap.librarybatchchunk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pessoa {

    private Integer id;
    private String nome;
    private String cpf;

}