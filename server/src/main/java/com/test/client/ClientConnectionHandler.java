package com.test.client;

import com.test.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientConnectionHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientConnectionHandler.class);
    Socket socket;
    BufferedInputStream in;
    PrintWriter out;
    String userName;

    String terminationCharacter = "!";
    public ClientConnectionHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("ClientConnectionHandler");
        try {
            while (true){
                byte[] reader = new byte[1024];
                int read = in.read(reader);
                log.info("Client received: " + new String(reader, StandardCharsets.UTF_8));
                if(read == -1){
                    log.info("Client disconnected");
                    break;
                }

                if(Arrays.equals(terminationCharacter.getBytes(StandardCharsets.UTF_8), Arrays.copyOfRange(reader, 0, terminationCharacter.length()))) {
                    log.info("Received termination character exiting communication");
                    break;
                }

                Server.broadcast(new String(reader, StandardCharsets.UTF_8), this);
            }

            boolean removedClientFromServer =  Server.getClientSet().remove(this);

            if(removedClientFromServer){
                log.info("Successfully removed client from system.");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                log.info("Closing socket");
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                log.error("Something failed when closing sockets , {}", e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        log.info("Sending message '{}' on behalf of user name {}", message, userName);
        out.write(message);
        out.flush();
    }


}
