package aq.koptev.server.sevicies.authentication;

public enum AuthenticationMessages {

    WRONG_LOGIN("�������� �����"), WRONG_PASSWORD("�������� ������"), EMPTY_MESSAGE("");
    private String message;

    AuthenticationMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
