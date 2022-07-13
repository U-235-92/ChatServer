package aq.koptev.server.sevicies;

import aq.koptev.server.models.User;

public interface AuthenticationService {
    String WRONG_LOGIN = "Login is incorrect";
    String WRONG_PASSWORD = "Password is incorrect";
    String EMPTY_MESSAGE = "";
    User getAuthenticatedUser(String login, String password);
    boolean isExistUser(String login);
    String getErrorAuthenticationMessage(String password);
}
