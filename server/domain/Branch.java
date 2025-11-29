package server.domain;

import java.util.UUID;

public class Branch {
    private final UUID branchId; // index
    private String branchName;
    private String address;
    private String phoneNumber;
    private int totalSold;
    private double totalMoneyEarned;

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
        this.totalSold = 0;
        this.totalMoneyEarned = 0.0;
    }

    public Branch(UUID branchId, String branchName, String address, String phoneNumber, int totalSold,
            double totalMoneyEarned) {
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
        if (totalSold < 0) {
            throw new IllegalArgumentException("totalSold must be non-negative");
        }
        if (totalMoneyEarned < 0) {
            throw new IllegalArgumentException("totalMoneyEarned must be non-negative");
        }
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.totalSold = totalSold;
        this.totalMoneyEarned = totalMoneyEarned;
    }

    // getters

    public UUID getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public double getTotalMoneyEarned() {
        return totalMoneyEarned;
    }

    // setters

    public void setBranchName(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("branchName must not be null or empty");
        }
        this.branchName = branchName;
    }

    public void setAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber must not be null");
        }
        this.phoneNumber = phoneNumber;
    }

    public void addSale(int quantity, double amount) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "quantity needs to be bigger then 0, whats the point of selling 0 items????");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount cant be below 0, Im not going to give you free money");
        }
        this.totalSold += quantity;
        this.totalMoneyEarned += amount;
    }

    public Branch createCopy() {
        return new Branch(
                this.branchId,
                this.branchName,
                this.address,
                this.phoneNumber,
                this.totalSold,
                this.totalMoneyEarned);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "branchId=" + branchId +
                ", branchName='" + branchName + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", totalSold=" + totalSold +
                ", totalMoneyEarned=" + totalMoneyEarned +
                '}';
    }
}
