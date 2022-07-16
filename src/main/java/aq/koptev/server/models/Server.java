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
        for(ServerHandler handler : handlers) {

        }
    }

    public synchronized void sendServerMessage(ServerHandler handler, String message) {
        for(ServerHandler serverHandler : handlers) {
            if(serverHandler == handler) {
                continue;
            }
            try {
                serverHandler.sendMessage(ServerHandler.SERVER_COMMAND, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void send(String data) {
        if(isPersonalMessage(data)) {
            String[] dataParts = getDataParts(data);
            String sender = getSenderMessage(dataParts);
            String date = getDateMessage(dataParts);
            String textMessage = getTextMessage(dataParts);
            String[] receivers = getReceiversMessage(dataParts);
            processSendPersonalMessage(sender, date, textMessage, receivers);
        } else {
            processSendCommonMessage(data);
        }
    }

    private String[] getDataParts(String data) {
        String tmp = data.substring(ServerHandler.PERSONAL_MESSAGE_COMMAND.length()).trim();
        String[] dataParts = tmp.trim().split("\s");
        return dataParts;

    }

    private String getSenderMessage(String[] dataParts) {
        return dataParts[0];
    }

    private String getDateMessage(String[] dataParts) {
        return dataParts[1];
    }

    private String getTextMessage(String[] dataParts) {
        String textMessage = "";
        for(int i  = 4; i < dataParts.length; i++) {
            textMessage += dataParts[i] + " ";
        }
        return textMessage.trim();
    }

    private String[] getReceiversMessage(String[] dataParts) {
        String receivers = dataParts[3];
        return receivers.split("#");
    }

    private void processSendPersonalMessage(String sender, String dateMessage, String textMessage, String[] receivers) {
        for(ServerHandler handler : handlers) {
            for(String receiver : receivers) {
                if(authenticationService.isExistUser(receiver)) {
                    if(isUserConnected(new User(receiver, ""))) {
                        sendPersonalMessageIfReceiverConnected(handler, sender, receiver, dateMessage, textMessage);
                    } else {
                        sendPersonalMessageIfReceiverDisconnected(handler, sender, receiver);
                    }
                } else {
                    sendPersonalMessageIfReceiverIsNotExist(handler, sender, receiver);
                }
            }
        }
    }

    private void sendPersonalMessageIfReceiverConnected(ServerHandler handler, String sender, String receiver,
                                                        String dateMessage, String textMessage) {
        if(handler.getUser().getLogin().equals(receiver) ||
                handler.getUser().getLogin().equals(sender)) {
            try {
                String toSend = sender + " " + dateMessage + " " + textMessage;
                handler.sendData(toSend);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPersonalMessageIfReceiverDisconnected(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                handler.sendData("User with login " + receiver + " is disconnected");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPersonalMessageIfReceiverIsNotExist(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                handler.sendData("User with login " + receiver + " isn't exist");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processSendCommonMessage(String data) {
        for(ServerHandler handler : handlers) {
            try {
                handler.sendData(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isPersonalMessage(String message) {
        return message.startsWith(ServerHandler.PERSONAL_MESSAGE_COMMAND);
    }

    public void receiveData(String data) {
        send(data);
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
