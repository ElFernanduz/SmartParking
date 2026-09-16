package com.unillanos.smartparking.infraestructura.config;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.aplicacion.seguridad.AuthenticationService;
import com.unillanos.smartparking.aplicacion.seguridad.AuthorizationService;
import com.unillanos.smartparking.aplicacion.seguridad.SimplePasswordHasher;
import com.unillanos.smartparking.aplicacion.servicio.ConfiguracionServicio;
import com.unillanos.smartparking.aplicacion.servicio.ConsultaEstadoServicio;
import com.unillanos.smartparking.aplicacion.servicio.ConsultaHistorialServicio;
import com.unillanos.smartparking.aplicacion.servicio.ControlManualServicio;
import com.unillanos.smartparking.aplicacion.servicio.EjecutarEmergenciaServicio;
import com.unillanos.smartparking.aplicacion.servicio.ProcesarLecturaHumoServicio;
import com.unillanos.smartparking.aplicacion.servicio.RegistrarEntradaServicio;
import com.unillanos.smartparking.aplicacion.servicio.RegistrarSalidaServicio;
import com.unillanos.smartparking.aplicacion.servicio.SincronizarCuposServicio;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.autenticacion.PasswordHasher;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.politica.PoliticaAcceso;
import com.unillanos.smartparking.dominio.politica.PoliticaAccesoPorDefecto;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarEstadoCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarHistorialCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ControlManualCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.GestionarConfiguracionCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ProcesarLecturaHumoCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarEntradaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarSalidaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.SincronizarCuposCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSeguridad;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import com.unillanos.smartparking.infraestructura.dispositivo.PropiedadesDispositivo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los objetos del nucleo son clases planas; aqui se declaran como beans y se
 * les inyectan las implementaciones de los puertos. Spring no entra al nucleo.
 */
@Configuration
@EnableConfigurationProperties(PropiedadesDispositivo.class)
public class ConfiguracionBeans {

    @Bean
    public EjecutorSerie ejecutorSerie() {
        return new EjecutorSerie();
    }

    @Bean
    public PoliticaAcceso politicaAcceso() {
        return new PoliticaAccesoPorDefecto();
    }

    @Bean
    public EstadoAlarma estadoAlarma() {
        return new EstadoAlarma();
    }

    @Bean
    public EstadoParqueadero estadoParqueadero(RepositorioConfiguracion repositorioConfiguracion) {
        return new EstadoParqueadero(repositorioConfiguracion.obtenerCapacidad());
    }

    @Bean
    public Barrera barreraEntrada() {
        return new Barrera(TipoPunto.ENTRADA);
    }

    @Bean
    public Barrera barreraSalida() {
        return new Barrera(TipoPunto.SALIDA);
    }

    @Bean
    public FabricaComandos fabricaComandos(PuertoPasarelaDispositivo pasarela) {
        return new FabricaComandos(pasarela);
    }

    @Bean
    public InvocadorComandos invocadorComandos() {
        return new InvocadorComandos();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new SimplePasswordHasher();
    }

