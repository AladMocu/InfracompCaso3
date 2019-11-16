package dataManagement.statistics;


import uniandes.gload.core.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class StatisticsManagement {
    private ArrayList<Double> times;
    private ArrayList<Double> cpus;
    private int failures;
    private String kind;
    private File file;


    public StatisticsManagement(String kind,int poolsize){
        try{
            this.kind=kind;
            times= new ArrayList<>();
            cpus= new ArrayList<>();
            failures=0;

            FileReader reader=new FileReader("./docs/load.properties");
            Properties loadPrpoperties= new Properties();

            loadPrpoperties.load(reader);

            int numberOfTasks=Integer.parseInt(loadPrpoperties.getProperty("tasknumber"));
            int gapBetweenTasks=Integer.parseInt(loadPrpoperties.getProperty("taskgap"));

            file= new File("./docs/punto3/"+kind+"-"+poolsize+"T-"+numberOfTasks+"X-"+gapBetweenTasks+"ms.csv");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void registerValue(ArrayList<Double> values)
    {
        times.add(values.get(0));
        cpus.add(values.get(1));
        if(values.get(2)>0)
        {
            failures++;
        }
        writefile(values.get(0),values.get(1),failures);
    }

    public void writefile(double time, double cpu,double lost)
    {
        try{
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(time+";"+cpu+";"+lost);
            out.close();
            bw.close();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


}
