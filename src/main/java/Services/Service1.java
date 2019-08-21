package Services;

import chatbot.Sensor;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import static weatherman.web.utils.JSONUtil.json;

public class Service1 {

    public Service1() {
    }

    public JsonObject getAllSensors()
            throws ClientProtocolException, IOException {
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getIdSensor()
            throws ClientProtocolException, IOException {
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getInfoSensor(String sensor)
            throws ClientProtocolException, IOException {
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor" + "/" + sensor.trim();

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getEstadoActuador()
            throws ClientProtocolException, IOException {
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getModificarActuador()
            throws ClientProtocolException, IOException {
        String url = "http://demo5917714.mockable.io/actuador";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPPut object and execute the url
        HttpPut httpPut = new HttpPut(url);
        HttpResponse response = httpclient.execute(httpPut);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getModificarSensor(Sensor sensor)
            throws ClientProtocolException, IOException {
        System.out.println(sensor);
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPPut object and execute the url
        HttpPut httpPut = new HttpPut(url);

        JsonObject json2 = new JsonObject();
        json2.add("id", new JsonPrimitive(sensor.getId()));
        json2.add("actuador", new JsonPrimitive(sensor.getActuador()));
        json2.add("temperatura", new JsonPrimitive(sensor.getTemperatura()));
        json2.add("humedad", new JsonPrimitive(sensor.getHumedad()));
        json2.add("presion", new JsonPrimitive(sensor.getPresion()));
        json2.add("fecha", new JsonPrimitive(sensor.getFecha()));
        json2.add("ejex", new JsonPrimitive(sensor.getEjex()));
        json2.add("ejey", new JsonPrimitive(sensor.getEjey()));
        json2.add("ejez", new JsonPrimitive(sensor.getEjez()));
        
        StringEntity params = new StringEntity(json2.toString());
        httpPut.setEntity(params);
        httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        System.out.println("put:" + httpPut);
        //step 4: Process the result
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("status:"+statusCode);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("error");
        }

        return json2;
    }

    public JsonObject getIdSensor2()
            throws ClientProtocolException, IOException {
        String url = "http://serviciossupplychain.herokuapp.com/myApp/rest/sensor";

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }
}
