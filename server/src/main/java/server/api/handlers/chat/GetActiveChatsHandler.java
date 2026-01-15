package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import server.domain.chat.ChatSession;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.EmployeeRepository;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ActiveChatInfo;
import shareddto.chat.ChatPacket;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetActiveChatsHandler extends AbstractSocketHandler {

    private final EmployeeRepository employeeRepository;

    public GetActiveChatsHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null || packet.getMessage() == null) {
            sendError(clientSocket, "Invalid request");
            return;
        }

        String managerEmail = packet.getMessage();

        // Get manager's role and branch
        Employee manager = employeeRepository.findByEmail(managerEmail).orElse(null);
        if (manager == null) {
            sendError(clientSocket, "Manager not found");
            return;
        }

        EmployeeRole role = manager.getRole();
        if (role != EmployeeRole.ADMIN && role != EmployeeRole.SHIFT_MANAGER) {
            sendError(clientSocket, "Access denied");
            return;
        }

        boolean isAdmin = role == EmployeeRole.ADMIN;
        UUID branchId = manager.getBranchId();

        ChatManager chatManager = ChatManager.getInstance();
        List<ChatSession> sessions = chatManager.getActiveChats(managerEmail, branchId, isAdmin);

        // Convert to DTOs
        List<ActiveChatInfo> infos = new ArrayList<>();
        for (ChatSession session : sessions) {
            infos.add(new ActiveChatInfo(
                    session.getSessionId(),
                    session.getDisplayString(),
                    session.getStartTime()));
        }

        // Serialize to JSON
        String jsonList = gson.toJson(infos);
        ChatPacket response = new ChatPacket(null, null, jsonList);
        sendMessage(clientSocket, new SocketMessage(EventType.GET_ACTIVE_CHATS, response));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        ChatPacket errorPacket = new ChatPacket(null, null, "ERROR:" + message);
        sendMessage(clientSocket, new SocketMessage(EventType.GET_ACTIVE_CHATS, errorPacket));
    }
}
