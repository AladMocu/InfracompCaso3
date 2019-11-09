package insecure.Generator;

import insecure.cliente.Cliente;
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
        }
        success();
    }
    @Override
    public void fail()
    {
        System.out.println(Task.MENSAJE_FAIL);
    }

    @Override
    public void success()
    {
        System.out.println(Task.OK_MESSAGE);
    }
}
