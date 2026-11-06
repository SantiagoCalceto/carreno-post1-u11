package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.DescuentoMayorista;
import com.tienda.pedidos.strategy.DescuentoRegular;
import com.tienda.pedidos.strategy.DescuentoVip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Gracias a DIP el servicio se prueba sin Spring ni base de datos: el repositorio y la
 * notificacion son mocks, y las estrategias se pasan en un Map igual al que arma Spring.
 * (Con el codigo de partida esto era imposible: la notificacion se creaba con "new".)
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock PedidoRepository repo;
    @Mock NotificacionService notificador;
    PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PedidoServiceImpl(repo, notificador, Map.of(
                "REGULAR", new DescuentoRegular(),
                "VIP", new DescuentoVip(),
                "MAYORISTA", new DescuentoMayorista()));
    }

    @ParameterizedTest(name = "{0}: descuento={1}, iva={2}, total={3}")
    @CsvSource({
            "VIP,       15000.0, 16150.0, 101150.0",
            "MAYORISTA, 25000.0, 14250.0,  89250.0",
            "REGULAR,       0.0, 19000.0, 119000.0",
            "vip,       15000.0, 16150.0, 101150.0"   // no distingue mayusculas
    })
    void crear_segunTipoCliente_aplicaLaEstrategiaCorrecta(String tipo, double descuento, double iva, double total) {
        when(repo.save(any())).thenAnswer(inv -> { Pedido p = inv.getArgument(0); p.setId(1L); return p; });

        PedidoResponseDTO r = service.crear(request("ana@correo.com", tipo, 100000.0));

        assertThat(r.getDescuento()).isEqualTo(descuento);
        assertThat(r.getIva()).isEqualTo(iva);
        assertThat(r.getTotal()).isEqualTo(total);
        assertThat(r.getTipoCliente()).isEqualTo(tipo.toUpperCase());
        assertThat(r.getEstado()).isEqualTo("CREADO");
        verify(notificador).enviarConfirmacion("ana@correo.com", total);
    }

    @Test
    void crear_tipoDesconocido_aplicaRegular() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO r = service.crear(request("ana@correo.com", "ORO", 100000.0));

        assertThat(r.getTipoCliente()).isEqualTo("REGULAR");
        assertThat(r.getDescuento()).isEqualTo(0.0);
    }

    @Test
    void crear_montosConDecimales_seRedondeanADosDecimales() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO r = service.crear(request("ana@correo.com", "MAYORISTA", 59990.5));

        assertThat(r.getDescuento()).isEqualTo(14997.63);
        assertThat(r.getIva()).isEqualTo(8548.65);
        assertThat(r.getTotal()).isEqualTo(53541.52);
    }

    @Test
    void crear_guardaNotasInternasEnLaEntidad_peroNoLasDevuelve() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Pedido> guardado = ArgumentCaptor.forClass(Pedido.class);

        service.crear(request("ana@correo.com", "VIP", 100000.0));

        verify(repo).save(guardado.capture());
        assertThat(guardado.getValue().getNotasInternas()).isNotBlank();
        assertThat(guardado.getValue().getFechaCreacion()).isNotNull();
        assertThat(Arrays.stream(PedidoResponseDTO.class.getDeclaredFields()).map(f -> f.getName()))
                .doesNotContain("notasInternas", "fechaCreacion");
    }

    @Test
    void buscarPorId_existe_retornaDTO() {
        Pedido p = new Pedido();
        p.setId(7L); p.setClienteEmail("ana@correo.com"); p.setTotal(101150.0); p.setNotasInternas("interno");
        when(repo.findById(7L)).thenReturn(Optional.of(p));

        PedidoResponseDTO r = service.buscarPorId(7L);

        assertThat(r.getId()).isEqualTo(7L);
        assertThat(r.getTotal()).isEqualTo(101150.0);
    }

    @Test
    void buscarPorId_noExiste_lanzaRecursoNoEncontradoException() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Pedido con id 999 no encontrado.");
        verifyNoInteractions(notificador);
    }

    private static PedidoRequestDTO request(String email, String tipo, Double subtotal) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setClienteEmail(email);
        dto.setTipoCliente(tipo);
        dto.setSubtotal(subtotal);
        return dto;
    }
}
