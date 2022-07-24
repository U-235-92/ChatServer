package aq.koptev.server.models;

public enum Command {

    AUTHENTICATION_COMMAND("#authentication"),
    REGISTRATION_COMMAND("#registration"),
    COMMON_MESSAGE_COMMAND("#common_msg"),
    PRIVATE_SERVER_MESSAGE("#private_server_message"),
    PRIVATE_USER_MESSAGE_COMMAND("#private_user_message"),
    ERROR_AUTHENTICATION_COMMAND("#err_autentication"),
    ERROR_REGISTRATION_COMMAND("#err_registration"),
    ERROR_CHANGE_LOGIN_MESSAGE("#err_change_login"),
    OK_AUTHENTICATION_COMMAND("#ok_authentication"),
    OK_REGISTRATION_COMMAND("#ok_registration"),
    OK_CHANGE_LOGIN_MESSAGE("#ok_change_login"),
    USER_CONNECT_COMMAND("#user_connected"),
    USER_DISCONNECT_COMMAND("#user_disconected"),
    GET_CONNECTED_USERS_COMMAND("#get_connected_users"),
    CHANGE_USER_LOGIN_COMMAND("#change_user_login");
    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
