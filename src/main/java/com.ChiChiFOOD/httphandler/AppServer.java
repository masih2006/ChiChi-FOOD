package com.ChiChiFOOD.httphandler;


import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class AppServer {
    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // in baiad dorost she
        server.createContext("/auth/register", new RegisterHandler());
        server.createContext("/auth/login", new LoginHandler());
        server.createContext("/auth/logout", new AuthFilterHandler(new LogoutHandler()));
        server.createContext("/auth/profile", new AuthFilterHandler(new ProfileHandler()));
        server.createContext("/restaurants", new AuthFilterHandler(new RestaurantHandler()));
        server.createContext("/restaurants/{id}/item", new AuthFilterHandler(new RestaurantItemHandler()));
        server.setExecutor(null); // use default executor
        server.start();
        System.out.println("HTTP server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP server stopped.");
        }
    }
}
