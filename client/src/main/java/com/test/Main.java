package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final AtomicBoolean isConnected = new AtomicBoolean(false);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 7777)){
            isConnected.set(true);
            InputStream inputStream = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            Thread inputThread = new Thread(inputThreadProcessor(inputStream));
            inputThread.start();
            Scanner scanner = new Scanner(System.in);
            while (isConnected.get()) {
                System.out.print("Message to send : ");
                String message = scanner.nextLine();
                if (message.equals("done")) {
                    break;
                }
                outputStream.write(message.getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            isConnected.set(false);
        }

    }

    public static Runnable inputThreadProcessor(InputStream inputStream) {
        return () -> {
            log.info("Reader thread started");
            byte[] buffer = new byte[1024];
            try {
                while (inputStream.read(buffer) != -1) {
                    String message = new String(buffer, StandardCharsets.UTF_8).trim();
                    System.out.println("Received message: " + message);
                    buffer = new byte[1024];
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                isConnected.set(false);
            }
        };
    }
}