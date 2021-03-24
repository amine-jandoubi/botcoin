package com.botcoin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public final class JsonUtils {
    public static final ObjectMapper ObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static ObjectMapper getParser() {
        return ObjectMapper;
    }

    public static String pretify(String json) {

        Object jsonObject = null;
        try {
            jsonObject = ObjectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return toJson(jsonObject);

    }

    public static <T> T toObject(String response, Class<T> type) {
        try {
            return ObjectMapper.readValue(response, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object object) {
        try {
            return ObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
