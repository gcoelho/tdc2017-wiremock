package com.hpe.wiremock;

public class UUIDNotFoundException extends Exception {

    public UUIDNotFoundException(String uuid) {
        super("Could not find the corresponding UUID for " + uuid);
    }
}
