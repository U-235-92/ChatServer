package aq.koptev.server.models;

import aq.koptev.server.sevicies.authentication.AuthenticationService;
import aq.koptev.server.sevicies.authentication.DBAuthenticationService;

import java.io.IOException;

public class Authenticator {
    private AuthenticationService authenticationService;
    private Server server;

    public Authenticator(Server server) {
        authenticationService = new DBAuthenticationService();
        this.server = server;
    }

    public User processAuthentication(String message) throws IOException {
        String[] parts = message.split("\\s+");
        User user;
        if(parts == null || parts.length > 2 || parts.length == 0) {
            return null;
        } else if(parts.length == 1) {
            String login = parts[0];
            user = getUser(login, "");
            if(user == null) {
                return null;
            } else {
                return user;
            }
        } else {
            String login = parts[0];
            String password = parts[1];
            user = getUser(login, password);
            if(user == null) {
                return null;
            } else {
                return user;
            }
        }
    }

    private User getUser(String login, String password) throws IOException {
        User user = authenticationService.getUser(login, password);
        if(user != null) {
            if(!server.isUserConnected(user.getLogin())) {
                return user;
            }
        }
        return null;
    }

    public String getErrorMessage(String message) {
        String[] parts = message.split("\\s+");
        if(parts == null || parts.length > 2 || parts.length == 0) {
            return "Логин не может содержать пробел или быть пустым";
        } else if(parts.length == 1) {
            String login = parts[0];
            if(server.isUserConnected(login)) {
                return String.format("Пользователь с логином %s уже авторизован", login);
            } else {
                return authenticationService.getErrorAuthenticationMessage(login, "");
            }
        } else {
            String login = parts[0];
            String password = parts[1];
            if(server.isUserConnected(login)) {
                return String.format("Пользователь с логином %s уже авторизован", login);
            } else {
                return authenticationService.getErrorAuthenticationMessage(login, password);
            }
        }
    }
}
