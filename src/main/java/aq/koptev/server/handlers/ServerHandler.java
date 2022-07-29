package aq.koptev.server.handlers;

import aq.koptev.server.models.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerHandler implements Handler {
    private Socket clientSocket;
    private Server server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private User user;
    private Identifier identifier;

    public ServerHandler(Server server, Socket clientSocket, User user) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.user = user;
        inputStream = new DataInputStream(clientSocket.getInputStream());
        outputStream = new DataOutputStream(clientSocket.getOutputStream());
        identifier = new Identifier(this, server.getAuthenticationService(), server.getRegistrationService());
    }

    @Override
    public void handle() {
        Thread thread = new Thread(() -> {
            try {
                waitMessage();
            } catch (IOException e) {
                try {
                    processDisconnectHandler();
                } catch (IOException ex) {
                    e.printStackTrace();
                }
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private synchronized void processDisconnectHandler() throws IOException {
        sendDisconnectedUserLogin();
        disconnectHandler();
    }

    private void sendDisconnectedUserLogin() throws IOException {
        System.out.println("diconnect");
        if(user != null) {
            server.processMessage(Command.USER_DISCONNECT_COMMAND, String.format("Пользователь %s покинул чат", user.getLogin()));
        }
    }

    private void disconnectHandler() {
        try {
            server.removeHandler(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public synchronized void waitMessage() throws IOException {
        while(true) {
            String incomingString = inputStream.readUTF();
            if(!incomingString.startsWith("#")) {
                System.out.println("Bad command");
            }
            Command command = Command.getCommandByValue(incomingString.split("\\s+", 2)[0]);
            String message = incomingString.split("\\s+", 2)[1];
            server.processMessage(command, message);
        }
    }

    @Override
    public synchronized void sendMessage(Command command, String message) throws IOException {
        String send = String.format("%s %s", command.getCommand(), message).trim();
        outputStream.writeUTF(send);
    }

    @Override
    public synchronized void sendMessage(Command command, String sender, String message) throws IOException {
        String send = String.format("%s %s %s", command.getCommand(), sender, message).trim();
        outputStream.writeUTF(send);
    }

    @Override
    public synchronized void sendMessage(Command command, String sender, String receiver, String message) throws IOException {
        String send = String.format("%s %s %s %s", sender, receiver, message).trim();
        outputStream.writeUTF(send);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void registrationHandler() {
        server.addHandler(this);
    }

    @Override
    public void closeConnection() throws IOException {
        clientSocket.close();
    }

    @Override
    public boolean isConnected(String login) {
        return server.isUserConnected(login);
    }
}
