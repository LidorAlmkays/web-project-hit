package server.api;

import server.application.adaptors.AuthService;
import server.application.adaptors.BranchItemService;
import server.application.adaptors.BranchService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;
import server.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;
    private final BranchItemService branchItemService;
    private final BranchService branchService;
    private ServerSocket serverSocket;
    private boolean running;
    private HandlerFactory handlerFactory;

    public SocketServer(AuthService authService, LoggerService logService, EmployeeService employeeService,
            BranchItemService branchItemService, BranchService branchService) {
        this.branchItemService = branchItemService;
        this.branchService = branchService;
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
        this.running = false;
        this.handlerFactory = new HandlerFactory(authService, logService, employeeService, branchItemService,
                branchService);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Config.SOCKET_PORT);
            running = true;
            System.out.println("Socket server started on port " + Config.SOCKET_PORT);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                ClientSocketHandler socketService = new ClientSocketHandler(clientSocket, handlerFactory);
                Thread clientThread = new Thread(socketService);
                clientThread.start();
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Error in socket server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
        }
        System.out.println("Socket server stopped");
    }

}
