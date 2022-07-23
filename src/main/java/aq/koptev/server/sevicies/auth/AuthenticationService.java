package aq.koptev.server.sevicies.auth;

import aq.koptev.server.models.User;

public interface AuthenticationService {
    String WRONG_LOGIN = "Неверный логин";
    String WRONG_PASSWORD = "Неверный пароль";
    String EMPTY_MESSAGE = "";
    User getUser(String login, String password);
    boolean isExistUser(String login);
    String getErrorAuthenticationMessage(String login, String password);
    void registerUser(User user);
}
