package icsrv20192;

import java.awt.geom.Arc2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;

/**
 * Esta clase se encarga del protocolo
 */
public class D implements Callable<ArrayList<Double>> {

	public static final String OK = "OK";
	public static final String ALGORITMOS = "ALGORITMOS";
	public static final String CERTSRV = "CERTSRV";
	public static final String CERCLNT = "CERCLNT";
	public static final String SEPARADOR = ":";
	public static final String HOLA = "HOLA";
	public static final String INICIO = "INICIO";
	public static final String ERROR = "ERROR";
	public static final String REC = "recibio-";
	public static final int numCadenas = 8;

	// Atributos
	private Socket sc = null;
	private String dlg;
	private byte[] mybyte;
	private static File file;
	private static X509Certificate certSer;
	private static KeyPair keyPairServidor;

	//Atributos Analisis
	double time;
	double lost;
	
	public static void init(X509Certificate pCertSer, KeyPair pKeyPairServidor, File pFile) {
		certSer = pCertSer;
		keyPairServidor = pKeyPairServidor;
		file = pFile;
	}
	
	public D (Socket csP, int idP) {
		sc = csP;
		lost=0;
		dlg = new String("delegado " + idP + ": ");
		try {
		mybyte = new byte[520]; 
		mybyte = certSer.getEncoded();
		} catch (Exception e) {
			System.out.println("Error creando encoded del certificado para el thread" + dlg);
			e.printStackTrace();
		}
	}
	
	private boolean validoAlgHMAC(String nombre) {
		return ((nombre.equals(S.HMACMD5) || 
			 nombre.equals(S.HMACSHA1) ||
			 nombre.equals(S.HMACSHA256) ||
			 nombre.equals(S.HMACSHA384) ||
			 nombre.equals(S.HMACSHA512)
			 ));
	}
	
	/*
	 * Generacion del archivo log. 
	 * Nota: 
	 * - Debe conservar el metodo como está. 
	 * - Es el único metodo permitido para escribir en el log.
	 */
	private void escribirMensaje(String pCadena) {
		
		try {
			FileWriter fw = new FileWriter(file,true);
			fw.write(pCadena);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printBase64Binary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseBase64Binary(s);
	}

	public double getSystemCpuLoad() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}

	@Override
	public ArrayList<Double> call() {
		String[] cadenas;
		cadenas = new String[numCadenas];

		String linea;
		System.out.print(dlg + "Empezando atencion.");
		try {

			PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
			BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

			/***** Fase 1:  *****/
			linea = dc.readLine();
			cadenas[0] = "Fase1: ";
			if (!linea.equals(HOLA)) {
				ac.println(ERROR);
				sc.close();
				throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
			} else {
				ac.println(OK);
				cadenas[0] = REC + linea + "- ::: .";
				System.out.print(cadenas[0]);
			}

			/***** Fase 2:  *****/
			linea = dc.readLine();
			cadenas[1] = "Fase2: ";
			if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(ALGORITMOS))) {
				ac.println(ERROR);
				sc.close();
				throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
			}

			String[] algoritmos = linea.split(SEPARADOR);
			if (!algoritmos[1].equals(S.DES) && !algoritmos[1].equals(S.AES) &&
					!algoritmos[1].equals(S.BLOWFISH) && !algoritmos[1].equals(S.RC4)){
				ac.println(ERROR);
				sc.close();
				throw new Exception(dlg + ERROR + "Alg.Simetrico" + REC + algoritmos + "-terminando.");
			}
			if (!algoritmos[2].equals(S.RSA) ) {
				ac.println(ERROR);
				sc.close();
				throw new Exception(dlg + ERROR + "Alg.Asimetrico." + REC + algoritmos + "-terminando.");
			}
			if (!validoAlgHMAC(algoritmos[3])) {
				ac.println(ERROR);
				sc.close();
				throw new Exception(dlg + ERROR + "AlgHash." + REC + algoritmos + "-terminando.");
			}
			cadenas[1] = REC + linea + "- ::: .";
			System.out.print(cadenas[1]);
			ac.println(OK);

