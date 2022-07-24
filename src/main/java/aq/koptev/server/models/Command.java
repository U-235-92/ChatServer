package aq.koptev.server.models;

public enum Command {

    AUTHENTICATION_COMMAND("#l"),
    REGISTRATION_COMMAND("#r"),
    COMMON_MESSAGE_COMMAND("#a"),
    PRIVATE_SERVER_MESSAGE("#psm"),
    PRIVATE_MESSAGE_COMMAND("#p"),
    ERROR_AUTHENTICATION_COMMAND("#errauth"),
    ERROR_REGISTRATION_COMMAND("#errreg"),
    ERROR_CHANGE_LOGIN_MESSAGE("#errchlog"),
    OK_AUTHENTICATION_COMMAND("#okauth"),
    OK_REGISTRATION_COMMAND("#okreg"),
    OK_CHANGE_USER_ACCOUNT_MESSAGE("#okchlog"),
    USER_CONNECT_COMMAND("#c"),
    USER_DISCONNECT_COMMAND("#dc"),
    CHANGE_USER_ACCOUNT_COMMAND("#chuacc"),
    CONNECTED_USERS_REQUEST("#reqcu");
    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
