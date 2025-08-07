package com.test.server;

import com.test.client.ClientConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private static Set<ClientConnectionHandler> clientSet;

    public Server(int numberOfThreads) throws IOException {
        this.serverSocket = new ServerSocket(7777);
        this.threadPool = Executors.newFixedThreadPool(numberOfThreads);
        clientSet = Collections.synchronizedSet(new HashSet<>());
        logger.info("Server started on port {}", serverSocket.getLocalPort());
        logger.info("Thread pool initialized with {} threads", numberOfThreads);
    }

    public void start() {
        logger.info("Server listening for incoming connections...");
        try {
            while(true) {
                Socket socket = serverSocket.accept();
                logger.info("New connection from {}", socket.getRemoteSocketAddress());
                ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(socket);
                clientSet.add(clientConnectionHandler);
                threadPool.execute(clientConnectionHandler);
            }
        } catch (IOException e) {
            logger.error("Connection accept failed, and connection dropped", e);
        }
    }


    public static void broadcast(String message, ClientConnectionHandler sender){
        clientSet.forEach(clientConnectionHandler ->
        {
            if(clientConnectionHandler != sender){
                clientConnectionHandler.sendMessage(message);
            }
        });
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public static Set<ClientConnectionHandler> getClientSet() {
        return clientSet;
    }

    public static void setClientSet(Set<ClientConnectionHandler> clientSet) {
        Server.clientSet = clientSet;
    }
}
