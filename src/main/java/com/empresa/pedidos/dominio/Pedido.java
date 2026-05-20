package com.empresa.pedidos.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoPedido tipo;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    private Double subtotal;
    private Double costo;

    public Pedido() {}

    public Long getId() { return id; }
    public TipoPedido getTipo() { return tipo; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }
}