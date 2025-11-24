package server.domain;

import java.util.UUID;

public class Branch {
    private final UUID branchId; // index
    private String branchName;
    private String address;
    private String phoneNumber;
    private UUID managerId; // Optional - for future Employee reference
    private boolean isActive;

    public Branch(String branchName, String address, String phoneNumber) {
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("branchName must not be null or empty");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber must not be null");
        }
        this.branchId = UUID.randomUUID();
        this.branchName = branchName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.managerId = null;
        this.isActive = true;
    }

    public Branch(UUID branchId, String branchName, String address, String phoneNumber, UUID managerId,
            boolean isActive) {
        if (branchId == null) {
            throw new IllegalArgumentException("branchId must not be null");
        }
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("branchName must not be null or empty");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber must not be null");
        }
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.managerId = managerId;
        this.isActive = isActive;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("branchName must not be null or empty");
        }
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber must not be null");
        }
        this.phoneNumber = phoneNumber;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append(branchId.toString()).append("\n");
        sb.append(branchName).append("\n");
        sb.append(address).append("\n");
        sb.append(phoneNumber).append("\n");
        sb.append(managerId != null ? managerId.toString() : "null").append("\n");
        sb.append(isActive);
        return sb.toString();
    }

    public static Branch decodeFromString(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content must not be null or empty");
        }

        String[] lines = content.split("\n");
        if (lines.length < 6) {
            throw new IllegalArgumentException("Invalid branch data format");
        }

        UUID branchId = UUID.fromString(lines[0].trim());
        String branchName = lines[1].trim();
        String address = lines[2].trim();
        String phoneNumber = lines[3].trim();
        UUID managerId = lines[4].trim().equals("null") ? null : UUID.fromString(lines[4].trim());
        boolean isActive = Boolean.parseBoolean(lines[5].trim());

        return new Branch(branchId, branchName, address, phoneNumber, managerId, isActive);
    }

    public Branch createCopy() {
        return new Branch(
                this.branchId,
                this.branchName,
                this.address,
                this.phoneNumber,
                this.managerId,
                this.isActive);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "branchId=" + branchId +
                ", branchName='" + branchName + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", managerId=" + managerId +
                ", isActive=" + isActive +
                '}';
    }
}

