package server;

import server.infustructre.InfrastructureFactory;
import server.application.ApplicationFactory;

public class App {
    private final InfrastructureFactory infrastructureFactory;
    private final ApplicationFactory applicationFactory;

    public App() {
        this.infrastructureFactory = new InfrastructureFactory();
        this.applicationFactory = new ApplicationFactory();
    }

    public void start() {
        System.out.println("Starting application");

        System.out.println("Creating services");

        System.out.println("Creating socket server");

        System.out.println("Serving socket server");

    }

}
