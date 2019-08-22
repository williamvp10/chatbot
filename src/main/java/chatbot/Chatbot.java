package chatbot;

import Services.Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chatbot {

    JsonObject context;
    Service service;
    HashMap<String, Usuario> Usuarios = new HashMap<String, Usuario>();

    //main test
    public static void main(String[] args) throws IOException {
        Chatbot c = new Chatbot();
        Scanner scanner = new Scanner(System.in);
        String userUtterance;
        do {
            System.out.println("User:");
            System.out.print("Id:");
            String id = scanner.nextLine();
            System.out.print("utt:");
            userUtterance = scanner.nextLine();
            System.out.print("type:");
            String type = scanner.nextLine();

            JsonObject userInput = new JsonObject();
            userInput.add("userId", new JsonPrimitive(id));
            userInput.add("userUtterance", new JsonPrimitive(userUtterance));
            userInput.add("userType", new JsonPrimitive(type));
            System.out.println("input:" + userInput);
            JsonObject botOutput = c.process(userInput);
            String botUtterance = "";
            if (botOutput != null && botOutput.has("botUtterance")) {
                botUtterance = botOutput.get("botUtterance").getAsString();
            }
            System.out.println("Bot:" + botUtterance);
        } while (!userUtterance.equals("QUIT"));
        scanner.close();
    }

    public Chatbot() {
        context = new JsonObject();
        service = new Service();
    }

    public String processFB(JsonObject userInput) throws IOException {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject process(JsonObject userInput) throws IOException {
        System.out.println(userInput.toString());
        //this.context = new JsonObject();
        //step1: search user or add
        searchUser(userInput);
        //step2: process user input and identify bot intent
        processUserInput(userInput);
        System.out.println("context " + context.toString());
        //step3: structure output
        JsonObject out = getBotOutput();
        System.out.println("out " + out.toString());
        return out;
    }

    public void searchUser(JsonObject userInput) {
        String userid = "", userName = "";
        //info usuario 
        if (userInput.has("userId")) {
            userid = userInput.get("userId").getAsString();
            userid = userid.replaceAll("%2C", ",");
        }
        if (this.Usuarios.get(userid) != null) {
            System.out.println(this.Usuarios.get(userid).getId());
        } else {
            System.out.println("nuevo");
            JsonObject infouser = null;
            try {
                infouser = service.getUserFB(userid);
            } catch (Exception ex) {
                System.out.println("error al buscar usuario ");
            }
            if (infouser != null) {
                userName = infouser.get("first_name").getAsString();
                this.Usuarios.put(userid, new Usuario(userid, userName));
            }
            System.out.println("user"+infouser);
        }
        context.add("userId", new JsonPrimitive(userid));
        context.add("userName", new JsonPrimitive(userName));

        for (Usuario usuario : this.Usuarios.values()) {
            System.out.println("usuario " + usuario.getId() + " : " + usuario.getNombre());
        }
    }

    /*
    
    --------------------------------------------------------------------------------------------
    
     */
    public void processUserInput(JsonObject userInput) throws IOException {
        String userUtterance = "", userType = "";
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        //default case

        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }
        if (userInput.has("userType")) {
            userType = userInput.get("userType").getAsString();
            userType = userType.replaceAll("%2C", ",");
        }
        if (user.getEstado().equals("texto_alerta")) {
            user.setEstado("");
            if (userUtterance.length() > 0) {
                user.getSensor().setActuador(userUtterance);
                context.add("botIntent", new JsonPrimitive("enviar_alerta_sensor"));
            } else {
                user.getSensor().setActuador(userUtterance);
                context.add("botIntent", new JsonPrimitive("intentError"));
            }
        } else if (userType.length() != 0) {
            System.out.println("userType: " + userType);
            String[] type = userType.split(":");

            if (type[0].equals("listar_estado_sensores")) {
                user.ClearSensor();
                findEstado_sensores(user);
            } else if (type[0].equals("listar_modificar_sensores")) {
                user.ClearSensor();
                findModificar_sensor(user);
            } else if (type[0].equals("listar_alerta_sensores")) {
                user.ClearSensor();
                findAlerta(user);
            } else if (type[0].equals("estado_sensor")) {
                user.ClearSensor();
                findEstado_sensores(user);
                if (type.length > 1) {
                    user.setSensor(user.getSensors().find(type[1]));
                }
            } else if (type[0].equals("pregunta_alerta")) {
                findEstado_sensores(user);
                if (type.length > 1) {
                    user.setSensor(user.getSensors().find(type[1]));
                }
            } else if (type[0].equals("modificar_sensor")) {
                findEstado_sensores(user);
                if (type.length > 1) {
                    user.setSensor(user.getSensors().find(type[1]));
                }
            }
        } else if (userUtterance.length() != 0) {
            System.out.println("userUtterance: " + userUtterance);
            JsonObject intent = null;

            intent = this.service.getIntent(userUtterance);
            String intent_name = "";
            JsonArray entities = null;
            try {
                intent_name = intent.get("intent").getAsJsonObject().get("name").getAsString();
                entities = intent.get("entities").getAsJsonArray();
            } catch (Exception ex) {
                intent_name = "";
            }
            Double confidence = Double.parseDouble(intent.get("intent").getAsJsonObject().get("confidence").getAsString());
            if (confidence > 0.6) {
                saveEntity(user, entities);// validar las entidades encontradas
                if (intent_name.equals("saludo")) {
                    context.add("botIntent", new JsonPrimitive("saludoUsuario"));
                } else if (intent_name.equals("despedida")) {
                    context.add("botIntent", new JsonPrimitive("byeUsuario"));
                } else if (intent_name.equals("menu")) {
                    context.add("botIntent", new JsonPrimitive("menu"));
                } else if (intent_name.equals("agradecimiento")) {
                    context.add("botIntent", new JsonPrimitive("agradecimientoUsuario"));
                } else if (intent_name.equals("estado_sensores")) {
                    findEstado_sensores(user);
                } else if (intent_name.equals("modificar_sensor")) {
                    findModificar_sensor(user);
                } else if (intent_name.equals("Alerta")) {
                    findAlerta(user);
                } else {
                    System.out.println("nlp no pudo procesar el texto");
                    context.add("botIntent", new JsonPrimitive("intentError"));
                }
            } else {
                context.add("botIntent", new JsonPrimitive("intentError"));
            }
        } else {
            context.add("botIntent", new JsonPrimitive("intentError"));
        }
    }

    public void saveEntity(Usuario user, JsonArray entities) {
        if (entities.size() != 0) {
            for (int i = 0; i < entities.size(); i++) {
                JsonObject entity = entities.get(i).getAsJsonObject();
                String nameEntity = entity.get("entity").getAsString();
                String valueEntity = entity.get("value").getAsString();
                Double confidenceEntity = entity.get("confidence").getAsDouble();
                if (confidenceEntity >= 0.7) {
                    if (nameEntity.equals("sensorId")) {
                        user.getSensor().setId(valueEntity);
                    }
                }
            }
        }
    }

    public void findEstado_sensores(Usuario user) {
        if (user.getSensor().getId().length() == 0) {
            context.add("botIntent", new JsonPrimitive("listar_estado_sensores"));
            user.setEstado("infosensores");
        } else {
            context.add("botIntent", new JsonPrimitive("estado_sensor"));
        }
    }

    public void findAlerta(Usuario user) {
        if (user.getSensor().getId().length() == 0) {
            context.add("botIntent", new JsonPrimitive("listar_alerta_sensores"));
        } else {
            context.add("botIntent", new JsonPrimitive("pregunta_alerta"));
            user.setEstado("texto_alerta");
        }
    }

    public void findModificar_sensor(Usuario user) {
        if (user.getSensor().getId().length() == 0) {
            context.add("botIntent", new JsonPrimitive("listar_modificar_sensores"));
            user.setEstado("infosensores");
        } else {
            context.add("botIntent", new JsonPrimitive("modificar_sensor"));
        }
    }

    //bot output
    public JsonObject getBotOutput() throws IOException {
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        String botUtterance = "";
        String type = "";
        if (botIntent.equals("saludoUsuario")) {
            botUtterance = "Hola " + user.getNombre() + " en que te puedo ayudar ";
            type = "texto";
            out = getbotsaludo();
        } else if (botIntent.equals("byeUsuario")) {
            botUtterance = "Adiós " + user.getNombre() + ", que tengas buen día!! ";
            type = "texto";
            out = getbotsaludo();
        } else if (botIntent.equals("menu")) {
            botUtterance = "selecciona un opcion";
            type = "texto";
            out = getbotMenu();
        } else if (botIntent.equals("agradecimientoUsuario")) {
            botUtterance = "con gusto, espero haberte ayudado ";
            type = "texto";
            out = getbotAgradecimiento();
        } else if (botIntent.equals("intenterror")) {
            botUtterance = "disculpa no logre entenderte";
            type = "texto";
            out = getbotsaludo();
        } else if (botIntent.equals("pregunta_alerta")) {
            botUtterance = "escribe el mensaje de alerta";
            type = "texto";
            out = getbotsaludo();
        } else if (botIntent.equals("listar_estado_sensores")) { //servicio
            botUtterance = "estado de los sensores";
            type = "lista";
            out = getbotListarEstadoSensores(user);
            if (out == null) {
                botUtterance = "no hay sensores conectados";
                type = "texto";
                out = getbotsaludo();
            }
        } else if (botIntent.equals("listar_alerta_sensores")) { //servicio
            botUtterance = "escoge sensor al cual vas enviar la alerta";
            type = "lista";
            out = getbotListarAlertaSensores(user);
            if (out == null) {
                botUtterance = "no hay sensores conectados";
                type = "texto";
                out = getbotsaludo();
            }
        } else if (botIntent.equals("listar_modificar_sensores")) { //servicio
            botUtterance = "escoge sensor para modificarlo";
            type = "lista";
            out = getbotListarModificarSensores(user);
            if (out == null) {
                botUtterance = "no hay sensores conectados";
                type = "texto";
                out = getbotsaludo();
            }
        } else if (botIntent.equals("estado_sensor")) { //servicio
            botUtterance = "info del sensor " + user.getSensor().getId();
            type = "lista";
            out = getbotEstado_sensor(user);
            if (out == null) {
                botUtterance = "no se encontro el sensor";
                type = "texto";
                out = getbotsaludo();
            }
        } else if (botIntent.equals("modificar_sensor")) { //servicio
            botUtterance = "sensor modificado";
            type = "lista";
            out = getbotModificar_sensor(user);
            if (out == null) {
                botUtterance = "no se encontro el sensor";
                type = "texto";
                out = getbotsaludo();
            }
        } else if (botIntent.equals("enviar_alerta_sensor")) { //servicio
            botUtterance = "alerta Enviada";
            type = "texto";
            out = getbotEnviar_alerta_sensor(user);
            if (out == null) {
                botUtterance = "no se encontro el sensor";
                type = "texto";
                out = getbotsaludo();
            }
        } else {
            botUtterance = "disculpa no logre entenderte";
            type = "texto";
            out = getbotsaludo();
        }
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
        return out;
    }

    public JsonObject getbotsaludo() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotAgradecimiento() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotMenu() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        b = new JsonObject();
        b.add("titulo", new JsonPrimitive("Estado Sensores"));
        b.add("respuesta", new JsonPrimitive("listar_estado_sensores"));
        buttons.add(b);
        b = new JsonObject();
        b.add("titulo", new JsonPrimitive("Modificar Sensor"));
        b.add("respuesta", new JsonPrimitive("listar_modificar_sensores"));
        buttons.add(b);
        b = new JsonObject();
        b.add("titulo", new JsonPrimitive("Enviar Alerta"));
        b.add("respuesta", new JsonPrimitive("listar_alerta_sensores"));
        buttons.add(b);
        out.add("buttons", buttons);
        return out;
    }

    private JsonObject getbotListarEstadoSensores(Usuario user) {
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
        if (servicio == null) {
            return null;
        } else {
            try {
                JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
                System.out.println(servicio);
                user.getSensors().clear();
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
                    user.getSensors().add(sensor);
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
                    b.add("titulo", new JsonPrimitive("Ver Más"));
                    String var = "" + obj.get("id").getAsString();
                    b.add("respuesta", new JsonPrimitive("estado_sensor:" + var));
                    b1.add(b);
                    e.add("buttons", b1);
                    elements.add(e);
                    System.out.println("elements:" + elements);
                }
            } catch (Exception err) {

                JsonObject obj = servicio.get("sensor").getAsJsonObject();
                user.getSensors().clear();
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
                user.getSensors().add(sensor);
                e = new JsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Ver Más"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("estado_sensor:" + var));
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

    private JsonObject getbotListarAlertaSensores(Usuario user) {
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
        if (servicio == null) {
            return null;
        } else {
            try {
                JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
                System.out.println(servicio);
                user.getSensors().clear();
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
                    user.getSensors().add(sensor);
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
                    b.add("titulo", new JsonPrimitive("Enviar Alerta"));
                    String var = "" + obj.get("id").getAsString();
                    b.add("respuesta", new JsonPrimitive("pregunta_alerta:" + var));
                    b1.add(b);
                    e.add("buttons", b1);
                    elements.add(e);
                    System.out.println("elements:" + elements);
                }
            } catch (Exception err) {
                JsonObject obj = servicio.get("sensor").getAsJsonObject();
                user.getSensors().clear();
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
                user.getSensors().add(sensor);
                e = new JsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Enviar Alerta"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("pregunta_alerta:" + var));
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

    private JsonObject getbotListarModificarSensores(Usuario user) {
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
        if (servicio == null) {
            return null;
        } else {
            try {
                JsonArray elementosServicio = (JsonArray) servicio.get("sensor").getAsJsonArray();
                System.out.println(servicio);
                user.getSensors().clear();
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
                    user.getSensors().add(sensor);
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
                    b.add("titulo", new JsonPrimitive("Modificar"));
                    String var = "" + obj.get("id").getAsString();
                    b.add("respuesta", new JsonPrimitive("modificar_sensor:" + var));
                    b1.add(b);
                    e.add("buttons", b1);
                    elements.add(e);
                    System.out.println("elements:" + elements);
                }
            } catch (Exception err) {

                JsonObject obj = servicio.get("sensor").getAsJsonObject();
                user.getSensors().clear();
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
                user.getSensors().add(sensor);

                e = new JsonObject();
                System.out.println("obj:" + obj);
                e.add("titulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
                e.add("subtitulo", new JsonPrimitive("id: " + obj.get("id").getAsString()));
                e.add("url", new JsonPrimitive("https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive("Modificar"));
                String var = "" + obj.get("id").getAsString();
                b.add("respuesta", new JsonPrimitive("modificar_sensor:" + var));
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

    private JsonObject getbotEstado_sensor(Usuario user) {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        try {
            obj = service.getInfoSensor(user.getSensor().getId());
        } catch (IOException ex) {
            Logger.getLogger(Chatbot.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            e = new JsonObject();
            e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
            e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
            e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
            e.add("buttons", new JsonArray());
            elements.add(e);
        } catch (Exception exp) {
            return null;
        }
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    private JsonObject getbotModificar_sensor(Usuario user) {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        try {
            obj = service.ModificarSensor(user.getSensor());
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            e = new JsonObject();
            e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
            e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
            e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
            e.add("buttons", new JsonArray());
            elements.add(e);
        } catch (Exception exp) {
            return null;
        }
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }

    private JsonObject getbotEnviar_alerta_sensor(Usuario user) {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        try {
            obj = service.ModificarSensor(user.getSensor());
        } catch (IOException ex) {
            Logger.getLogger(Chatbot1.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            e = new JsonObject();
            e.add("titulo", new JsonPrimitive(" temperatura: " + obj.get("temperatura").getAsString() + "  humedad: " + obj.get("humedad").getAsString() + " presion: " + obj.get("presion").getAsString()));
            e.add("subtitulo", new JsonPrimitive("" + "id: " + obj.get("id").getAsString()));
            e.add("url", new JsonPrimitive("" + "https://www.pce-instruments.com/espanol/slot/4/artimg/large/pce-instruments-sensor-de-temperatura-pce-ir-57-5638928_957363.jpg"));
            e.add("buttons", new JsonArray());
            elements.add(e);
        } catch (Exception exp) {
            return null;
        }
        out.add("buttons", buttons);
        out.add("elements", elements);
        return out;
    }
}
