package server.api.handlers;

import java.net.Socket;

import server.application.adaptors.AuthService;

public class LoginEmployeeHandler implements SocketHandler {
    private final AuthService authService;

    public LoginEmployeeHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {

    }
}
