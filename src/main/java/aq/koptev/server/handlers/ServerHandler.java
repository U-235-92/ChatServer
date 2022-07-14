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
    public static final String LOG_IN_COMMAND = "//log";
    public static final String CONNECT_COMMAND = "//connected";
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

    public void handle() {
        Thread thread = new Thread(() -> {
            try {
                authorizeProcess();
                sendConnectedLogin();
                sendConnectedUsers();
                receiveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void authorizeProcess() throws IOException {
        while(true) {
            String[] logParts = getLoginAndPassword();
            if(logParts == null || logParts.length == 0) {
                sendData(LOG_ERROR_MESSAGE);
                continue;
            } else if(logParts.length == 1) {
                String login = logParts[0];
                if(isAuthorizeSuccess(login, "")) {
                    break;
                }
            } else {
                String login = logParts[0];
                String password = logParts[1];
                if(isAuthorizeSuccess(login, password)) {
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
        if(data.startsWith(LOG_IN_COMMAND)) {
            data = data.substring(LOG_IN_COMMAND.length());
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

    private boolean isAuthorizeSuccess(String login, String password) throws IOException {
        AuthenticationService authenticationService = server.getAuthenticationService();
        user = authenticationService.getAuthenticatedUser(login, password);
        if(user == null) {
            String message = authenticationService.getErrorAuthenticationMessage(password);
            sendData(message);
            return false;
        } else {
            if(server.isUserConnected(user)) {
                String message = "<---User with login " + user.getLogin() + " is authorized--->";
                sendData(message);
                return false;
            } else {
                server.addHandler(this);
                sendData(SUCCESS_CONNECTION_MESSAGE);
                return true;
            }
        }
    }

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
