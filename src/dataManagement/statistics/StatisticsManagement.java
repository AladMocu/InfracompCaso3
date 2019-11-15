package dataManagement.statistics;


import java.io.*;
import java.util.ArrayList;

public class StatisticsManagement {
    private ArrayList<Double> times;
    private ArrayList<Double> cpus;
    private int failures;
    private String kind;
    private File file;


    public StatisticsManagement(String kind){
        try{
            this.kind=kind;
            times= new ArrayList<>();
            cpus= new ArrayList<>();
            failures=0;
            file= new File("./docs/"+kind+".csv");


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