			/***** Fase 3:  *****/
			String testCert = toHexString(mybyte);
			ac.println(testCert);
			cadenas[2] ="envio certificado del servidor  ::: ";
			System.out.print(cadenas[2] + testCert);

			/***** Fase 4: *****/
			cadenas[3] = "";
			linea = dc.readLine();
			//Creacion llave Simetrica===================================================|
			time=System.nanoTime();
			byte[] llaveSimetrica = S.ad(
					toByteArray(linea),
					keyPairServidor.getPrivate(), algoritmos[2] );
			SecretKey simetrica = new SecretKeySpec(llaveSimetrica, 0, llaveSimetrica.length, algoritmos[1]);
			cadenas[3] = "recibio y creo llave simetrica  ::: ";
			System.out.print(cadenas[3]);

			/***** Fase 5:  *****/
			cadenas[4]="";
			linea = dc.readLine();
			System.out.print("" + "Recibio reto del cliente:-" + linea + "-");
			byte[] retoByte = toByteArray(linea);
			byte [ ] ciphertext1 = S.se(retoByte, simetrica, algoritmos[1]);
			ac.println(toHexString(ciphertext1));
			System.out.print("" + "envio reto cifrado con llave simetrica al cliente.  ::: .");

			linea = dc.readLine();
			if ((linea.equals(OK))) {
				cadenas[4] = "recibio confirmacion del cliente:"+ linea +"- ::: .";
				System.out.print(cadenas[4]);
			} else {
				sc.close();
				throw new Exception(dlg + ERROR + "en confirmacion de llave simetrica." + REC + "-terminando.");
			}

			/***** Fase 6:  *****/
			linea = dc.readLine();
			byte[] ccByte = S.sd(
					toByteArray(linea), simetrica, algoritmos[1]);
			String cc = toHexString(ccByte);
			System.out.print("" + "recibio cc y descifro:-" + cc + "- ::: .");

			linea = dc.readLine();
			byte[] claveByte = S.sd(
					toByteArray(linea), simetrica, algoritmos[1]);
			String clave = toHexString(claveByte);
			System.out.print("" + "recibio clave y descifro:-" + clave + "- ::: .");
			cadenas[5] = dlg + "recibio cc y clave -  ::: ";

			Random rand = new Random();
			int valor = rand.nextInt(1000000);
			String strvalor = valor+"";
			while (strvalor.length()%4!=0) strvalor += 0;
			byte[] valorByte = toByteArray(strvalor);
			byte [ ] ciphertext2 = S.se(valorByte, simetrica, algoritmos[1]);
			ac.println(toHexString(ciphertext2));
			cadenas[6] = "envio valor "+strvalor+" cifrado con llave simetrica al cliente.  ::: .";
			System.out.print(cadenas[6]);

			byte [] hmac = S.hdg(valorByte, simetrica, algoritmos[3]);
			byte[] recibo = S.ae(hmac, keyPairServidor.getPrivate(), algoritmos[2]);
			ac.println(toHexString(recibo));
			System.out.print("" + "envio hmac cifrado con llave privada del servidor.  ::: .");

			//escritura de HMAC ================================================|
			time=System.nanoTime()-time;
			time=time/1E9;
			cadenas[7] = "";
			linea = dc.readLine();
			if (linea.equals(OK)) {
				cadenas[7] = "Terminando exitosamente." + linea;
				System.out.println(cadenas[7]);
			} else {
				cadenas[7] = "Terminando con error" + linea;
				System.out.println(cadenas[7]);
			}
			sc.close();

			for (int i=0;i<numCadenas;i++) {
				escribirMensaje(cadenas[i]);
			}
		} catch (Exception e) {
			lost=1;
			e.printStackTrace();
		}


		ArrayList<Double> analitics= new ArrayList<>();
		try {
		analitics.add(time);
		analitics.add(getSystemCpuLoad());
		analitics.add(lost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return analitics;
	}
}
