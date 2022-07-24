package aq.koptev.server.sevicies.authentication;

public enum AuthenticationMessages {

    WRONG_LOGIN("Неверный логин"), WRONG_PASSWORD("Неверный пароль"), EMPTY_MESSAGE("");
    private String message;

    AuthenticationMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
