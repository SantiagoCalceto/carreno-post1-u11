package com.tienda.pedidos.dto;

// DTO de salida: lo unico que ve el cliente de la API. NO declara notasInternas
// (ni fechaCreacion interna): aunque la entidad cambie, este contrato no filtra datos.
public class PedidoResponseDTO {

    private Long id;
    private String clienteEmail;
    private String tipoCliente;
    private Double subtotal;
    private Double descuento;
    private Double iva;
    private Double total;
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getDescuento() { return descuento; }
    public void setDescuento(Double descuento) { this.descuento = descuento; }
    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
