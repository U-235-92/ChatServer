package aq.koptev.server.models;

public enum Command {

    AUTHENTICATION_COMMAND("#authentication"),
    REGISTRATION_COMMAND("#registration"),
    COMMON_MESSAGE_COMMAND("#common_msg"),
    PRIVATE_SERVER_MESSAGE("#private_server_message"),
    PRIVATE_MESSAGE_COMMAND("#private_user_message"),
    ERROR_AUTHENTICATION_COMMAND("#err_autentication"),
    ERROR_REGISTRATION_COMMAND("#err_registration"),
    ERROR_CHANGE_USER_ACCOUNT_SETTINGS_COMMAND("#err_change_user_settings"),
    OK_AUTHENTICATION_COMMAND("#ok_authentication"),
    OK_REGISTRATION_COMMAND("#ok_registration"),
    OK_CHANGE_USER_ACCOUNT_SETTINGS_COMMAND("#ok_change_user_settings"),
    USER_CONNECT_COMMAND("#user_connected"),
    USER_DISCONNECT_COMMAND("#user_disconected"),
    GET_CONNECTED_USERS_COMMAND("#get_connected_users"),
    SET_CONNECTED_USERS_COMMAND("#set_connected_users"),
    GET_CONNECTED_USER_META_COMMAND("#get_connected_user_meta"),
    SET_CONNECTED_USER_META_COMMAND("#set_connected_user_meta"),
    CHANGE_USER_ACCOUNT_SETTINGS_COMMAND("#change_user_settings"),
    RECEIVE_MESSAGE_COMMAND("#receive_message"),
    UNSUPPORTED_COMMAND("#unsupported_command");
    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommandByValue(String command) {
        for(Command c : Command.values()) {
            if(c.getCommand().equals(command)) {
                return c;
            }
        }
        return null;
    }
}
