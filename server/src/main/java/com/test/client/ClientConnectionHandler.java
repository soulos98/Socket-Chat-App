package com.test.client;

import com.test.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnectionHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientConnectionHandler.class);
    Socket socket;
    BufferedInputStream in;
    PrintWriter out;
    String userName;

    String terminationTrigger = "exit";
    public ClientConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        Thread.currentThread().setName("ClientConnectionHandler");
        try {
            byte[] reader = new byte[1024];
            while (in.read(reader) != -1) {
                String message = new String(reader, StandardCharsets.UTF_8).trim();
                if(message.equals(terminationTrigger)) {
                    log.info("Cutting communication with client.");
                    break;
                }
                Server.broadcast(message, this);
                reader = new byte[1024]; // Clear out previous buffer;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Commencing teardown of socket {}:{}", socket.getInetAddress(), socket.getPort());
            Server.getClientSet().remove(this);
            try {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                log.error("Failed to close socket {}:{}", socket.getInetAddress(), socket.getPort());
            }
        }
    }

    public void sendMessage(String message) {
        log.info("Sending message '{}' on behalf of user name {}", message, userName);
        out.write(message);
        out.flush();
    }


}
