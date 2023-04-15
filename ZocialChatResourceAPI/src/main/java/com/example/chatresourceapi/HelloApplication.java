package com.example.chatresourceapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class HelloApplication extends Application {
    // loads a message at url http://localhost:8080/ChatResourceAPI-1.0-SNAPSHOT/api
}