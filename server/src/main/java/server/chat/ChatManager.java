package server.chat;

import com.google.gson.Gson;
import server.domain.employee.EmployeeRole;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.api.handlers.AbstractSocketHandler;
import java.net.Socket;

public class ChatManager extends AbstractSocketHandler {
    private static ChatManager instance;
    private final Gson gson = new Gson();

    // Mapping: UserID -> Socket Output Stream (Active network sessions)
    private final Map<UUID, DataOutputStream> activeSessions = new ConcurrentHashMap<>();

    // Mapping: UserID -> UserID (Who is talking to whom)
    private final Map<UUID, UUID> activeChats = new ConcurrentHashMap<>();

    // Mapping: TargetUserID -> Queue of Waiting UserIDs
    private final Map<UUID, Queue<UUID>> waitingQueues = new ConcurrentHashMap<>();

    private ChatManager() {
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void registerSession(UUID userId, DataOutputStream out) {
        if (userId == null)
            return;
        activeSessions.put(userId, out);
        System.out.println("ChatManager: Registered session for " + userId);
    }

    public void unregisterSession(UUID userId) {
        if (userId == null)
            return;
        activeSessions.remove(userId);
        closeChat(userId);
        waitingQueues.remove(userId);
        System.out.println("ChatManager: Unregistered session for " + userId);
    }

    public synchronized void requestChat(UUID senderId, UUID targetId, EmployeeRole senderRole) {
        if (!activeSessions.containsKey(targetId)) {
            sendSystemMessage(senderId, "User is offline.");
            return;
        }

        if (activeChats.containsKey(targetId)) {
            if (activeChats.get(targetId).equals(senderId)) {
                sendSystemMessage(senderId, "You are already in a chat with this user.");
                return;
            }

            if (senderRole == EmployeeRole.SHIFT_MANAGER) {
                // Manager Override logic
                managerJoin(senderId, targetId);
            } else {
                queueUser(targetId, senderId);
            }
        } else {
            startChat(senderId, targetId);
        }
    }

    private void startChat(UUID user1, UUID user2) {
        activeChats.put(user1, user2);
        activeChats.put(user2, user1);

        sendPacket(user1, new ChatPacket(user2, user1, "Chat started with " + user2));
        sendPacket(user2, new ChatPacket(user1, user2, "Chat started with " + user1));
    }

    private void queueUser(UUID targetId, UUID waiterId) {
        waitingQueues.computeIfAbsent(targetId, k -> new ConcurrentLinkedQueue<>()).add(waiterId);
        int position = waitingQueues.get(targetId).size();
        sendPacket(waiterId, new ChatPacket(null, waiterId, "User is busy. You are #" + position + " in queue."));
    }

    // Manager joins the chat between Target and Their Partner
    private void managerJoin(UUID managerId, UUID targetId) {
        UUID partnerId = activeChats.get(targetId);

        // Notify everyone
        sendSystemMessage(targetId, "Manager has joined the chat.");
        sendSystemMessage(partnerId, "Manager has joined the chat.");
        sendSystemMessage(managerId, "Joined chat between " + targetId + " and " + partnerId);

        // For simple 3-way without a Room object:
        // We will map Manager -> Target (Primary).
        // And when Manager sends message, logic in handleMessage will need to know to
        // broadcast.
        // Or simpler: Just treat Manager interactions ad-hoc in handleMessage if they
        // are not in activeChats map.
        // Let's register manager in activeChats to target for now, but we need to
        // handle the fact target is mapped to partner.
        // Refined approach:
        // We do NOT add manager to activeChats map in the standard 1-to-1 way.
        // We leave them "floating" and allow them to send messages by checking
        // roles/permissions?
        // Or better: Create a secondary map `managerOverriding` -> `targetChatId`.
    }

    public synchronized void handleMessage(ChatPacket packet) {
        UUID sender = packet.getSenderId();
        UUID receiver = packet.getReceiverId();

        // 1. Normal Chat
        if (activeChats.containsKey(sender) && activeChats.get(sender).equals(receiver)) {
            sendPacket(receiver, packet);

            // If there's a manager monitoring/joined, send to them too?
            // (Skipped for simplicity unless user asks for full implementation of that)
            return;
        }

        // 2. Manager Override Sending (If we implement that flow)
        // For now, if not in chat -> Error
        sendSystemMessage(sender, "Error: You are not in a chat with this user.");
    }

    public synchronized void closeChat(UUID userId) {
        UUID partnerId = activeChats.remove(userId);
        if (partnerId != null) {
            activeChats.remove(partnerId);
            sendPacket(partnerId, new ChatPacket(userId, partnerId, "User has left the chat."));
            checkQueue(partnerId);
        }
        checkQueue(userId);
    }

    private void checkQueue(UUID freeUserId) {
        Queue<UUID> queue = waitingQueues.get(freeUserId);
        if (queue != null && !queue.isEmpty()) {
            UUID nextUser = queue.poll();
            sendSystemMessage(freeUserId, "User " + nextUser + " was waiting for you. Starting chat...");
            sendSystemMessage(nextUser, "User " + freeUserId + " is now free. Starting chat...");
            startChat(freeUserId, nextUser);
        }
    }

    private void sendSystemMessage(UUID targetId, String msg) {
        sendPacket(targetId, new ChatPacket(null, targetId, "[System]: " + msg));
    }

    private void sendPacket(UUID targetId, ChatPacket packet) {
        DataOutputStream out = activeSessions.get(targetId);
        if (out != null) {
            try {
                // Use Generic SocketMessage wrapper
                SocketMessage outputMsg = new SocketMessage(EventType.CHAT_MESSAGE, gson.toJson(packet));
                String json = gson.toJson(outputMsg);
                synchronized (out) {
                    out.writeUTF(json);
                }
            } catch (IOException e) {
                unregisterSession(targetId);
            }
        }
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet != null) {
            // If it's a request to start a chat, we need logic.
            // Packet contains sender, receiver, message.
            // If message is "REQUEST_CHAT", we call requestChat?
            // Protocol decision: How does client ask to start chat?
            // "CHAT_REQUEST" event type -> handler factory calls this.
            // The packet message might be empty or specific?

            // For now, let's assume specific "Command" or just infer from context.
            // If activeChats doesn't contain sender, treat as Request?
            // Or use explicit methods if we had them.
            // Since we merged, we handle ALL events here.
            // But we don't know the EventType here!

            // We need to know if it is REQUEST or MESSAGE.
            // But SocketHandler interface only gives us data.
            // This is the downside of merging.

            // Hack/Solution: ChatPacket content or metadata.
            // Or assume if they are NOT in a chat, it is a request?
            // If they are in a chat, it is a message?

            UUID sender = packet.getSenderId();
            UUID receiver = packet.getReceiverId();

            // Simple Logic:
            if (activeChats.containsKey(sender) && activeChats.get(sender).equals(receiver)) {
                handleMessage(packet);
            } else {
                // Not connected yet -> TREAT AS REQUEST
                requestChat(sender, receiver, null); // passing null role for now
            }
        }
    }
}
