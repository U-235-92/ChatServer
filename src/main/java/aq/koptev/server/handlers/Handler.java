package aq.koptev.server.handlers;

import aq.koptev.server.models.User;

import java.io.IOException;

public interface Handler {
    void sendMessage(String command, String message) throws IOException;
    void sendMessage(String command, User sender, String message) throws IOException;
    void sendMessage(String command, User sender, User receiver, String message) throws IOException;
    void waitMessage() throws IOException;
    User getUser();
    void setUser(User user);
    void handle();
    void registrationHandler();
    void closeConnection() throws IOException;
    boolean isConnected(User user);
}
