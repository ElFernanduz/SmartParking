package com.unillanos.smartparking.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.dominio.excepcion.SinCuposDisponiblesExcepcion;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.politica.PoliticaAccesoPorDefecto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoParqueaderoTest {

    @Test
    @DisplayName("Un ingreso descuenta un cupo")
    void elIngresoDescuentaUnCupo() {
        EstadoParqueadero estado = new EstadoParqueadero(6);

        estado.registrarEntrada();

        assertEquals(5, estado.getCuposDisponibles());
        assertEquals(1, estado.getCuposOcupados());
    }

    @Test
    @DisplayName("Sin cupos el ingreso lanza excepcion de dominio")
    void sinCuposElIngresoFalla() {
        EstadoParqueadero estado = new EstadoParqueadero(1);
        estado.registrarEntrada();

        assertThrows(SinCuposDisponiblesExcepcion.class, estado::registrarEntrada);
        assertEquals(0, estado.getCuposDisponibles());
    }

    @Test
    @DisplayName("Una salida suma un cupo sin exceder la capacidad")
    void laSalidaNoExcedeLaCapacidad() {
        EstadoParqueadero estado = new EstadoParqueadero(2);

        estado.registrarSalida();
        estado.registrarSalida();

        assertEquals(2, estado.getCuposDisponibles());
    }

    @Test
    @DisplayName("Al quedarse sin cupos el estado pasa a LLENO y vuelve a OPERATIVO al liberarse")
    void elEstadoSigueAlConteo() {
        EstadoParqueadero estado = new EstadoParqueadero(1);

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
        EstadoParqueadero estado = new EstadoParqueadero(2);

        estado.cambiarEstado(EstadoOperativo.EMERGENCIA);
        estado.registrarEntrada();

        assertEquals(EstadoOperativo.EMERGENCIA, estado.getEstado());
    }

    @Test
    @DisplayName("Cambiar la capacidad conserva los cupos ocupados")
    void actualizarCapacidadConservaOcupados() {
        EstadoParqueadero estado = new EstadoParqueadero(6);
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
        EstadoParqueadero estado = new EstadoParqueadero(6);
        estado.registrarEntrada();
        estado.registrarEntrada();
        estado.registrarEntrada();

        estado.actualizarCapacidad(2);

        assertEquals(2, estado.getCapacidadTotal());
        assertEquals(0, estado.getCuposDisponibles());
        assertEquals(EstadoOperativo.LLENO, estado.getEstado());
    }

    @Test
    @DisplayName("La invariante impide construir un estado inconsistente")
    void laInvarianteSeRespeta() {
        assertThrows(ValorInvalidoExcepcion.class,
                () -> new EstadoParqueadero(3, 5, EstadoOperativo.OPERATIVO));
        assertThrows(ValorInvalidoExcepcion.class,
                () -> new EstadoParqueadero(3, -1, EstadoOperativo.OPERATIVO));
    }

    @Test
    @DisplayName("La recalibracion fija los cupos disponibles dentro del rango valido")
    void recalibracionDeCupos() {
        EstadoParqueadero estado = new EstadoParqueadero(6);

        estado.fijarCuposDisponibles(2);

        assertEquals(2, estado.getCuposDisponibles());
        assertEquals(4, estado.getCuposOcupados());
        assertThrows(ValorInvalidoExcepcion.class, () -> estado.fijarCuposDisponibles(7));
    }

    @Test
    @DisplayName("puedeAdmitir consulta la politica recibida")
    void puedeAdmitirDelegaEnLaPolitica() {
        EstadoParqueadero estado = new EstadoParqueadero(1);
        PoliticaAccesoPorDefecto politica = new PoliticaAccesoPorDefecto();

        assertTrue(estado.puedeAdmitir(politica));
        estado.registrarEntrada();
        assertFalse(estado.puedeAdmitir(politica));
    }
}
