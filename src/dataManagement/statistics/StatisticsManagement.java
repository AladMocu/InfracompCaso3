package dataManagement.statistics;


import java.util.ArrayList;

public class StatisticsManagement {
    private ArrayList<Double> times;
    private ArrayList<Double> cpus;
    private int failures;
    private String kind;

    public StatisticsManagement(String kind)
    {
        this.kind=kind;
        times= new ArrayList<>();
        cpus= new ArrayList<>();
        failures=0;
    }

    public void registerValue(ArrayList<Double> values)
    {
        times.add(values.get(0));
        cpus.add(values.get(1));
        if(values.get(3)>0)
        {
            failures++;
        }
    }
}
