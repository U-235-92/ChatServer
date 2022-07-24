package aq.koptev.server.handlers;

import aq.koptev.server.models.Command;
import aq.koptev.server.models.User;

import java.io.IOException;

public interface Handler {
    void sendMessage(Command command, String message) throws IOException;
    void sendMessage(Command command, String sender, String message) throws IOException;
    void sendMessage(Command command, String sender, String receiver, String message) throws IOException;
    void waitMessage() throws IOException;
    User getUser();
    void setUser(User user);
    void handle();
    void registrationHandler();
    void closeConnection() throws IOException;
    boolean isConnected(String login);
}
