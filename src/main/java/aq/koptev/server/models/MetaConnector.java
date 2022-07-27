package aq.koptev.server.models;

import aq.koptev.server.handlers.ServerHandler;

import java.io.DataInputStream;
import java.io.IOException;

public class MetaConnector {

    private Server server;
    private User user;
    private DataInputStream inputStream;
    private boolean isWork = true;

    public MetaConnector(Server server, User user, DataInputStream inputStream) {
        this.server = server;
        this.user = user;
        this.inputStream = inputStream;
    }

    public void processConnectMeta() throws IOException {
        while(isWork) {
//            handler.sendMessage(Command.GET_CONNECTED_USER_COMMAND, String.format("%s %s", user.getLogin(), user.getPassword()));
            server.processMessage(Command.GET_CONNECTED_USER_COMMAND.getCommand(), String.format("%s %s", user.getLogin(), user.getPassword()));
            String answer = inputStream.readUTF();
            if(answer.trim().equals(Command.RECEIVE_MESSAGE_COMMAND.getCommand())) {
//                handler.sendMessage(Command.USER_CONNECT_COMMAND, String.format("Пользователь %s вошел в чат", user.getLogin()));
                server.processMessage(Command.USER_CONNECT_COMMAND.getCommand(), String.format("Пользователь %s вошел в чат", user.getLogin()));
            }
            answer = inputStream.readUTF();
            if(answer.trim().equals(Command.RECEIVE_MESSAGE_COMMAND.getCommand())) {
//                handler.sendMessage(Command.GET_CONNECTED_USERS_COMMAND, null);
                server.processMessage(Command.GET_CONNECTED_USER_COMMAND.getCommand(), null);
            }
            answer = inputStream.readUTF();
            if(answer.trim().equals(Command.RECEIVE_MESSAGE_COMMAND.getCommand())) {
                isWork = false;
            }
        }
    }
}
