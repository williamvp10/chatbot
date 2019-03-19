package weatherman.chatbot;

import Services.Service1;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import java.util.ArrayList;

public class Chatbot {

    JsonObject context;
    Service1 service;

    Productor cProducto;
    ArrayList<Ingredientes> cIngredientes;
    Tiendas cTiendas;

    public static void main(String[] args) throws IOException {
        Chatbot c = new Chatbot();
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

    public Chatbot() {
        context = new JsonObject();
        context.add("currentTask", new JsonPrimitive("none"));
        service = new Service1();
        this.cProducto = new Productor();
        this.cIngredientes = new ArrayList<>();
        this.cTiendas = new Tiendas();
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
        //default case
        userAction.add("userIntent", new JsonPrimitive(""));
        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }

        if (userUtterance.matches("hola")) {
            userAction.add("userIntent", new JsonPrimitive("intenthola"));
        } else {
            String userType = " ";
            if (userInput.has("userType")) {
                 System.out.println("type::: "+ userInput.get("userType").getAsString());
                userType = userInput.get("userType").getAsString();
                System.out.println("type "+ userType);
                userType = userType.replaceAll("%2C", ",");
            }
            System.out.println("type "+ userType);
            if (userType != null) {
                String[] entrada = userType.split(":");
                System.out.println("type "+ entrada[0]);
                if (entrada[0].trim().equals("requestProducto")) {
                    userAction.add("userIntent", new JsonPrimitive("intentProducto"));
                } else if (entrada[0].trim().equals("requestIngredientes")) {
                    userAction.add("userIntent", new JsonPrimitive("intentIngredientes"));
                } else if (entrada[0].trim().equals("requestTiendas")) {
                    userAction.add("userIntent", new JsonPrimitive("intentTiendas"));
                } else if (entrada[0].trim().equals("requestfinalizar")) {
                    userAction.add("userIntent", new JsonPrimitive("intentfinalizar"));
                } else if (entrada[0].trim().equals("requestResultados")) {
                    userAction.add("userIntent", new JsonPrimitive("intentResultados"));
                } else {
                    userAction.add("userIntent", new JsonPrimitive("intenterror"));
                }
                if (entrada.length > 1) {
                    if (entrada[1].equals("Producto")) {
                        context.add("Producto", new JsonPrimitive(userUtterance));
                        if (entrada.length > 2) {
                            this.cProducto = new Productor();
                            String[] data = entrada[2].split("--");
                            this.cProducto.settipo(data[0].split("-")[1]);
                            this.cProducto.setprecio(data[1].split("-")[1]);
                        }
                    }
                    if (entrada[1].equals("Ingredientes")) {
                        context.add("Ingredientes", new JsonPrimitive(userUtterance));
                        this.cIngredientes.clear();
                        String[] data = userUtterance.split(",");
                        for (int i = 0; i < data.length; i++) {
                            String[] data1 = data[i].split("--");
                            Ingredientes obj = new Ingredientes();
                            obj.setingredientes(data1[0].split("-")[1]);
                            obj.setprecio(data1[1].split("-")[1]);
                            this.cIngredientes.add(obj);
                        }
                    }
                    if (entrada[1].equals("Tiendas")) {
                        context.add("Tiendas", new JsonPrimitive(userUtterance));
                        if (entrada.length > 2) {
                            this.cTiendas = new Tiendas();
                            String[] data = entrada[2].split("--");
                            this.cTiendas.setid(data[0].split("-")[1]);
                            this.cTiendas.setnombre(data[1].split("-")[1]);
                            this.cTiendas.setdireccion(data[2].split("-")[1]);
                            this.cTiendas.seturl(data[3].split("-")[1]);
                            this.cTiendas.settelefono(data[4].split("-")[1]);
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
        } else if (userIntent.equals("intentResultados")) {
            context.add("currentTask", new JsonPrimitive("taskResultados"));
        } else if (userIntent.equals("intenthola")) {
            context.add("currentTask", new JsonPrimitive("taskProducto"));
        } else if (userIntent.equals("intentProducto")) {
            context.add("currentTask", new JsonPrimitive("taskProducto"));
        } else if (userIntent.equals("intentIngredientes")) {
            context.add("currentTask", new JsonPrimitive("taskIngredientes"));
        } else if (userIntent.equals("intentTiendas")) {
            context.add("currentTask", new JsonPrimitive("taskTiendas"));
        } else if (userIntent.equals("intentfinalizar")) {
            context.add("currentTask", new JsonPrimitive("taskfinalizar"));
        }
    }

    public void identifyBotIntent() {
        String currentTask = context.get("currentTask").getAsString();
        if (currentTask.equals("taskerror")) {
            context.add("botIntent", new JsonPrimitive("boterror"));
        } else if (currentTask.equals("taskResultados")) {
            context.add("botIntent", new JsonPrimitive("botResultados"));
        } else if (currentTask.equals("taskhola")) {
            context.add("botIntent", new JsonPrimitive("bothola"));
        } else if (currentTask.equals("taskProducto")) {
            context.add("botIntent", new JsonPrimitive("botProducto"));
        } else if (currentTask.equals("taskIngredientes")) {
            context.add("botIntent", new JsonPrimitive("botIngredientes"));
        } else if (currentTask.equals("taskTiendas")) {
            context.add("botIntent", new JsonPrimitive("botTiendas"));
        } else if (currentTask.equals("taskfinalizar")) {
            context.add("botIntent", new JsonPrimitive("botfinalizar"));
        }
    }

    public JsonObject getBotOutput() throws IOException {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonArray buttons = new JsonArray();
        String botUtterance = "";
        String type = "";

        if (botIntent.equals("boterror")) {
            botUtterance = "error! ";
            type = "error";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("bothola")) {
            botUtterance = "hola que deseas en este instante? ";
            type = "Producto";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("botProducto")) {
            type = "Producto";
            botUtterance = "hola que deseas en este instante?";
            JsonObject b = null;
            JsonArray b1 = null;
            JsonArray elements = new JsonArray();
            JsonObject e = null;
            JsonObject obj = null;
            JsonObject servicio = service.getProducto();
            JsonArray elementosServicio = (JsonArray) servicio.get("product").getAsJsonArray();

            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                obj = elementosServicio.get(i).getAsJsonObject();
                e.add("titulo", new JsonPrimitive("" + "" + obj.get("tipo").getAsString()));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive(obj.get("tipo").getAsString()));
                String var = "" + "tipo-" + obj.get("tipo").getAsString() + "--" + "precio-" + obj.get("precio").getAsString();
                b.add("respuesta", new JsonPrimitive("requestIngredientes:Producto:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
            }
            out.add("elements", elements);
        } else if (botIntent.equals("botIngredientes")) {
            type = "Ingredientes";
            botUtterance = "seleccione Ingredientes";
            JsonObject b = null;
            JsonArray b1 = null;
            JsonArray elements = new JsonArray();
            JsonObject e = null;
            JsonObject obj = null;
            JsonObject servicio = service.getIngredientes(this.cProducto.gettipo());
            JsonArray elementosServicio = (JsonArray) servicio.get("product").getAsJsonArray();

            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                obj = elementosServicio.get(i).getAsJsonObject();
                e.add("titulo", new JsonPrimitive("" + "" + obj.get("ingredientes").getAsString()));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive(obj.get("ingredientes").getAsString()));
                String var = "" + "ingredientes-" + obj.get("ingredientes").getAsString() + "--" + "precio-" + obj.get("precio").getAsString();
                b.add("respuesta", new JsonPrimitive("add Ingredientes:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
            }
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("enviar"));
            b.add("respuesta", new JsonPrimitive("requestTiendas:Ingredientes"));
            buttons.add(b);
            out.add("elements", elements);
        } else if (botIntent.equals("botTiendas")) {
            type = "Tiendas";
            botUtterance = "estas son las tiendas que ofrecen el producto que deseas";
            JsonObject b = null;
            JsonArray b1 = null;
            JsonArray elements = new JsonArray();
            JsonObject e = null;
            JsonObject obj = null;
            JsonObject servicio = service.getTiendas(this.cProducto.gettipo(), getIngredientesingredientes());
            JsonArray elementosServicio = (JsonArray) servicio.get("tienda").getAsJsonArray();

            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                obj = elementosServicio.get(i).getAsJsonObject();
                e.add("titulo", new JsonPrimitive("" + "" + obj.get("nombre").getAsString()));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive(obj.get("nombre").getAsString()));
                String var = "" + "id-" + obj.get("id").getAsString() + "--" + "nombre-" + obj.get("nombre").getAsString() + "--" + "direccion-" + obj.get("direccion").getAsString() + "--" + "url-" + " " + "--" + "telefono-" + obj.get("telefono").getAsString();
                b.add("respuesta", new JsonPrimitive("requestResultados:Tiendas:" + var));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
            }
            out.add("elements", elements);
        } else if (botIntent.equals("botfinalizar")) {
            type = "finalizar";
            botUtterance = "tu pedido a sido procesado";
            JsonObject b = null;
        } else if (botIntent.equals("botResultados")) {
            type = "Resultados";
            botUtterance = "desea confirmar pedido?";
            JsonObject b = null;
            JsonObject OInformeProducto = new JsonObject();
            OInformeProducto.add("text", new JsonPrimitive("" + "el producto es: " + this.cProducto.gettipo()));
            out.add("InformeProducto", OInformeProducto);
            JsonObject OInformeIngredientes = new JsonObject();
            OInformeIngredientes.add("text", new JsonPrimitive("" + "ingredientes: " + getIngredientesingredientes()));
            out.add("InformeIngredientes", OInformeIngredientes);
            JsonObject OInformeTienda = new JsonObject();
            OInformeTienda.add("text", new JsonPrimitive("" + "Tienda: " + this.cTiendas.getnombre()));
            out.add("InformeTienda", OInformeTienda);
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("Si"));
            b.add("respuesta", new JsonPrimitive("requestfinalizar"));
            buttons.add(b);
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("No"));
            b.add("respuesta", new JsonPrimitive("requestProducto"));
            buttons.add(b);
        }
        out.add("buttons", buttons);
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
        return out;
    }

    public String getIngredientesingredientes() {
        String res = "";
        for (int i = 0; i < this.cIngredientes.size(); i++) {
            if (i != 0) {
                res += ",";
            }
            res += this.cIngredientes.get(i).getingredientes();
        }
        return res;
    }

    public String getIngredientesprecio() {
        String res = "";
        for (int i = 0; i < this.cIngredientes.size(); i++) {
            if (i != 0) {
                res += ",";
            }
            res += this.cIngredientes.get(i).getprecio();
        }
        return res;
    }
}
