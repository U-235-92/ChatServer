package aq.koptev.server.models;

import aq.koptev.server.handlers.Handler;
import aq.koptev.server.sevicies.authentication.AuthenticationService;

import java.io.DataInputStream;
import java.io.IOException;

public class Authenticator {

    private Handler handler;
    private AuthenticationService authenticationService;
    private boolean isProcessSuccess = false;

    public Authenticator(Handler handler, AuthenticationService authenticationService) {
        this.handler = handler;
        this.authenticationService = authenticationService;
    }

    public synchronized void closeAuthenticationProcess() {
        isProcessSuccess = true;
    }

    public void authentication(DataInputStream inputStream) {
        Thread authThread = new Thread(() -> {
            while(!isProcessSuccess) {
                String message = null;
                try {
                    message = inputStream.readUTF();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(!message.startsWith("#")) {
                    System.out.println("Bad command");
                    continue;
                }
                String command = message.split("\\s+", 2)[0];
                if(command.equals(Command.AUTHENTICATION_COMMAND.getCommand())) {
                    String dataAuthentication = message.split("\\s+", 2)[1];
                    try {
                        isProcessSuccess = processAuthentication(dataAuthentication);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        authThread.start();
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
        User user = authenticationService.getUser(login, password);
        String errorAuthenticationMessage = "";
        if(user == null) {
            errorAuthenticationMessage = authenticationService.getErrorAuthenticationMessage(login, password);
            handler.sendMessage(Command.ERROR_AUTHENTICATION_COMMAND.getCommand(), errorAuthenticationMessage);
            return false;
        } else {
            if(handler.isConnected(user)) {
                errorAuthenticationMessage = String.format("Пользователь с логином %s уже авторизован", user.getLogin());
                handler.sendMessage(Command.ERROR_AUTHENTICATION_COMMAND.getCommand(), errorAuthenticationMessage);
                return false;
            } else {
                handler.setUser(user);
                handler.registrationHandler();
                handler.sendMessage(Command.OK_AUTHENTICATION_COMMAND.getCommand(), login);
                handler.sendMessage(Command.USER_CONNECT_COMMAND.getCommand(),
                        String.format("Пользователь %s вошел в чат", handler.getUser().getLogin()));
                return true;
            }
        }
    }
}
