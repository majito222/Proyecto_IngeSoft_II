package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.exception.GalponAjenoException;
import com.avimanager.domain.exception.OrdenNoDisponibleException;
import com.avimanager.domain.exception.OrdenNoEncontradaException;
import com.avimanager.domain.exception.AlertaNoEncontradaException;
import com.avimanager.domain.exception.AlertaYaAtendidaException;
import com.avimanager.domain.exception.CapacidadGalponExcedidaException;
import com.avimanager.domain.exception.CredencialesInvalidasException;
import com.avimanager.domain.exception.GalponConLoteActivoException;
import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.LoteYaFinalizadoException;
import com.avimanager.domain.exception.PesajeNoEncontradoException;
import com.avimanager.domain.exception.PesajeYaRegistradoException;
import com.avimanager.domain.exception.PesajeYaRevisadoException;
import com.avimanager.domain.exception.TurnoAjenoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.exception.TurnoYaIniciadoException;
import com.avimanager.domain.exception.UsuarioNoEncontradoException;
import com.avimanager.domain.exception.UsuarioNoEsWorkerException;
import com.avimanager.domain.exception.WorkerConTurnoAbiertoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlertaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarAlertaNoEncontrada(AlertaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler({LoteNoEncontradoException.class, PesajeNoEncontradoException.class,
            GalponNoEncontradoException.class, UsuarioNoEncontradoException.class, OrdenNoEncontradaException.class})
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Reglas del turno (F-10): la peticion es valida pero choca con el estado actual. */
    @ExceptionHandler({TurnoYaIniciadoException.class, TurnoNoDisponibleException.class, GalponNoAsignadoException.class,
            AlertaYaAtendidaException.class, PesajeYaRegistradoException.class, PesajeYaRevisadoException.class})
    public ResponseEntity<Map<String, String>> manejarConflictoDeTurno(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Reglas de galpones y lotes (F-11): la peticion es valida pero rompe una regla de negocio. */
    @ExceptionHandler({CapacidadGalponExcedidaException.class, GalponConLoteActivoException.class,
            LoteYaFinalizadoException.class, UsuarioNoEsWorkerException.class, WorkerConTurnoAbiertoException.class,
            OrdenNoDisponibleException.class})
    public ResponseEntity<Map<String, String>> manejarConflictoDeGalpon(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Tiene el permiso CERRAR_TURNO, pero el turno es de otro worker. */
    @ExceptionHandler({TurnoAjenoException.class, GalponAjenoException.class})
    public ResponseEntity<Map<String, String>> manejarRecursoAjeno(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
    }
}
