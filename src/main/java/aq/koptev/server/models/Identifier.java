package aq.koptev.server.models;

import aq.koptev.server.handlers.Handler;
import aq.koptev.server.sevicies.authentication.AuthenticationService;
import aq.koptev.server.sevicies.registration.RegistrationService;

import java.io.DataInputStream;
import java.io.IOException;

public class Identifier {

    private Handler handler;
    private AuthenticationService authenticationService;
    private RegistrationService registrationService;
    private boolean isProcessSuccess = false;

    public Identifier(Handler handler, AuthenticationService authenticationService, RegistrationService registrationService) {
        this.handler = handler;
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
    }

    public void identificationProcess(DataInputStream inputStream) {
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
                    e.printStackTrace();
                }
            } else if(command.equals(Command.REGISTRATION_COMMAND.getCommand())) {
                String dataRegistration = message.split("\\s+", 2)[1];
                try {
                    processRegistration(dataRegistration);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        User user = authenticationService.getUser(login, password);
        String errorAuthenticationMessage = "";
        if(user == null) {
            errorAuthenticationMessage = authenticationService.getErrorAuthenticationMessage(login, password);
            handler.sendMessage(Command.ERROR_AUTHENTICATION_COMMAND, errorAuthenticationMessage);
            return false;
        } else {
            if(handler.isConnected(user.getLogin())) {
                errorAuthenticationMessage = String.format("Пользователь с логином %s уже авторизован", user.getLogin());
                handler.sendMessage(Command.ERROR_AUTHENTICATION_COMMAND, errorAuthenticationMessage);
                return false;
            } else {
                handler.setUser(user);
                handler.registrationHandler();
                String message = String.format("%s %s", login, password);
                handler.sendMessage(Command.OK_AUTHENTICATION_COMMAND, message);
                ///
//                handler.sendMessage(Command.GET_CONNECTED_USER_COMMAND, String.format("%s %s", login, password));
//                handler.sendMessage(Command.USER_CONNECT_COMMAND, String.format("Пользователь %s вошел в чат", login));
//                handler.sendMessage(Command.GET_CONNECTED_USERS_COMMAND, null);
                return true;
            }
        }
    }

    private void processRegistration(String dataRegistration) throws IOException {
        String login = dataRegistration.split("\\s+", 2)[0];
        String password = dataRegistration.split("\\s+", 2)[1];
        if(login.matches("\\s+")) {
            handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND, "Логин не может содержать символ пробела");
        } else if(login.length() > 30 || password.length() > 30) {
            handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND, "Поле логин и пароль не могут быть длинее 30 символов");
        } else {
            User user = new User(login, password);
            if(registrationService.registerUser(user)) {
                handler.sendMessage(Command.OK_REGISTRATION_COMMAND, "");
            } else {
                handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND, String.format("Логин %s уже занят", login));
            }
        }
    }
}
