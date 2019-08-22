/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbot;

/**
 *
 * @author willi
 */
public class Usuario {

    private String id;
    private String nombre;
    private Sensor sensor;
    private String estado;
    private Sensors sensors;
    
    public Usuario() {
        this.id = "";
        this.nombre = "";
        this.estado = "";
        this.sensor=new Sensor();
        this.sensors=new Sensors();
    }

    public Usuario(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.estado = "";
        this.sensor=new Sensor();
        this.sensors=new Sensors();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensors getSensors() {
        return sensors;
    }

    public void setSensors(Sensors sensors) {
        this.sensors = sensors;
    }

    public void ClearSensor() {
        this.sensor = new Sensor();
    }
    
    public void ClearSensores() {
        this.sensors.clear();
    }
}
