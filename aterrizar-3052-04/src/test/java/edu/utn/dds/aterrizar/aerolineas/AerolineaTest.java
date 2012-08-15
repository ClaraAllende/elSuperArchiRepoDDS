package edu.utn.dds.aterrizar.aerolineas;



import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lanchita.AerolineaLanchita;
import com.lanchita.excepciones.EstadoErroneoException;

import edu.utn.dds.aterrizar.escalas.VueloDirecto;
import edu.utn.dds.aterrizar.parser.Parser;
import edu.utn.dds.aterrizar.usuario.Usuario;
import edu.utn.dds.aterrizar.vuelo.Asiento;
import edu.utn.dds.aterrizar.vuelo.Busqueda;
/**
 * 
 * @author clari, ariel
 *
 */
public class AerolineaTest {

	private Aerolinea comunicadorDeAerolinea;
	private Busqueda vuelo;
	private Usuario usuario;
	private Date fecha;
	private AerolineaLanchita aerolineaLanchita;
	private Parser parser;

	@Before
	public void setUp()  {
		aerolineaLanchita = mock(AerolineaLanchita.class);
		parser = mock(Parser.class);
		vuelo = mock(Busqueda.class);
		usuario = mock(Usuario.class);
		fecha= new Date();
		this.comunicadorDeAerolinea = new AerolineaLanchitaWrapper(aerolineaLanchita, parser);
	}
	
	@Test
	public void testBuscarAsientos()  {
		String[] asiento = { "01202022220202-3", "159.90", "P", "V", "D", "" };
		String[][] asientosLanchita = {asiento};
		when(aerolineaLanchita.getAsientosDisponibles(anyString(), anyString(), any(Date.class))).thenReturn(asientosLanchita);
		when(parser.parseDisponibles(any(String[][].class), any(Busqueda.class), any(AerolineaLanchitaWrapper.class))).thenReturn(Arrays.asList(new VueloDirecto()));
		when(vuelo.getOrigen()).thenReturn("BUE");
		when(vuelo.getDestino()).thenReturn("LA");
		when(vuelo.getFecha()).thenReturn(fecha);
		List<VueloDirecto> disponibles = comunicadorDeAerolinea.buscarVuelos(vuelo);
		Assert.assertNotNull(disponibles);
		Assert.assertFalse(disponibles.isEmpty());
		
		verify(parser).parseDisponibles(asientosLanchita, vuelo, comunicadorDeAerolinea);
		verify(aerolineaLanchita).getAsientosDisponibles("BUE", "LA", fecha);
	}
	
	@Test
	public void testComprarAsientoDisponible() {
		when(usuario.getDni()).thenReturn("35247037");
		Asiento asiento= new Asiento(vuelo, mock(Aerolinea.class));
		asiento.setCodigoDeVuelo("01202022220202");
		asiento.setNumeroDeAsiento(3);
		asiento.setEstado("D");
		comunicadorDeAerolinea.comprarAsiento(asiento, usuario);
		assertEquals("C", asiento.getEstado());
		verify(aerolineaLanchita).comprar("01202022220202-3", "35247037");
	}
	
	
	@Test(expected = AsientoNoDisponibleException.class)
	public void testComprarAsientoNoDisponible() {
		when(usuario.getDni()).thenReturn("35247037");
		Asiento asiento= new Asiento(vuelo, mock(AerolineaLanchitaWrapper.class));
		asiento.setCodigoDeVuelo("01202022220202");
		asiento.setNumeroDeAsiento(3);
		asiento.setEstado("R");
		doThrow(new EstadoErroneoException()).when(aerolineaLanchita).comprar(anyString(), anyString());
		
		comunicadorDeAerolinea.comprarAsiento(asiento, usuario);
		
		verify(aerolineaLanchita).comprar("01202022220202-3", "35247037");
	}
	
	
	@Test(expected = AsientoNoDisponibleException.class)
	public void testComprarAsientoDisponibleDosVecesLaSegundaTiraError() {
		when(usuario.getDni()).thenReturn("35247037");
		
		Asiento asientoDisponible = new Asiento(vuelo, mock(Aerolinea.class));
		asientoDisponible.setCodigoDeVuelo("01202022220202");
		asientoDisponible.setNumeroDeAsiento(3);
		asientoDisponible.setEstado("D");
		
		doNothing().doThrow(new EstadoErroneoException()).when(aerolineaLanchita).comprar(anyString(), anyString());
		
		comunicadorDeAerolinea.comprarAsiento(asientoDisponible, usuario);
		assertEquals("C",asientoDisponible.getEstado());
		verify(aerolineaLanchita).comprar("01202022220202-3", "35247037");

		
		comunicadorDeAerolinea.comprarAsiento(asientoDisponible, usuario);
		verify(aerolineaLanchita).comprar("01202022220202-3", "35247037");
}
}
