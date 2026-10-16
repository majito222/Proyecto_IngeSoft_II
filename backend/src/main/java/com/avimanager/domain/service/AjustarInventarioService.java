package com.avimanager.domain.service;

import com.avimanager.domain.event.StockCriticoDetectado;
import com.avimanager.domain.exception.InsumoNoEncontradoException;
import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.policy.PoliticaStock;
import com.avimanager.domain.port.in.AjustarInventarioCommand;
import com.avimanager.domain.port.in.AjustarInventarioUseCase;
import com.avimanager.domain.port.out.ActaAjusteRepositoryPort;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.service.inventario.ComandoAjusteInventario;
import com.avimanager.domain.service.inventario.ComandoInventario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementa F-05.1 (RN-08). Es el invocador del patron Command: arma el
 * ComandoAjusteInventario con los datos del ajuste, lo ejecuta y guarda el acta
 * que devuelve como auditoria. Si el ajuste deja el insumo bajo el 50% de la
 * bodega, publica StockCriticoDetectado, igual que el cierre de turno (F-03).
 * Solo el Administrador llega aqui (permiso AJUSTAR_INVENTARIO en SecurityConfig).
 */
public class AjustarInventarioService implements AjustarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepository;
    private final ActaAjusteRepositoryPort actaAjusteRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PublicadorEventosPort publicadorEventos;
    private final Clock clock;

    public AjustarInventarioService(InventarioRepositoryPort inventarioRepository,
                                    ActaAjusteRepositoryPort actaAjusteRepository,
                                    UsuarioRepositoryPort usuarioRepository,
                                    PublicadorEventosPort publicadorEventos,
                                    Clock clock) {
        this.inventarioRepository = inventarioRepository;
        this.actaAjusteRepository = actaAjusteRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicadorEventos = publicadorEventos;
        this.clock = clock;
    }

    @Override
    public ActaAjuste ajustarInventario(AjustarInventarioCommand comando) {
        Insumo insumo = inventarioRepository.buscarPorId(comando.getInsumoId())
                .orElseThrow(() -> new InsumoNoEncontradoException(comando.getInsumoId()));
        String administradorNombre = usuarioRepository.buscarPorUsername(comando.getAdministradorUsername())
                .map(Usuario::getNombreCompleto)
                .orElse(comando.getAdministradorUsername());
        LocalDateTime ahora = LocalDateTime.now(clock);

        ComandoInventario ajuste = new ComandoAjusteInventario(UUID.randomUUID().toString(), insumo,
                comando.getStockContado(), comando.getJustificacion(), comando.getAdministradorUsername(),
                administradorNombre, ahora);
        ActaAjuste acta = ajuste.ejecutar();

        inventarioRepository.guardar(insumo);
        actaAjusteRepository.guardar(acta);

        if (PoliticaStock.cruzaUmbralCritico(acta.getStockSistema(), insumo.getStock(), insumo.getCapacidad())) {
            publicadorEventos.publicar(new StockCriticoDetectado(insumo, acta.getStockSistema(), ahora));
        }
        return acta;
    }
}
