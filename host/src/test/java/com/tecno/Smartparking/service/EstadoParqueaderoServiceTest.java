package com.tecno.Smartparking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tecno.Smartparking.exception.NegocioException;
import com.tecno.Smartparking.model.EstadoOperativo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoParqueaderoServiceTest {

    private EstadoParqueaderoService estado;

    @BeforeEach
    void prepararEstado() {
        estado = new EstadoParqueaderoService();
        estado.inicializar(6, 0, EstadoOperativo.OPERATIVO);
    }

    @Test
    @DisplayName("Un ingreso descuenta un cupo")
    void elIngresoDescuentaUnCupo() {
        estado.registrarEntrada();

        assertEquals(5, estado.getCuposDisponibles());
        assertEquals(1, estado.getCuposOcupados());
    }

    @Test
    @DisplayName("Sin cupos el ingreso lanza un error de negocio")
    void sinCuposElIngresoFalla() {
        estado.fijarCuposDisponibles(0);

        assertThrows(NegocioException.class, estado::registrarEntrada);
        assertEquals(0, estado.getCuposDisponibles());
    }

    @Test
    @DisplayName("Una salida suma un cupo sin exceder la capacidad")
    void laSalidaNoExcedeLaCapacidad() {
        estado.registrarSalida();
        estado.registrarSalida();

        assertEquals(6, estado.getCuposDisponibles());
    }

    @Test
    @DisplayName("Al quedarse sin cupos pasa a LLENO y vuelve a OPERATIVO al liberarse")
    void elEstadoSigueAlConteo() {
        estado.fijarCuposDisponibles(1);

        estado.registrarEntrada();
        assertEquals(EstadoOperativo.LLENO, estado.getEstado());
        assertTrue(estado.estaLleno());

        estado.registrarSalida();
        assertEquals(EstadoOperativo.OPERATIVO, estado.getEstado());
        assertFalse(estado.estaLleno());
    }

    @Test
    @DisplayName("La emergencia manda sobre el conteo")
    void laEmergenciaNoLaPisaElConteo() {
        estado.cambiarEstado(EstadoOperativo.EMERGENCIA);

        estado.registrarEntrada();

        assertEquals(EstadoOperativo.EMERGENCIA, estado.getEstado());
    }

    @Test
    @DisplayName("Cambiar la capacidad conserva los cupos ocupados")
    void actualizarCapacidadConservaOcupados() {
        estado.registrarEntrada();
        estado.registrarEntrada();

        estado.actualizarCapacidad(10);

        assertEquals(10, estado.getCapacidadTotal());
        assertEquals(8, estado.getCuposDisponibles());
        assertEquals(2, estado.getCuposOcupados());
    }

    @Test
    @DisplayName("Reducir la capacidad por debajo de los ocupados deja el parqueadero lleno")
    void reducirCapacidadPorDebajoDeLosOcupados() {
        estado.registrarEntrada();
        estado.registrarEntrada();
        estado.registrarEntrada();

        estado.actualizarCapacidad(2);

        assertEquals(0, estado.getCuposDisponibles());
        assertEquals(EstadoOperativo.LLENO, estado.getEstado());
    }

    @Test
    @DisplayName("La recalibracion fija los cupos dentro del rango valido")
    void recalibracionDeCupos() {
        estado.fijarCuposDisponibles(2);

        assertEquals(2, estado.getCuposDisponibles());
        assertEquals(4, estado.getCuposOcupados());
        assertThrows(NegocioException.class, () -> estado.fijarCuposDisponibles(7));
    }

    @Test
    @DisplayName("Al inicializar se reconstruye el conteo desde los ocupados guardados")
    void inicializacionDesdeLaBase() {
        estado.inicializar(6, 2, EstadoOperativo.OPERATIVO);

        assertEquals(2, estado.getCuposOcupados());
        assertEquals(4, estado.getCuposDisponibles());
    }
}
