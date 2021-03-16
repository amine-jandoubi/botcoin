package com.botcoin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public final class JsonUtils {
    public static final ObjectMapper ObjectMapper = new ObjectMapper();

    public static ObjectMapper getParser() {
        return ObjectMapper;
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
