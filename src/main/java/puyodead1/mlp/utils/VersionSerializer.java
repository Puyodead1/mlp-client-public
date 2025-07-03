package puyodead1.mlp.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import puyodead1.mlp.MLPService.Version;

import java.lang.reflect.Type;

public class VersionSerializer implements JsonSerializer<Version> {
    @Override
    public JsonElement serialize(Version src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src.isNull()) {
            return JsonNull.INSTANCE;
        }
        JsonObject obj = new JsonObject();
        if(src.name != null) obj.addProperty("name", src.name);
        if(src.protocol != null) obj.addProperty("protocol", src.protocol);
        return obj;
    }
}
