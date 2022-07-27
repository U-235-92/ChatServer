package aq.koptev.server.models;

import aq.koptev.server.sevicies.registration.DBRegistrationService;
import aq.koptev.server.sevicies.registration.RegistrationService;

import java.io.IOException;

public class Registrar {

    private RegistrationService registrationService;
    private Server server;

    public Registrar(Server server) {
        this.server = server;
        registrationService = new DBRegistrationService();
    }

    public String processRegistration(String message) throws IOException {
        String login = message.split("\\s+", 2)[0];
        String password = message.split("\\s+", 2)[1];
        if(login.matches("\\s+")) {
            return "Логин не может содержать символ пробела";
        } else if(login.length() > 30 || password.length() > 30) {
            return "Поле логин и пароль не могут быть длинее 30 символов";
        } else {
            User user = new User(login, password);
            if(registrationService.registerUser(user)) {
                return null;
            } else {
                return String.format("Логин %s уже занят", login);
            }
        }
    }
}
