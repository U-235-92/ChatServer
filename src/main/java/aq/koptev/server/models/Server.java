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

    public void addHandler(ServerHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(ServerHandler handler) {
        handlers.remove(handler);
    }

    public void sendData(String data) {
        //отправить заинтересованным пользователям
        if(data.startsWith(ServerHandler.PERSONAL_MESSAGE_COMMAND)) {
            String tmp = data.substring(ServerHandler.PERSONAL_MESSAGE_COMMAND.length()).trim();
            String[] messageParts = tmp.trim().split("\s");
            String sender = messageParts[0];
            String date = messageParts[1];
            String receiversString = messageParts[3];
            String message = messageParts[4];
            String[] receivers = receiversString.split("#");
            for(ServerHandler handler : handlers) {
                for(String receiver : receivers) {
                    if(authenticationService.isExistUser(receiver)) {
                        if(isUserConnected(new User(receiver, ""))) {
                            if(handler.getUser().getLogin().equals(receiver) ||
                                    handler.getUser().getLogin().equals(sender)) {
                                try {
                                    String toSend = sender + " " + date + " " + message;
                                    handler.sendData(toSend);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            if(handler.getUser().getLogin().equals(sender)) {
                                try {
                                    handler.sendData("User with login " + receiver + " is disconnected");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else {
                        if(handler.getUser().getLogin().equals(sender)) {
                            try {
                                handler.sendData("User with login " + receiver + " isn't exist");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        } else {
            for(ServerHandler handler : handlers) {
                try {
                    handler.sendData(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean hasConnectedUsers() {
        return handlers.size() > 0;
    }

    public void receiveData(String data) {
        //обработать сообщение
        sendData(data);
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public boolean isUserConnected(User user) {
        for(ServerHandler handler : handlers) {
            if(user.getLogin().equals(handler.getUser().getLogin())) {
                return true;
            }
        }
        return false;
    }

    public String getConnectedUsers() {
        String users = "";
        for(int i = 0; i < handlers.size(); i++) {
            String login = handlers.get(i).getUser().getLogin();
            if(i == handlers.size() - 1) {
                users += login;
            } else {
                users += login + "#";
            }
        }
        return users;
    }

    public void sendAuthenticationUsersData(String data) {
        for(ServerHandler handler : handlers) {
            try {
                handler.sendData(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
