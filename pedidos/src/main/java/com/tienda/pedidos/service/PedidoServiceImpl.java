package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PedidoServiceImpl implements PedidoService {

    static final double TASA_IVA = 0.19;
    static final String TIPO_POR_DEFECTO = "REGULAR";

    private final PedidoRepository repo;
    private final NotificacionService notificador;
    private final Map<String, EstrategiaDescuento> estrategias;

    // Todas las dependencias llegan por constructor (DIP): ningun colaborador se crea con "new".
    // Spring llena el Map con los beans EstrategiaDescuento, usando el nombre del bean como llave.
    public PedidoServiceImpl(PedidoRepository repo,
                             NotificacionService notificador,
                             Map<String, EstrategiaDescuento> estrategias) {
        this.repo        = repo;
        this.notificador = notificador;
        this.estrategias = estrategias;
    }

    @Override
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        String tipo = resolverTipo(dto.getTipoCliente());
        EstrategiaDescuento estrategia = estrategias.get(tipo);
        double descuento = redondear(estrategia.calcularDescuento(dto.getSubtotal()));

        double baseConDescuento = dto.getSubtotal() - descuento;
        double iva = redondear(baseConDescuento * TASA_IVA);
        double total = redondear(baseConDescuento + iva);

        Pedido pedido = new Pedido();   // la entidad es un dato, no un colaborador
        pedido.setClienteEmail(dto.getClienteEmail());
        pedido.setTipoCliente(tipo);
        pedido.setSubtotal(dto.getSubtotal());
        pedido.setDescuento(descuento);
        pedido.setIva(iva);
        pedido.setTotal(total);
        pedido.setEstado("CREADO");
        pedido.setNotasInternas("Pedido creado automaticamente, revisar stock antes de despachar");
        pedido.setFechaCreacion(LocalDateTime.now());

        Pedido guardado = repo.save(pedido);

        notificador.enviarConfirmacion(guardado.getClienteEmail(), guardado.getTotal());
        return toResponseDTO(guardado);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido p = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        return toResponseDTO(p);
    }

    // Un tipo de cliente desconocido no rompe la creacion: se aplica la estrategia REGULAR
    private String resolverTipo(String tipoCliente) {
        String tipo = tipoCliente.trim().toUpperCase();
        return estrategias.containsKey(tipo) ? tipo : TIPO_POR_DEFECTO;
    }

    // Montos de dinero a 2 decimales (HALF_UP) para evitar residuos de punto flotante
    private static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Mapeo explicito entidad -> DTO: notasInternas y fechaCreacion no se copian
    private PedidoResponseDTO toResponseDTO(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(p.getId());
        dto.setClienteEmail(p.getClienteEmail());
        dto.setTipoCliente(p.getTipoCliente());
        dto.setSubtotal(p.getSubtotal());
        dto.setDescuento(p.getDescuento());
        dto.setIva(p.getIva());
        dto.setTotal(p.getTotal());
        dto.setEstado(p.getEstado());
        return dto;
    }
}
