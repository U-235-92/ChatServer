package aq.koptev.server.handlers;

import aq.koptev.server.models.Server;
import aq.koptev.server.models.User;
import aq.koptev.server.sevicies.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerHandler {

    public static final String SPACE_SYMBOL = " ";
    public static final String AUTHORIZE_COMMAND = "#l";
    public static final String REGISTRATION_COMMAND = "#r";
    public static final String COMMON_MESSAGE_COMMAND = "#a";
    public static final String PRIVATE_MESSAGE_COMMAND = "#p";
    public static final String ERROR_AUTHENTICATION_COMMAND = "#errauth";
    public static final String OK_AUTHENTICATION_COMMAND = "#okauth";
    public static final String CONNECT_COMMAND = "#c";
    public static final String DISCONNECT_COMMAND = "#dc";
    public static final String SERVER_COMMAND = "#scom";






//    public static final String CONNECT_COMMAND = "//connected";
    public static final String SUCCESS_CONNECT_COMMAND = "//success";
    public static final String LOGIN_COMMAND = "//login";
    public static final String PERSONAL_MESSAGE_COMMAND = "//personal";
    public static final String LOG_ERROR_MESSAGE = "<---Log error--->";
    public static final String SUCCESS_CONNECTION_MESSAGE = "<---Success, you're joined!--->";
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
                waitAuthentication();
                waitMessage();
            } catch (IOException e) {
                server.sendServerMessage(this, "Пользователь " + user.getLogin() + " покинул чат");
                server.removeHandler(this);
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

    private void waitAuthentication() throws IOException {
        boolean isProcessSuccess = false;
        while(!isProcessSuccess) {
            String message = inputStream.readUTF();
            if(!message.startsWith("#")) {
                System.out.println("Неверная команда");
                continue;
            }
            String command = message.split("\\s+", 2)[0];
            switch (command) {
                case AUTHORIZE_COMMAND:
                    String dataAuthentication = message.split("\\s+")[1];
                    isProcessSuccess = processAuthentication(dataAuthentication);
                    break;
                case REGISTRATION_COMMAND:
                    //процесс регистрации
                    String dataRegistration = message.split("\\s+")[1];
                    isProcessSuccess = processRegistration(dataRegistration);
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
                server.sendServerMessage(this, "Пользователь " + user.getLogin() + " вошел в чат");
                server.addHandler(this);
                sendMessage(OK_AUTHENTICATION_COMMAND, login);
                return true;
            }
        }
    }

    private boolean processRegistration(String dataRegistration) {
        return false;
    }

    private void waitMessage() throws IOException {
        while(true) {
            String message = inputStream.readUTF();
            if(!message.startsWith("#")) {
                System.out.println("Неверная команда");
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
                    //общее сообщение
                    break;
                case PRIVATE_MESSAGE_COMMAND:
                    sender = message.split("\\s+", 3)[1];
                    receiver = message.split("\\s+", 3)[2];
                    textMessage = message.split("\\s+", 4)[3];
                    server.sendPrivateMessage(sender, receiver, textMessage);
                    //личное сообщение
                    break;
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    public void handle() {
//        Thread thread = new Thread(() -> {
//            try {
//                authorizeProcess();
//                sendConnectedLogin();
//                sendConnectedUsers();
//                receiveData();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        thread.start();
//    }

    private void authorizeProcess() throws IOException {
        while(true) {
            String[] logParts = getLoginAndPassword();
            if(logParts == null || logParts.length == 0) {
                sendData(LOG_ERROR_MESSAGE);
                continue;
            } else if(logParts.length == 1) {
                String login = logParts[0];
                if(isAuthenticationSuccess(login, "")) {
                    break;
                }
            } else {
                String login = logParts[0];
                String password = logParts[1];
                if(isAuthenticationSuccess(login, password)) {
                    break;
                }
            }
        }
    }

    private String[] getLoginAndPassword() {
        String[] parts;
        String data = "";
        try {
            data = inputStream.readUTF().trim();
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(data.startsWith(AUTHORIZE_COMMAND)) {
            data = data.substring(AUTHORIZE_COMMAND.length());
            parts = data.trim().split(SPACE_SYMBOL);
            if (parts.length > 2) {
                return null;
            } else {
                return parts;
            }
        } else {
            return null;
        }
    }

//    private boolean isAuthenticationSuccess(String login, String password) throws IOException {
//        AuthenticationService authenticationService = server.getAuthenticationService();
//        user = authenticationService.getAuthenticatedUser(login, password);
//        if(user == null) {
//            String message = authenticationService.getErrorAuthenticationMessage(password);
//            sendData(message);
//            return false;
//        } else {
//            if(server.isUserConnected(user)) {
//                String message = "<---User with login " + user.getLogin() + " is authorized--->";
//                sendData(message);
//                return false;
//            } else {
//                server.addHandler(this);
//                sendData(SUCCESS_CONNECTION_MESSAGE);
//                return true;
//            }
//        }
//    }

    private void sendConnectedLogin() throws IOException {
        sendData(LOGIN_COMMAND + user.getLogin());
    }

    private void sendConnectedUsers() throws IOException {
        String users = server.getConnectedUsers();
        String data = CONNECT_COMMAND + users;
        server.sendAuthenticationUsersData(data);
    }

    private void receiveData() throws IOException {
        while(true) {
            String data = inputStream.readUTF();
            server.receiveData(data);
        }
    }

    public void sendData(String data) throws IOException {
        outputStream.writeUTF(data);
    }

    public User getUser() {
        return user;
    }
}
