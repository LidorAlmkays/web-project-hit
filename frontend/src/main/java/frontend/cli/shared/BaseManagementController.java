package frontend.cli.shared;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;

import java.io.IOException;
import java.util.Scanner;

public class BaseManagementController<V extends BaseManagementView> {
    protected static final Gson gson = new Gson();
    protected final IClientTransport client;
    protected final V view;
    protected final Scanner scanner;

    protected BaseManagementController(IClientTransport client, V view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
    }

    protected SocketMessage sendOrReport(EventType event, Object request, String errorPrefix) throws IOException {
        SocketMessage response = client.send(event, request);
        if (response == null) {
            view.error(errorPrefix + "No response from server.");
            return null;
        }
        Object data = response.getData();
        if (data instanceof String) {
            view.error(errorPrefix + data);
            return null;
        }
        return response;
    }
}
