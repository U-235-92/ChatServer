package aq.koptev.server.models;

import aq.koptev.server.handlers.ServerHandler;
import aq.koptev.server.sevicies.AuthenticationService;
import aq.koptev.server.sevicies.SimpleAuthenticationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int DEFAULT_PORT = 9000;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private int port;
    private AuthenticationService authenticationService;
    private List<ServerHandler> handlers;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        this.port = port;
        handlers = new ArrayList<>();
        authenticationService = new SimpleAuthenticationService();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitClient() {
        while(true) {
            System.out.println("Server wait for connection!");
            processWaitClient();
            System.out.println("Connection is success!");
            setUpHandler();
        }
    }

    private void processWaitClient() {
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpHandler() {
        ServerHandler handler = null;
        try {
            handler = new ServerHandler(this, clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.handle();
    }

    public synchronized void addHandler(ServerHandler handler) {
        handlers.add(handler);
    }

    public synchronized void removeHandler(ServerHandler handler) throws IOException {
        handler.closeConnection();
        handlers.remove(handler);
    }

    public synchronized void sendCommonMessage(String sender, String message) {
        for(ServerHandler handler : handlers) {
            try {
                handler.sendMessage(ServerHandler.COMMON_MESSAGE_COMMAND, sender, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void sendPrivateMessage(String sender, String receiver, String message) {
        for(ServerHandler item : handlers) {
            if(authenticationService.isExistUser(receiver)) {
                if(isUserConnected(new User(receiver, ""))) {
                    sendPrivateMessageIfReceiverConnected(item, sender, receiver, message);
                } else {
                    sendPrivateMessageIfReceiverDisconnected(item, sender, receiver);
                }
            } else {
                sendPrivateMessageIfReceiverIsNotExist(item, sender, receiver);
            }
        }
    }

    private void sendPrivateMessageIfReceiverConnected(ServerHandler handler, String sender, String receiver, String message) {
        if(handler.getUser().getLogin().equals(receiver) || handler.getUser().getLogin().equals(sender)) {
            try {
                handler.sendMessage(ServerHandler.PRIVATE_SERVER_MESSAGE, sender, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPrivateMessageIfReceiverDisconnected(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                String message = "Пользователь с логином " + receiver + " сейчас не в сети";
                handler.sendMessage(ServerHandler.PRIVATE_SERVER_MESSAGE, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPrivateMessageIfReceiverIsNotExist(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                String message = "Пользователя с логином " + receiver + " не существует";
                handler.sendMessage(ServerHandler.PRIVATE_SERVER_MESSAGE, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized boolean isUserConnected(User user) {
        for(ServerHandler handler : handlers) {
            if(user.getLogin().equals(handler.getUser().getLogin())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendServerMessageOnConnectedUser(ServerHandler handler) {
        for(ServerHandler serverHandler : handlers) {
            if(serverHandler == handler) {
                continue;
            }
            try {
                String message = String.format("Пользователь %s вошел в чат", handler.getUser().getLogin());
                serverHandler.sendMessage(ServerHandler.USER_CONNECT_COMMAND, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendServerMessageOnDisconnectedUser(ServerHandler handler) {
        for(ServerHandler serverHandler : handlers) {
            if(serverHandler == handler) {
                continue;
            }
            try {
                String message = String.format("Пользователь %s покинул чат", handler.getUser().getLogin());
                serverHandler.sendMessage(ServerHandler.USER_DISCONNECT_COMMAND, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getConnectedUsers() {
        String users = "";
        for(ServerHandler handler : handlers) {
            users += String.format("%s ", handler.getUser().getLogin());
        }
        for(ServerHandler handler : handlers) {
            try {
                handler.sendMessage(ServerHandler.CONNECTED_USERS_REQUEST, users);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }
}
