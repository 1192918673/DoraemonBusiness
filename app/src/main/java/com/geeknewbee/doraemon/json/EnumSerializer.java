package com.geeknewbee.doraemon.json;

import com.geeknewbee.doraemon.center.command.CommandType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * CommandType 和 Gson 进行转换的类
 */
public class EnumSerializer implements JsonSerializer<CommandType>,
        JsonDeserializer<CommandType> {
    @Override
    public CommandType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.getAsInt() < CommandType.values().length)
            return CommandType.values()[json.getAsInt()];
        return null;
    }

    @Override
    public JsonElement serialize(CommandType src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.ordinal());
    }
}
