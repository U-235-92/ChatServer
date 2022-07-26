package aq.koptev.server.models;

import aq.koptev.server.handlers.ServerHandler;
import aq.koptev.server.sevicies.account.AccountService;
import aq.koptev.server.sevicies.account.SimpleAccountService;
import aq.koptev.server.sevicies.authentication.AuthenticationService;
import aq.koptev.server.sevicies.authentication.DBAuthenticationService;
import aq.koptev.server.sevicies.registration.DBRegistrationService;
import aq.koptev.server.sevicies.registration.RegistrationService;

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
    private RegistrationService registrationService;
    private AccountService accountService;
    private List<ServerHandler> handlers;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        this.port = port;
        handlers = new ArrayList<>();
        authenticationService = new DBAuthenticationService();
        registrationService = new DBRegistrationService();
        accountService = new SimpleAccountService();
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
        processConnectedUsers();
    }

    public synchronized void processMessage(String command, String message) {
        if(command.equals(Command.COMMON_MESSAGE_COMMAND.getCommand())) {
            processCommonMessage(message);
        } else if(command.equals(Command.PRIVATE_MESSAGE_COMMAND.getCommand())) {
            processPrivateMessage(message);
        } else if(command.equals(Command.USER_CONNECT_COMMAND.getCommand())) {
            processUserConnectedMessage(message);
        } else if(command.equals(Command.USER_DISCONNECT_COMMAND.getCommand())) {
            processUserDisconnectedMessage(message);
        } else if(command.equals(Command.GET_CONNECTED_USERS_COMMAND.getCommand())) {
            processConnectedUsers();
        } else if(command.equals(Command.GET_CONNECTED_USER_COMMAND.getCommand())) {
            processConnectedUser(message);
        } else if(command.equals(Command.CHANGE_USER_ACCOUNT_SETTINGS_COMMAND.getCommand())) {
            processChangeUserAccountMessage(message);
        }
    }

    private void processCommonMessage(String message) {
        String sender = message.split("\\s+", 2)[0];
        String textMessage = message.split("\\s+", 2)[1];
        for(ServerHandler handler : handlers) {
            try {
                handler.sendMessage(Command.COMMON_MESSAGE_COMMAND, sender, textMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processPrivateMessage(String message) {
        String sender = message.split("\\s+", 3)[0];
        String receiver = message.split("\\s+", 3)[1];
        String textMessage = message.split("\\s+", 3)[2];
        for(ServerHandler item : handlers) {
            if(authenticationService.isExistUser(receiver)) {
                if(isUserConnected(receiver)) {
                    sendPrivateMessageIfReceiverConnected(item, sender, receiver, textMessage);
                } else {
                    sendPrivateMessageIfReceiverDisconnected(item, sender, receiver);
                }
            } else {
                sendPrivateMessageIfReceiverIsNotExist(item, sender, receiver);
            }
        }
    }

    public synchronized boolean isUserConnected(String login) {
        for(ServerHandler handler : handlers) {
            if(login.equals(handler.getUser().getLogin())) {
                return true;
            }
        }
        return false;
    }

    private void sendPrivateMessageIfReceiverConnected(ServerHandler handler, String sender, String receiver, String message) {
        if(handler.getUser().getLogin().equals(receiver) || handler.getUser().getLogin().equals(sender)) {
            try {
                handler.sendMessage(Command.PRIVATE_MESSAGE_COMMAND, sender, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPrivateMessageIfReceiverDisconnected(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                String message = String.format("Пользователь с логином %s сейчас не в сети", receiver);
                handler.sendMessage(Command.PRIVATE_SERVER_MESSAGE, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPrivateMessageIfReceiverIsNotExist(ServerHandler handler, String sender, String receiver) {
        if(handler.getUser().getLogin().equals(sender)) {
            try {
                String message = String.format("Пользователя с логином %s не существует", receiver);
                handler.sendMessage(Command.PRIVATE_SERVER_MESSAGE, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processUserConnectedMessage(String message) {
        for(ServerHandler serverHandler : handlers) {
            try {
                serverHandler.sendMessage(Command.USER_CONNECT_COMMAND, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        processConnectedUsers();
    }

    private void processChangeUserAccountMessage(String message) {
        String oldLogin = null;
        String newLogin = null;
        String newPassword = null;
        if(accountService.changeAccountSettings(message)) {
            if(message.split("\\s+", 4).length > 3) {
                oldLogin = message.split("\\s+", 4)[0];
                newLogin = message.split("\\s+", 4)[1];
                newPassword = message.split("\\s+", 4)[3];
                for(ServerHandler handler : handlers) {
                    if(handler.getUser().getLogin().equals(oldLogin)) {
                        handler.getUser().setLogin(newLogin);
                        handler.getUser().setPassword(newPassword);
                        try {
                            handler.sendMessage(Command.OK_CHANGE_USER_ACCOUNT_SETTINGS_COMMAND, String.format("%s %s", newLogin, newPassword));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
                processConnectedUsers();
            } else {
                oldLogin = message.split("\\s+", 4)[0];
                newLogin = message.split("\\s+", 4)[1];
                newPassword = "";
                for(ServerHandler handler : handlers) {
                    if(handler.getUser().getLogin().equals(oldLogin)) {
                        handler.getUser().setLogin(newLogin);
                        handler.getUser().setPassword(newPassword);
                        try {
                            handler.sendMessage(Command.OK_CHANGE_USER_ACCOUNT_SETTINGS_COMMAND, String.format("%s %s", newLogin, newPassword));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
                processConnectedUsers();
            }
        } else {
            oldLogin = message.split("\\s+", 4)[0];
            newLogin = message.split("\\s+", 4)[1];
            for(ServerHandler handler : handlers) {
                if(handler.getUser().getLogin().equals(oldLogin)) {
                    try {
                        handler.sendMessage(Command.ERROR_CHANGE_USER_ACCOUNT_SETTINGS_COMMAND, String.format("Указанный логин %s уже занят", newLogin));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }

        /*
            Обратиться к БД, узнать не занят ли новый логин
            Если не занят - поменять логин, если поменялся пароль - поменять пароль
            Обновить в списке хендлеров пользователя
            Обновить список подключенных пользователей у хендлеров
            Отправить новый логин и пароль пользователю, изменившему логин и пароль
            Иначе сообщение об ошибке занятого логина
            * */
    }

    private void processUserDisconnectedMessage(String message) {
        for(ServerHandler serverHandler : handlers) {
            try {
                serverHandler.sendMessage(Command.USER_DISCONNECT_COMMAND, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processConnectedUsers() {
        String users = "";
        for(ServerHandler handler : handlers) {
            users += String.format("%s ", handler.getUser().getLogin());
        }
        for(ServerHandler handler : handlers) {
            try {
                handler.sendMessage(Command.GET_CONNECTED_USERS_COMMAND, users);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processConnectedUser(String message) {
        String login = message.split("\\s+", 2)[0];
        String password = message.split("\\s+", 2)[1];
        for(ServerHandler handler : handlers) {
            if(handler.getUser().getLogin().equals(login)) {
                String send = String.format("%s %s", login, password);
                try {
                    handler.sendMessage(Command.GET_CONNECTED_USER_COMMAND, send);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }
}
