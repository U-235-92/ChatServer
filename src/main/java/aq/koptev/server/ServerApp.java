package aq.koptev.server;

import aq.koptev.server.models.Server;

import java.io.IOException;

public class ServerApp {

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.waitClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
