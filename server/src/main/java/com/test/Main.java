package com.test;

import com.test.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server(5, 7777);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}