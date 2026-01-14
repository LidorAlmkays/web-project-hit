package server.chat;

import java.util.UUID;

/**
 * Represents a participant in a chat session.
 */
public class ChatParticipant {
    private final String email;
    private final UUID branchId;
    private final boolean isManager;

    public ChatParticipant(String email, UUID branchId, boolean isManager) {
        this.email = email;
        this.branchId = branchId;
        this.isManager = isManager;
    }

    public String getEmail() {
        return email;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public boolean isManager() {
        return isManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChatParticipant that = (ChatParticipant) o;
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
