package chatbot;

import Services.Service1;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chatbot1 {

    JsonObject context;
    String userName;
    Service1 service;
    Sensor sensor;
    Actuador actuador;
    Sensors sensors;

    public static void main(String[] args) throws IOException {
        Chatbot1 c = new Chatbot1();
        Scanner scanner = new Scanner(System.in);
        String userUtterance;

        do {
            System.out.print("User:");
            userUtterance = scanner.nextLine();

            JsonObject userInput = new JsonObject();
            userInput.add("userUtterance", new JsonPrimitive(userUtterance));
            JsonObject botOutput = c.process(userInput);
            String botUtterance = "";
            if (botOutput != null && botOutput.has("botUtterance")) {
                botUtterance = botOutput.get("botUtterance").getAsString();
            }
            System.out.println("Bot:" + botUtterance);

        } while (!userUtterance.equals("QUIT"));
        scanner.close();
    }

    public Chatbot1() {
        context = new JsonObject();
        userName = "";
        context.add("currentTask", new JsonPrimitive("none"));
        service = new Service1();
        this.sensor = new Sensor();
        this.actuador = new Actuador();
        this.sensors = new Sensors();
    }

    public JsonObject process(JsonObject userInput) throws IOException {
        System.out.println(userInput.toString());
        //step1: process user input
        JsonObject userAction = processUserInput(userInput);

        //step2: update context
        updateContext(userAction);

        //step3: identify bot intent
        identifyBotIntent();
        System.out.println("context " + context.toString());
        //step4: structure output
        JsonObject out = getBotOutput();
        System.out.println("out " + out.toString());
        return out;
    }

    public String processFB(JsonObject userInput) throws IOException {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject processUserInput(JsonObject userInput) throws IOException {
        String userUtterance = null;
        JsonObject userAction = new JsonObject();

        //info usuario 
        if (userInput.has("userName")) {
            userName = userInput.get("userName").getAsString();;
        }
        System.out.println("user: " + userName);
        //default case
        userAction.add("userIntent", new JsonPrimitive(""));
        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }

        if (userUtterance.matches("Hola|hola|buenos dias|buenas tardes")) {
            userAction.add("userIntent", new JsonPrimitive("intentsaludo"));
        } else if (userUtterance.matches("opciones|menu")) {
            userAction.add("userIntent", new JsonPrimitive("intentopciones"));
        } else if (userUtterance.matches("gracias")) {
            userAction.add("userIntent", new JsonPrimitive("intentagradecimiento"));
        } else {
            String userType = " n ";
            if (userInput.has("userType")) {
                userType = userInput.get("userType").getAsString();
                userType = userType.replaceAll("%2C", ",");
            }

            if (userType != null) {
                String[] entrada = userType.split(":");
                if (entrada[0].trim().equals("requestSaludo")) {
                    userAction.add("userIntent", new JsonPrimitive("intentSaludo"));
                } else if (entrada[0].trim().equals("requestMenuOpciones")) {
                    userAction.add("userIntent", new JsonPrimitive("intentMenuOpciones"));
                } else if (entrada[0].trim().equals("requestAllSensors")) {
                    userAction.add("userIntent", new JsonPrimitive("intentAllSensors"));
                } else if (entrada[0].trim().equals("requestIdSensor")) {
                    userAction.add("userIntent", new JsonPrimitive("intentIdSensor"));
                } else if (entrada[0].trim().equals("requestInfoSensor")) {
                    userAction.add("userIntent", new JsonPrimitive("intentInfoSensor"));
                } else if (entrada[0].trim().equals("requestEstadoActuador")) {
                    userAction.add("userIntent", new JsonPrimitive("intentEstadoActuador"));
                } else if (entrada[0].trim().equals("requestModificarActuador")) {
                    this.sensor.setActuador(userUtterance);
                    userAction.add("userIntent", new JsonPrimitive("intentModificarActuador"));
                } else if (entrada[0].trim().equals("requestagradecimiento")) {
                    userAction.add("userIntent", new JsonPrimitive("intentagradecimiento"));
                } else if (entrada[0].trim().equals("requestModificarSensor")) {
                    userAction.add("userIntent", new JsonPrimitive("intentModificarSensor"));
                } else if (entrada[0].trim().equals("requestIdSensor2")) {
                    userAction.add("userIntent", new JsonPrimitive("intentIdSensor2"));
                } else if (entrada[0].trim().equals("requestIdSensor3")) {
                    userAction.add("userIntent", new JsonPrimitive("intentIdSensor3"));
                } else if (entrada[0].trim().equals("requestPreguntaEstadoActuador")) {
                    userAction.add("userIntent", new JsonPrimitive("intentPreguntaEstadoActuador"));
                } else {
                    userAction.add("userIntent", new JsonPrimitive("intenterror"));
                }

                if (entrada.length > 1) {
                    if (entrada[1].equals("IdSensor")) {
                        if (entrada.length > 2) {
                            this.sensor = this.sensors.find(entrada[2]);
                        }
                    }
                    if (entrada[1].equals("IdSensor2")) {
                        if (entrada.length > 2) {
                            this.sensor = this.sensors.find(entrada[2]);
                            this.sensor.setTemperatura("" + (Double.parseDouble(this.sensor.getTemperatura()) + 3));
                            System.out.println(this.sensor.getTemperatura());
                            this.sensor.setHumedad("" + (Double.parseDouble(this.sensor.getHumedad()) + 3));
                            this.sensor.setPresion("" + (Double.parseDouble(this.sensor.getPresion()) + 3));
                            System.out.println("sensor: " + this.sensor.getId() + " " + this.sensor.getTemperatura() + " " + this.sensor.toString());
                        }
                    }
                    if (entrada[1].equals("IdSensor3")) {
                        if (entrada.length > 2) {
                            this.sensor = this.sensors.find(entrada[2]);
                            System.out.println("sensor: " + this.sensor.getId() + " " + this.sensor.getTemperatura() + " " + this.sensor.toString());
                        }
                    }
                }
            }
        }
        return userAction;
    }

    public void updateContext(JsonObject userAction) {
        //copy userIntent
        context.add("userIntent", userAction.get("userIntent"));
        String userIntent = context.get("userIntent").getAsString();

        if (userIntent.equals("intenterror")) {
            context.add("currentTask", new JsonPrimitive("taskerror"));
        } else if (userIntent.equals("intentsaludo")) {
            context.add("currentTask", new JsonPrimitive("taskSaludo"));
        } else if (userIntent.equals("intentopciones")) {
            context.add("currentTask", new JsonPrimitive("taskMenuOpciones"));
        } else if (userIntent.equals("intentagradecimiento")) {
            context.add("currentTask", new JsonPrimitive("taskagradecimiento"));
        } else if (userIntent.equals("intentSaludo")) {
            context.add("currentTask", new JsonPrimitive("taskSaludo"));
        } else if (userIntent.equals("intentMenuOpciones")) {
            context.add("currentTask", new JsonPrimitive("taskMenuOpciones"));
        } else if (userIntent.equals("intentAllSensors")) {
            context.add("currentTask", new JsonPrimitive("taskAllSensors"));
        } else if (userIntent.equals("intentIdSensor")) {
            context.add("currentTask", new JsonPrimitive("taskIdSensor"));
        } else if (userIntent.equals("intentInfoSensor")) {
            context.add("currentTask", new JsonPrimitive("taskInfoSensor"));
        } else if (userIntent.equals("intentEstadoActuador")) {
            context.add("currentTask", new JsonPrimitive("taskEstadoActuador"));
        } else if (userIntent.equals("intentModificarActuador")) {
            context.add("currentTask", new JsonPrimitive("taskModificarActuador"));
        } else if (userIntent.equals("intentagradecimiento")) {
            context.add("currentTask", new JsonPrimitive("taskagradecimiento"));
        } else if (userIntent.equals("intentModificarSensor")) {
            context.add("currentTask", new JsonPrimitive("taskModificarSensor"));
        } else if (userIntent.equals("intentIdSensor2")) {
            context.add("currentTask", new JsonPrimitive("taskIdSensor2"));
        } else if (userIntent.equals("intentIdSensor3")) {
            context.add("currentTask", new JsonPrimitive("taskIdSensor3"));
        } else if (userIntent.equals("intentPreguntaEstadoActuador")) {
            context.add("currentTask", new JsonPrimitive("taskPreguntaEstadoActuador"));
        }
    }

    public void identifyBotIntent() {
        String currentTask = context.get("currentTask").getAsString();
        if (currentTask.equals("taskerror")) {
            context.add("botIntent", new JsonPrimitive("boterror"));
        } else if (currentTask.equals("tasksaludo")) {
            context.add("botIntent", new JsonPrimitive("botsaludo"));
        } else if (currentTask.equals("taskopciones")) {
            context.add("botIntent", new JsonPrimitive("botopciones"));
        } else if (currentTask.equals("taskagradecimiento")) {
            context.add("botIntent", new JsonPrimitive("botagradecimiento"));
        } else if (currentTask.equals("taskSaludo")) {
            context.add("botIntent", new JsonPrimitive("botSaludo"));
        } else if (currentTask.equals("taskMenuOpciones")) {
            context.add("botIntent", new JsonPrimitive("botMenuOpciones"));
        } else if (currentTask.equals("taskAllSensors")) {
            context.add("botIntent", new JsonPrimitive("botAllSensors"));
        } else if (currentTask.equals("taskIdSensor")) {
            context.add("botIntent", new JsonPrimitive("botIdSensor"));
        } else if (currentTask.equals("taskInfoSensor")) {
            context.add("botIntent", new JsonPrimitive("botInfoSensor"));
        } else if (currentTask.equals("taskEstadoActuador")) {
            context.add("botIntent", new JsonPrimitive("botEstadoActuador"));
        } else if (currentTask.equals("taskModificarActuador")) {
            context.add("botIntent", new JsonPrimitive("botModificarActuador"));
        } else if (currentTask.equals("taskagradecimiento")) {
            context.add("botIntent", new JsonPrimitive("botagradecimiento"));
        } else if (currentTask.equals("taskModificarSensor")) {
            context.add("botIntent", new JsonPrimitive("botModificarSensor"));
        } else if (currentTask.equals("taskIdSensor2")) {
            context.add("botIntent", new JsonPrimitive("botIdSensor2"));
        } else if (currentTask.equals("taskIdSensor3")) {
            context.add("botIntent", new JsonPrimitive("botIdSensor3"));
        } else if (currentTask.equals("taskPreguntaEstadoActuador")) {
            context.add("botIntent", new JsonPrimitive("botPreguntaEstadoActuador"));
        }
    }

    public JsonObject getBotOutput() throws IOException {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonArray buttons = new JsonArray();
        String botUtterance = "";
        String type = "";

        if (botIntent.equals("boterror")) {
            botUtterance = "no puedo ayudarle con su solicitud ";
            type = "error";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("botsaludo") || botIntent.equals("botSaludo")) {
            type = "Saludo";
            botUtterance = "hola " + userName + " en que puedo ayudarte?";
            out = getbotSaludo();
        } else if (botIntent.equals("botopciones") || botIntent.equals("botMenuOpciones")) {
            type = "MenuOpciones";
            botUtterance = "opciones:";
            out = (JsonObject) getbotMenuOpciones();
        } else if (botIntent.equals("botAllSensors")) {
            type = "AllSensors";
            botUtterance = "todos los sensores";
            out = getbotAllSensors();
        } else if (botIntent.equals("botIdSensor")) {
            type = "IdSensor";
            botUtterance = "escoja sensor";
            out = getbotIdSensor();
            if (out == null) {
                botUtterance = "escoja sensor";
                out = getbotSaludo();
            } else {
                botUtterance = "escoja sensor";
            }
        } else if (botIntent.equals("botInfoSensor")) {
            type = "InfoSensor";
            botUtterance = "info sensores";
            out = getbotInfoSensor();
        } else if (botIntent.equals("botEstadoActuador")) {
            type = "EstadoActuador";
            botUtterance = "estado actuador";
            out = getbotEstadoActuador();
        } else if (botIntent.equals("botModificarActuador")) {
            type = "ModificarActuador";
            botUtterance = "actuador modificado";
            out = getbotModificarActuador();
        } else if (botIntent.equals("botagradecimiento") || botIntent.equals("botagradecimiento")) {
            type = "agradecimiento";
            botUtterance = "con gusto :)";
            out = getbotagradecimiento();
        } else if (botIntent.equals("botModificarSensor")) {
            type = "ModificarSensor";
            botUtterance = "se a modificado el sensor";
            out = getbotModificarSensor();
        } else if (botIntent.equals("botIdSensor2")) {
            type = "IdSensor2";
            out = getbotIdSensor2();
            if (out == null) {
                botUtterance = "escoja sensor";
                out = getbotSaludo();
            } else {
                botUtterance = "escoja sensor";
            }
        } else if (botIntent.equals("botIdSensor3")) {
            type = "IdSensor2";
            botUtterance = "escoja sensor";
            out = getbotIdSensor3();
        } else if (botIntent.equals("botPreguntaEstadoActuador")) {
            type = "PreguntaEstadoActuador";
            botUtterance = "digite el mensaje";
            out.add("buttons", buttons);
        }

        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
        return out;
    }

    public JsonObject getbotSaludo() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotMenuOpciones() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonArray elements = new JsonArray();
        JsonObject b = new JsonObject();
        JsonArray b1 = new JsonArray();
        JsonObject e = new JsonObject();
        b.add("titulo", new JsonPrimitive("seleccionar"));
        b.add("respuesta", new JsonPrimitive("requestAllSensors"));
        b1.add(b);
        e.add("titulo", new JsonPrimitive("estado de los sensores"));
        e.add("subtitulo", new JsonPrimitive("opcion 1"));
        e.add("buttons", b1);
        elements.add(e);
        b = new JsonObject();
        b1 = new JsonArray();
        e = new JsonObject();
        b.add("titulo", new JsonPrimitive("seleccionar"));
        b.add("respuesta", new JsonPrimitive("requestIdSensor"));
        e.add("titulo", new JsonPrimitive("estado de un sensor"));
        e.add("subtitulo", new JsonPrimitive("opcion 2"));
        b1.add(b);
        e.add("buttons", b1);
        elements.add(e);

        b = new JsonObject();
        b1 = new JsonArray();
        e = new JsonObject();
        b.add("titulo", new JsonPrimitive("seleccionar"));
        b.add("respuesta", new JsonPrimitive("requestIdSensor2"));
        e.add("titulo", new JsonPrimitive("modificar sensor"));
        e.add("subtitulo", new JsonPrimitive("opcion 3"));
        b1.add(b);
        e.add("buttons", b1);
        elements.add(e);

        b = new JsonObject();
        b1 = new JsonArray();
        e = new JsonObject();
        b.add("titulo", new JsonPrimitive("seleccionar"));
        b.add("respuesta", new JsonPrimitive("requestIdSensor3"));
        e.add("titulo", new JsonPrimitive("modificar Actuador"));
        e.add("subtitulo", new JsonPrimitive("opcion 4"));
        b1.add(b);
        e.add("buttons", b1);
        elements.add(e);
//        b = new JsonObject();
//        b1 = new JsonArray();
//        e = new JsonObject();
//        b.add("titulo", new JsonPrimitive("seleccionar"));
//        b.add("respuesta", new JsonPrimitive("requestEstadoActuador"));
//        e.add("titulo", new JsonPrimitive("estado Actuador"));
//        e.add("subtitulo", new JsonPrimitive("opcion 5"));
//        b1.add(b);
//        e.add("buttons", b1);
//        elements.add(e);
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotAllSensors() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = null;
        try {
            servicio = service.getAllSensors();
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                System.out.println("obj:" + obj);
                System.out.println(" " + obj.get("id").getAsString());
                e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString()
                        + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
                e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                e.add("buttons", new JsonArray());
                elements.add(e);
                System.out.println(" elementos " + elements);
            }
        } catch (Exception err) {
            e = new JsonObject();
            JsonObject obj = servicio.get("sensor").getAsJsonObject();
            System.out.println("obj:" + obj);
            System.out.println(" " + obj.get("id").getAsString());
            e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString()
                    + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
            e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
            e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
            e.add("buttons", new JsonArray());
            elements.add(e);
            System.out.println(" elementos " + elements);
        }

        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotIdSensor() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = null;
        try {
            servicio = service.getIdSensor();
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (servicio == null) {
            return null;
        } else {
            try {
                JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
                System.out.println("salio");
                System.out.println(servicio);
                this.sensors = new Sensors();
                for (int i = 0; i < elementosServicio.size(); i++) {
                    JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                    Sensor sensor = new Sensor();
                    sensor.setId(obj.get("id").getAsString());
                    sensor.setHumedad(obj.get("humedad").getAsString());
                    sensor.setTemperatura(obj.get("temperatura").getAsString());
                    sensor.setPresion(obj.get("presion").getAsString());
                    sensor.setFecha(obj.get("fecha").getAsString());
                    sensor.setEjex(obj.get("ejex").getAsString());
                    sensor.setEjey(obj.get("ejey").getAsString());
                    sensor.setEjez(obj.get("ejez").getAsString());
                    this.sensors.add(sensor);
                }
                for (int i = 0; i < elementosServicio.size(); i++) {
                    e = new JsonObject();
                    JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                    System.out.println("obj:" + obj);
                    e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                    e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                    e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                    b = new JsonObject();
                    b1 = new JsonArray();
                    b.add("titulo", new JsonPrimitive("Seleccionar"));
                    String var = "" + obj.get("id").getAsString();
                    b.add("respuesta", new JsonPrimitive("requestInfoSensor:IdSensor:" + var));
                    b1.add(b);
                    e.add("buttons", b1);
                    elements.add(e);
                    System.out.println("elements:" + elements);
                }
            } catch (Exception err) {
                this.sensors = new Sensors();
                JsonObject obj = servicio.get("sensor").getAsJsonObject();
                Sensor sensor = new Sensor();
                sensor.setId(obj.get("id").getAsString());
                sensor.setHumedad(obj.get("humedad").getAsString());
                sensor.setTemperatura(obj.get("temperatura").getAsString());
                sensor.setPresion(obj.get("presion").getAsString());
                sensor.setFecha(obj.get("fecha").getAsString());
                sensor.setEjex(obj.get("ejex").getAsString());
                sensor.setEjey(obj.get("ejey").getAsString());
                sensor.setEjez(obj.get("ejez").getAsString());
                this.sensors.add(sensor);
                e = new JsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Seleccionar"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("requestInfoSensor:IdSensor:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
                System.out.println("elements:" + elements);
            }
        }
        out.add("elements", elements);
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    public JsonObject getbotInfoSensor() throws IOException {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = service.getInfoSensor(this.sensor.getId());
        e = new JsonObject();
        e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
        e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
        e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
        e.add("buttons", new JsonArray());
        elements.add(e);

        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    public JsonObject getbotEstadoActuador() throws IOException {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = service.getEstadoActuador();
        JsonObject obj = (JsonObject) servicio.get("actuador").getAsJsonObject();
        e = new JsonObject();
        e.add("titulo", new JsonPrimitive("" + " " + obj.get("estado").getAsString()));
        e.add("subtitulo", new JsonPrimitive("" + "valor: " + obj.get("valor").getAsString()));
        e.add("url", new JsonPrimitive(obj.get("url").getAsString()));
        e.add("buttons", new JsonArray());
        elements.add(e);
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    public JsonObject getbotModificarActuador() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        try {
            obj = service.getModificarSensor(this.sensor);
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        e = new JsonObject();
        e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
        e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
        e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
        e.add("buttons", new JsonArray());
        elements.add(e);
        out.add("buttons", buttons);
        out.add("elements", elements);

        return out;
    }

    public JsonObject getbotagradecimiento() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotModificarSensor() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        try {
            obj = service.getModificarSensor(this.sensor);
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        e = new JsonObject();
        e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
        e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
        e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
        e.add("buttons", new JsonArray());
        elements.add(e);
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    public JsonObject getbotIdSensor2() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = null;
        try {
            servicio = service.getIdSensor();
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (servicio == null) {
            return null;
        } else {
            try {
                JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
                System.out.println("salio");
                System.out.println(servicio);
                this.sensors = new Sensors();
                for (int i = 0; i < elementosServicio.size(); i++) {
                    JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                    Sensor sensor = new Sensor();
                    sensor.setId(obj.get("id").getAsString());
                    sensor.setHumedad(obj.get("humedad").getAsString());
                    sensor.setTemperatura(obj.get("temperatura").getAsString());
                    sensor.setPresion(obj.get("presion").getAsString());
                    sensor.setFecha(obj.get("fecha").getAsString());
                    sensor.setEjex(obj.get("ejex").getAsString());
                    sensor.setEjey(obj.get("ejey").getAsString());
                    sensor.setEjez(obj.get("ejez").getAsString());
                    this.sensors.add(sensor);
                }
                for (int i = 0; i < elementosServicio.size(); i++) {
                    e = new JsonObject();
                    JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                    System.out.println("obj:" + obj);
                    e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                    e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                    e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                    b = new JsonObject();
                    b1 = new JsonArray();
                    b.add("titulo", new JsonPrimitive("Seleccionar"));
                    String var = "" + obj.get("id").getAsString();
                    b.add("respuesta", new JsonPrimitive("requestModificarSensor:IdSensor2:" + var));
                    b1.add(b);
                    e.add("buttons", b1);
                    elements.add(e);
                    System.out.println("elements:" + elements);
                }
            } catch (Exception err) {
                this.sensors = new Sensors();
                JsonObject obj = servicio.get("sensor").getAsJsonObject();
                Sensor sensor = new Sensor();
                sensor.setId(obj.get("id").getAsString());
                sensor.setHumedad(obj.get("humedad").getAsString());
                sensor.setTemperatura(obj.get("temperatura").getAsString());
                sensor.setPresion(obj.get("presion").getAsString());
                sensor.setFecha(obj.get("fecha").getAsString());
                sensor.setEjex(obj.get("ejex").getAsString());
                sensor.setEjey(obj.get("ejey").getAsString());
                sensor.setEjez(obj.get("ejez").getAsString());
                this.sensors.add(sensor);
                e = new JsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Seleccionar"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("requestModificarSensor:IdSensor2:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
                System.out.println("elements:" + elements);
            }
        }
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;

    }

    public JsonObject getbotIdSensor3() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = null;
        try {
            servicio = service.getIdSensor();
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
            System.out.println("salio");
            System.out.println(servicio);
            this.sensors = new Sensors();
            for (int i = 0; i < elementosServicio.size(); i++) {
                JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                Sensor sensor = new Sensor();
                sensor.setId(obj.get("id").getAsString());
                sensor.setHumedad(obj.get("humedad").getAsString());
                sensor.setTemperatura(obj.get("temperatura").getAsString());
                sensor.setPresion(obj.get("presion").getAsString());
                sensor.setFecha(obj.get("fecha").getAsString());
                sensor.setEjex(obj.get("ejex").getAsString());
                sensor.setEjey(obj.get("ejey").getAsString());
                sensor.setEjez(obj.get("ejez").getAsString());
                sensor.setActuador(obj.get("actuador").getAsString());
                this.sensors.add(sensor);
            }
            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                JsonObject obj = elementosServicio.get(i).getAsJsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Seleccionar"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("requestPreguntaEstadoActuador:IdSensor3:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
                System.out.println("elements:" + elements);
            }
        } catch (Exception err) {
            this.sensors = new Sensors();
            JsonObject obj = servicio.get("sensor").getAsJsonObject();
            Sensor sensor = new Sensor();
            sensor.setId(obj.get("id").getAsString());
            sensor.setHumedad(obj.get("humedad").getAsString());
            sensor.setTemperatura(obj.get("temperatura").getAsString());
            sensor.setPresion(obj.get("presion").getAsString());
            sensor.setFecha(obj.get("fecha").getAsString());
            sensor.setEjex(obj.get("ejex").getAsString());
            sensor.setEjey(obj.get("ejey").getAsString());
            sensor.setEjez(obj.get("ejez").getAsString());
            sensor.setActuador(obj.get("actuador").getAsString());
            this.sensors.add(sensor);
            e = new JsonObject();
            System.out.println("obj:" + obj);
            e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
            e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
            e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
            b = new JsonObject();
            b1 = new JsonArray();
            b.add("titulo", new JsonPrimitive("Seleccionar"));
            String var = "" + obj.get("id").getAsString();
            b.add("respuesta", new JsonPrimitive("requestPreguntaEstadoActuador:IdSensor3:" + var));
            b1.add(b);
            e.add("buttons", b1);
            elements.add(e);
            System.out.println("elements:" + elements);
        }
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }
}
