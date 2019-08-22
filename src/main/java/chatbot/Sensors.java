package chatbot;

import java.util.ArrayList;

public class Sensors {

    private ArrayList<Sensor> sensor;

    public Sensors() {
        this.sensor= new ArrayList<>();
    }

    public Sensors(ArrayList sensor) {
        this.sensor=sensor;
    }

    public ArrayList<Sensor> getSensor() {
        return sensor;
    }

    public void setSensor(ArrayList<Sensor> sensor) {
        this.sensor = sensor;
    }
    
    public Sensor find(String id){
        Sensor s=null;
        for (int i = 0; i < this.sensor.size(); i++) {
            if(id.equals(this.sensor.get(i).getId())){
                s=this.sensor.get(i);
                break;
            }
        }
        return s;
    }
    
    public void add(Sensor s){
        this.sensor.add(s);
    }
    public void clear(){
        this.sensor.clear();
    }
}
