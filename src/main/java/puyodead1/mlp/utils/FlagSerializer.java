package puyodead1.mlp.utils;

import com.google.gson.*;
import puyodead1.mlp.client.ui.SearchParametersScreen;

import java.lang.reflect.Type;

public class FlagSerializer implements JsonSerializer<SearchParametersScreen.Flag> {
    @Override
    public JsonElement serialize(SearchParametersScreen.Flag src, Type typeOfSrc, JsonSerializationContext context) {
        return src.bool == null ? JsonNull.INSTANCE : new JsonPrimitive(src.bool);
    }
}
