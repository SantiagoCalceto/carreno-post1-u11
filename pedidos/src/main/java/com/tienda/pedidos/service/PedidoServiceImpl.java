package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

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
        log.info("Creando pedido: clienteEmail={}, tipoCliente={}",
                 dto.getClienteEmail(), dto.getTipoCliente());

        String tipo = resolverTipo(dto.getTipoCliente());
        EstrategiaDescuento estrategia = estrategias.get(tipo);
        double descuento = redondear(estrategia.calcularDescuento(dto.getSubtotal()));
        // Unico punto de auditoria del descuento (no se registra dentro de cada Strategy)
        log.info("Descuento aplicado: pedido para clienteEmail={}, tipoCliente={}, descuento={}",
                 dto.getClienteEmail(), tipo, descuento);

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
        // notasInternas NUNCA se registra en el log (dato operativo interno)
        log.info("Pedido creado exitosamente con id={}, total={}", guardado.getId(), guardado.getTotal());

        notificador.enviarConfirmacion(guardado.getClienteEmail(), guardado.getTotal());
        return toResponseDTO(guardado);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        log.debug("Buscando pedido con id={}", id);
        Pedido p = repo.findById(id).orElseThrow(() -> {
            log.warn("Pedido con id={} no encontrado", id);
            return new RecursoNoEncontradoException("Pedido", id);
        });
        return toResponseDTO(p);
    }

    // Un tipo de cliente desconocido no rompe la creacion: se aplica la estrategia REGULAR
    // y se deja un WARN, porque es una situacion inusual que conviene revisar.
    private String resolverTipo(String tipoCliente) {
        String tipo = tipoCliente.trim().toUpperCase();
        if (!estrategias.containsKey(tipo)) {
            log.warn("Tipo de cliente desconocido tipoCliente={}, se aplica {}", tipoCliente, TIPO_POR_DEFECTO);
            return TIPO_POR_DEFECTO;
        }
        return tipo;
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
