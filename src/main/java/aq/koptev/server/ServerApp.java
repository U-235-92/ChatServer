package aq.koptev.server;

import aq.koptev.server.models.Server;

public class ServerApp {

    public static void main(String[] args) {
        Server server = new Server();
        server.waitClient();
    }
}
