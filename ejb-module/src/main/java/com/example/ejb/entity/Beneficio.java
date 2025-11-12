package com.example.ejb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "beneficio")
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Column(name = "DESCRICAO", length = 255)
    private String descricao;

    @Column(name = "VALOR", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "ATIVO")
    private Boolean ativo = Boolean.TRUE;

    @Version
    @Column(name = "VERSION")
    private Long version;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Boolean getAtivo() { return ativo; }
    public Long getVersion() { return version; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public void creditar(BigDecimal quantia) {
        this.valor = this.valor.add(quantia);
    }

    public void debitar(BigDecimal quantia) {
        this.valor = this.valor.subtract(quantia);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Beneficio)) return false;
        Beneficio that = (Beneficio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
