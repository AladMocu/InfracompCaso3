package insecure.cliente;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class Cliente extends Thread{
    private   PublicKey KW ;
    /**
     *
     */
    public static String[] ALGORITMOS = {"AES", "BLOWFISH", "RSA", "HMACSHA1","HMACSHA256", "HMACSHA384", "HMACSHA512"};

    /**
     *
     */
    public final static String SALUDO="HOLA";

    /**
     *
     */
    public final static String STRALGORITMOS="ALGORITMOS";

    /**
     *
     */
    public final static String CORRECTO="OK";

    /**
     *
     */
    public final static String ERROR="ERROR";
    
    private PrintWriter writer;
    private BufferedReader reader;
    private String algoritmo;

    private X509Certificate certificado;
    private SecretKeySpec KS;



    public Cliente(PrintWriter writer, BufferedReader reader)  {
        this.writer=writer;
        this.reader=reader;
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        KS = new SecretKeySpec(key, "AES");
    }

    public void run()
    {
        try {
            //se envia un saludo al servidor
            writer.println("HOLA");
            //se espera un 'OK' 
            String confirmacion=reader.readLine();

            //se envian los algoritmos conocidos
            String parametros=STRALGORITMOS+":"+ALGORITMOS[0]+":"+ALGORITMOS[2]+":"+ALGORITMOS[6];
            writer.println(parametros);

            //se espera un ok
            confirmacion=reader.readLine();

            if(confirmacion.equals(ERROR))
            {
                return;
            }
            // se recibe el certificado
            String cert = reader.readLine();
            byte[] certificate = toByteArray(cert);
            InputStream in = new ByteArrayInputStream(certificate);
            certificado = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
            KW = certificado.getPublicKey();

            String reto= "retoescritobienfacilalv";
            //enviando reto
            writer.println(KW.getEncoded());

            writer.println(reto);
            //recibiendo reto cifrado
            String respuesta=reader.readLine();

            if(respuesta.equals(reto))
            {
                writer.println(CORRECTO);


            }
            else
            {
                writer.println(ERROR);
                return;
            }
            //envio de datos personales
            String cc= "1007273248";
            String password = "password";
            writer.println(cc);
            writer.println(password);
            //recibiendo valor y digest
            String valor= reader.readLine();



            String c_digest=reader.readLine();


            MessageDigest md = MessageDigest.getInstance("SHA-256");


            byte[] digest= md.digest(valor.getBytes(StandardCharsets.UTF_8));





            if(Arrays.equals(toByteArray(c_digest), digest))
            {
                writer.println(CORRECTO);
            }
            else
            {
                writer.println(ERROR);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String toHexString(byte[] byteArray)
    {
        return DatatypeConverter.printBase64Binary(byteArray);
    }
    public static byte[] toByteArray(String cadena)
    {
        return DatatypeConverter.parseBase64Binary(cadena);
    }
}
