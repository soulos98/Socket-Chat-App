package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Optional<Socket> optionSocket = Optional.empty();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            optionSocket = Optional.of(new Socket("localhost", 7777));
            Socket socket = optionSocket.get();
            InputStream inputStream = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            executor.execute(() -> {
                log.info("Reader thread started");
                byte[] buffer = new byte[1024];
                while(true){
                    try {
                        int readData = inputStream.read(buffer);
                        if ((readData) == -1)
                            break;
                        else {
                            log.info("read in {} bytes", readData);
                        }
                        System.out.println("Received message: "
                                + new String(Arrays.copyOfRange(buffer, 0, readData), StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            while (true) {
                System.out.print("Message to send : ");
                String message = scanner.nextLine();
                if (message.equals("exit")) {
                    break;
                }
                outputStream.write(message.getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            optionSocket.map(socket -> {
                try {
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return socket;
            });
            executor.shutdown();
        }



    }
}