    @Bean
    public AuthenticationService authenticationService(UserRepository users, PasswordHasher hasher) {
        return new AuthenticationService(users, hasher);
    }

    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationService();
    }

    @Bean
    public RegistrarEntradaCasoUso registrarEntradaServicio(EstadoParqueadero estadoParqueadero,
                                                            PoliticaAcceso politica,
                                                            @Qualifier("barreraEntrada") Barrera barrera,
                                                            RepositorioRegistroAcceso registros,
                                                            RepositorioEventoSistema eventos,
                                                            FabricaComandos comandos,
                                                            InvocadorComandos invocador,
                                                            PuertoPublicadorEventos publicador,
                                                            EjecutorSerie ejecutor,
                                                            PropiedadesDispositivo propiedades) {
        return new RegistrarEntradaServicio(estadoParqueadero, politica, barrera, registros, eventos,
                comandos, invocador, publicador, ejecutor, propiedades.getEsperaPasoMs());
    }

    @Bean
    public RegistrarSalidaCasoUso registrarSalidaServicio(EstadoParqueadero estadoParqueadero,
                                                          @Qualifier("barreraSalida") Barrera barrera,
                                                          RepositorioRegistroAcceso registros,
                                                          FabricaComandos comandos,
                                                          InvocadorComandos invocador,
                                                          PuertoPublicadorEventos publicador,
                                                          EjecutorSerie ejecutor,
                                                          PropiedadesDispositivo propiedades) {
        return new RegistrarSalidaServicio(estadoParqueadero, barrera, registros, comandos,
                invocador, publicador, ejecutor, propiedades.getEsperaPasoMs());
    }

    @Bean
    public EjecutarEmergenciaCasoUso ejecutarEmergenciaServicio(EstadoParqueadero estadoParqueadero,
                                                                EstadoAlarma estadoAlarma,
                                                                @Qualifier("barreraSalida") Barrera barrera,
                                                                RepositorioConfiguracion configuracion,
                                                                RepositorioEventoSistema eventos,
                                                                FabricaComandos comandos,
                                                                InvocadorComandos invocador,
                                                                PuertoPublicadorEventos publicador) {
        return new EjecutarEmergenciaServicio(estadoParqueadero, estadoAlarma, barrera, configuracion,
                eventos, comandos, invocador, publicador);
    }

    @Bean
    public ProcesarLecturaHumoCasoUso procesarLecturaHumoServicio(RepositorioConfiguracion configuracion,
                                                                  RepositorioEventoSeguridad eventos,
                                                                  EjecutarEmergenciaCasoUso emergencia,
                                                                  EstadoAlarma estadoAlarma,
                                                                  FabricaComandos comandos,
                                                                  InvocadorComandos invocador,
                                                                  PuertoPublicadorEventos publicador) {
        return new ProcesarLecturaHumoServicio(configuracion, eventos, emergencia, estadoAlarma,
                comandos, invocador, publicador);
    }

    @Bean
    public GestionarConfiguracionCasoUso configuracionServicio(EstadoParqueadero estadoParqueadero,
                                                               RepositorioConfiguracion configuracion,
                                                               PuertoPasarelaDispositivo pasarela,
                                                               FabricaComandos comandos,
                                                               InvocadorComandos invocador,
                                                               PuertoPublicadorEventos publicador) {
        return new ConfiguracionServicio(estadoParqueadero, configuracion, pasarela, comandos,
                invocador, publicador);
    }

    @Bean
    public ConsultarEstadoCasoUso consultaEstadoServicio(EstadoParqueadero estadoParqueadero,
                                                         EstadoAlarma estadoAlarma,
                                                         PuertoPasarelaDispositivo pasarela) {
        return new ConsultaEstadoServicio(estadoParqueadero, estadoAlarma, pasarela);
    }

    @Bean
    public ConsultarHistorialCasoUso consultaHistorialServicio(RepositorioRegistroAcceso registros,
                                                               RepositorioEventoSeguridad eventos) {
        return new ConsultaHistorialServicio(registros, eventos);
    }

    @Bean
    public ControlManualCasoUso controlManualServicio(@Qualifier("barreraEntrada") Barrera entrada,
                                                      @Qualifier("barreraSalida") Barrera salida,
                                                      RepositorioEventoSistema eventos,
                                                      FabricaComandos comandos,
                                                      InvocadorComandos invocador,
                                                      PuertoPublicadorEventos publicador) {
        return new ControlManualServicio(entrada, salida, eventos, comandos, invocador, publicador);
    }

    @Bean
    public SincronizarCuposCasoUso sincronizarCuposServicio(EstadoParqueadero estadoParqueadero,
                                                            RepositorioRegistroAcceso registros,
                                                            RepositorioEventoSistema eventos,
                                                            FabricaComandos comandos,
                                                            InvocadorComandos invocador,
                                                            PuertoPublicadorEventos publicador) {
        return new SincronizarCuposServicio(estadoParqueadero, registros, eventos, comandos,
                invocador, publicador);
    }
}
