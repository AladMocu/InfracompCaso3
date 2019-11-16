package secure.cliente;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
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
    private Cipher cipherAES;
    private Cipher cipherRSA;


    public Cliente(PrintWriter writer, BufferedReader reader)  {
        this.writer=writer;
        this.reader=reader;
        try {
             cipherAES = Cipher.getInstance("AES");
             cipherRSA = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
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
            //cifrar llave publica del secure.cliente con la publica del servidor
            cipherRSA.init(Cipher.ENCRYPT_MODE,KW);
            byte[] encryptedData = cipherRSA.doFinal(KS.getEncoded());
            writer.println(toHexString(encryptedData));
            String reto= "JuliEsLindaYEstoEsUnReto";
            //enviando reto
            writer.println(reto);
            //recibiendo reto cifrado
            String recibido=reader.readLine();
            cipherAES.init(Cipher.DECRYPT_MODE,KS);
            String respuesta= toHexString(cipherAES.doFinal(toByteArray(recibido)));

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
            cipherAES.init(Cipher.ENCRYPT_MODE,KS);
            byte[] c_cc= cipherAES.doFinal(toByteArray(cc));
            byte[] c_pw= cipherAES.doFinal(toByteArray(password));
            writer.println(toHexString(c_cc));
            writer.println(toHexString(c_pw));
            //recibiendo valor y digest
            String c_valor= reader.readLine();
            cipherAES.init(Cipher.DECRYPT_MODE,KS);
            String valor= toHexString(cipherAES.doFinal(toByteArray(c_valor)));



            String c_digest=reader.readLine();
            cipherRSA.init(Cipher.DECRYPT_MODE,KW);
            byte[] c_digest2=  cipherRSA.doFinal(toByteArray(c_digest));


            Mac mac = Mac.getInstance(ALGORITMOS[6]);
            mac.init(KS);
            byte[] digest= mac.doFinal(toByteArray(valor));



            if(Arrays.equals(c_digest2, digest))
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
