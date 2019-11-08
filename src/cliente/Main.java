package cliente;

import java.io.*;
import java.net.Socket;

public class Main
{


    public static void main(String[] args)
    {
        for (int i = 0; i <23 ; i++) {
            lanzarCliente();
        }
    }
    public static void lanzarCliente()
    {
        try {
            Socket socket = new Socket("localhost",4200);
            OutputStream output= socket.getOutputStream();
            PrintWriter writer= new PrintWriter(output,true);

            InputStream input =socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            Cliente cliente= new Cliente(writer,reader);

            cliente.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
