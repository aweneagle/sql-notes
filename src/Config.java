package canal_qbus;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonArray;
import javax.json.JsonObject;

import java.io.FileInputStream;
import java.io.File;
import java.util.List;

public class Config {

    private static JsonObject conf = null;

    public static void init(String file) throws Exception
    {
        JsonReader jr = Json.createReader(new FileInputStream(new File(file)));
        Config.conf = jr.readObject();
    }

    public static JsonObject get(String node)
    {
        return conf.getJsonObject(node);
    }

    public static List<JsonObject> getTopics()
    {
        return conf.getJsonArray("topics").getValuesAs(JsonObject.class);
    }
}


