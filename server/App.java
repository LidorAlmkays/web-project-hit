package server;

import server.infustructre.InfrastructureFactory;
import server.application.ApplicationFactory;
import server.api.SocketServer;

public class App {
    private final InfrastructureFactory infrastructureFactory;
    private final ApplicationFactory applicationFactory;

    public App() {
        this.infrastructureFactory = new InfrastructureFactory();
        this.applicationFactory = new ApplicationFactory();
    }

    public void start() {
        System.out.println("Starting application");
        var userAccountRepository = this.infrastructureFactory.createUserAccountRepository();
        var inventoryItemRepository = this.infrastructureFactory.createInventoryItemRepository();
        var chatRoomRepository = this.infrastructureFactory.createChatRoomRepository();

        System.out.println("Creating services");
        var userAccountService = this.applicationFactory.createUserAccountService(userAccountRepository);
        var inventoryItemService = this.applicationFactory.createInventoryItemService(inventoryItemRepository);
        var chatRoomService = this.applicationFactory.createChatRoomService(chatRoomRepository, userAccountService);

        System.out.println("Creating socket server");
        var socketServer = new SocketServer(chatRoomService, inventoryItemService, userAccountService);

        System.out.println("Serving socket server");
        socketServer.serve();
    }

}
