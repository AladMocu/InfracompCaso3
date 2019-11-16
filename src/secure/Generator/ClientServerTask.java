package secure.Generator;

import secure.cliente.Cliente;
import uniandes.gload.core.Task;

import java.io.*;
import java.net.Socket;

public class ClientServerTask extends Task {



    @Override
    public void execute()
    {
        try {
            Socket socket = new Socket("localhost", 4200);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            Cliente cliente = new Cliente(writer, reader);

            cliente.start();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
            return;
        }

        success();
    }
    @Override
    public void fail()
    {
        synchronized (Generator.failures)
        {
            Generator.failures+=1;
            System.err.println("Failure!: "+Generator.failures);
        }
    }

    @Override
    public void success()
    {
        System.out.println("Success!");
    }
}
