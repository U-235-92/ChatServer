package aq.koptev.server.handlers;

import aq.koptev.server.models.Server;
import aq.koptev.server.models.User;
import aq.koptev.server.sevicies.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerHandler {
    public static final String AUTHENTICATION_COMMAND = "#l";
    public static final String REGISTRATION_COMMAND = "#r";
    public static final String COMMON_MESSAGE_COMMAND = "#a";
    public static final String PRIVATE_MESSAGE_COMMAND = "#p";
    public static final String ERROR_AUTHENTICATION_COMMAND = "#errauth";
    public static final String OK_AUTHENTICATION_COMMAND = "#okauth";
    public static final String ERROR_REGISTRATION_COMMAND = "#errreg";
    public static final String OK_REGISTRATION_COMMAND = "#okreg";
    public static final String USER_CONNECT_COMMAND = "#c";
    public static final String USER_DISCONNECT_COMMAND = "#dc";
    public static final String PRIVATE_SERVER_MESSAGE = "#psm";
    public static final String CONNECTED_USERS_REQUEST = "#reqcu";
    private Socket clientSocket;
    private Server server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private User user;

    public ServerHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        inputStream = new DataInputStream(clientSocket.getInputStream());
        outputStream = new DataOutputStream(clientSocket.getOutputStream());
    }

    public void closeConnection() throws IOException {
        clientSocket.close();
    }

    public void handle() {
        Thread thread = new Thread(() -> {
            try {
                authenticationProcess();
                sendLoginUser();
                sendConnectedUsers();
                waitMessage();
            } catch (IOException e) {
                server.sendServerMessageOnDisconnectedUser(this);
                try {
                    server.removeHandler(this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void sendMessage(String command, String message) throws IOException {
        outputStream.writeUTF(String.format("%s %s", command, message));
    }

    public void sendMessage(String command, String sender, String message) throws IOException {
        outputStream.writeUTF(String.format("%s %s %s", command, sender, message));
    }

    private void authenticationProcess() throws IOException {
        boolean isProcessSuccess = false;
        while(!isProcessSuccess) {
            String message = inputStream.readUTF();
            if(!message.startsWith("#")) {
                System.out.println("Bad command");
                continue;
            }
            String command = message.split("\\s+", 2)[0];
            switch (command) {
                case AUTHENTICATION_COMMAND:
                    String dataAuthentication = message.split("\\s+", 2)[1];
                    isProcessSuccess = processAuthentication(dataAuthentication);
                    break;
                case REGISTRATION_COMMAND:
                    String dataRegistration = message.split("\\s+", 2)[1];
                    processRegistration(dataRegistration);
                    break;
            }
        }

    }

    private boolean processAuthentication(String dataAuthentication) throws IOException {
        String[] parts = dataAuthentication.split("\\s+");
        if(parts == null || parts.length > 2 || parts.length == 0) {
            return false;
        } else if(parts.length == 1) {
            String login = parts[0];
            if(isAuthenticationSuccess(login, "")) {
                return true;
            }
        } else {
            String login = parts[0];
            String password = parts[1];
            if(isAuthenticationSuccess(login, password)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthenticationSuccess(String login, String password) throws IOException {
        AuthenticationService authenticationService = server.getAuthenticationService();
        user = authenticationService.getAuthenticatedUser(login, password);
        String errorAuthenticationMessage = "";
        if(user == null) {
            errorAuthenticationMessage = authenticationService.getErrorAuthenticationMessage(password);
            sendMessage(ERROR_AUTHENTICATION_COMMAND, errorAuthenticationMessage);
            return false;
        } else {
            if(server.isUserConnected(user)) {
                errorAuthenticationMessage = "Пользователь с логином " + user.getLogin() + " уже авторизован";
                sendMessage(ERROR_AUTHENTICATION_COMMAND, errorAuthenticationMessage);
                return false;
            } else {
                server.addHandler(this);
                sendMessage(OK_AUTHENTICATION_COMMAND, login);
                server.sendServerMessageOnConnectedUser(this);
                return true;
            }
        }
    }

    private void sendLoginUser() throws IOException {
        sendMessage(OK_AUTHENTICATION_COMMAND, user.getLogin());
    }

    private void sendConnectedUsers() {
        server.getConnectedUsers();
    }

    private void processRegistration(String dataRegistration) throws IOException {
        String login = dataRegistration.split("\\s+", 2)[0];
        String password = dataRegistration.split("\\s+", 2)[1];
        if(login.matches("\\s+")) {
            sendMessage(ERROR_REGISTRATION_COMMAND, "Логин не может содержать символ пробела");
        } else if(login.length() > 30 || password.length() > 30) {
            sendMessage(ERROR_REGISTRATION_COMMAND, "Поле логин и пароль не могут быть длинее 30 символов");
        } else {
            User user = new User(login, password);
            server.getAuthenticationService().addUser(user);
            sendMessage(OK_REGISTRATION_COMMAND, "");
        }
    }

    private void waitMessage() throws IOException {
        while(true) {
            String message = inputStream.readUTF();
            if(!message.startsWith("#")) {
                System.out.println("Bad command");
            }
            String command = message.split("\\s+", 2)[0];
            String sender = "";
            String textMessage = "";
            String receiver = "";
            switch (command) {
                case COMMON_MESSAGE_COMMAND:
                    sender = message.split("\\s+", 3)[1];
                    textMessage = message.split("\\s+", 3)[2];
                    server.sendCommonMessage(sender, textMessage);
                    break;
                case PRIVATE_MESSAGE_COMMAND:
                    sender = message.split("\\s+", 3)[1];
                    receiver = message.split("\\s+", 3)[2].split("\\s+", 2)[0];
                    textMessage = message.split("\\s+", 4)[3];
                    server.sendPrivateMessage(sender, receiver, textMessage);
                    break;
            }
        }
    }

    public User getUser() {
        return user;
    }
}
