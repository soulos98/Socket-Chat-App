package com.test;


import com.test.server.Server;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    public static void main(String[] args) {
        try {
            Server server = new Server(5);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}