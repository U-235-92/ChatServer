package aq.koptev.server.sevicies.authentication;

import aq.koptev.server.models.User;

public interface AuthenticationService {

    User getUser(String login, String password);
    boolean isExistUser(String login);
    String getErrorAuthenticationMessage(String login, String password);
}
