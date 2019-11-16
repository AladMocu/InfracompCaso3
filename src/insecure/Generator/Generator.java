package insecure.Generator;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

import java.io.FileReader;
import java.util.Properties;

public class Generator {
    private LoadGenerator generator;

    public Generator()
    {
        try {

            FileReader reader=new FileReader("./docs/load.properties");
            Properties loadPrpoperties= new Properties();

            loadPrpoperties.load(reader);
            Task work= createTask();
            int numberOfTasks=Integer.parseInt(loadPrpoperties.getProperty("tasknumber"));
            int gapBetweenTasks=Integer.parseInt(loadPrpoperties.getProperty("taskgap"));
            generator= new LoadGenerator("Client - Server Load Test",numberOfTasks,work,gapBetweenTasks);
            generator.generate();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private Task createTask()
    {
        return new ClientServerTask();
    }
    public static void main(String... args)
    {
        @SuppressWarnings("unused")
        Generator gen= new Generator();
    }
}
