package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.exception.AlertaNoEncontradaException;
import com.avimanager.domain.exception.AlertaYaAtendidaException;
import com.avimanager.domain.exception.CredencialesInvalidasException;
import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.TurnoAjenoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.exception.TurnoYaIniciadoException;
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

    @ExceptionHandler(LoteNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarLoteNoEncontrado(LoteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Reglas del turno (F-10): la peticion es valida pero choca con el estado actual. */
    @ExceptionHandler({TurnoYaIniciadoException.class, TurnoNoDisponibleException.class, GalponNoAsignadoException.class,
            AlertaYaAtendidaException.class})
    public ResponseEntity<Map<String, String>> manejarConflictoDeTurno(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Tiene el permiso CERRAR_TURNO, pero el turno es de otro worker. */
    @ExceptionHandler(TurnoAjenoException.class)
    public ResponseEntity<Map<String, String>> manejarTurnoAjeno(TurnoAjenoException ex) {
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
