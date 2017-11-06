package com.hpe.wiremock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ResourceIdMapper {

    private static final Map<String, String> RESOURCE_MAPPINGS = new ConcurrentHashMap<>();

    public static Pattern UUID_PATTERN = Pattern.compile("(\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12})");

    public static String fromOriginal(String originalUUID) {
        String newUUID = RESOURCE_MAPPINGS.getOrDefault(originalUUID, UUID.randomUUID().toString());

        RESOURCE_MAPPINGS.put(originalUUID, newUUID);

        return newUUID;
    }

    public static String toOriginal(String modifiedUUID) throws UUIDNotFoundException {
        return RESOURCE_MAPPINGS.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(modifiedUUID))
                .findFirst()
                .map(entry -> entry.getKey())
                .orElseThrow(() -> new UUIDNotFoundException(modifiedUUID));
    }

}
