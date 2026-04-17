package com.parking.model;

import java.sql.Timestamp; // FIX: was incorrectly importing java.security.Timestamp

public class Booking {
    private int id;
    private int userId;
    private int slotId;
    private String slotNumber;
    private String driverName;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private double amount;
    private String status; // ACTIVE, COMPLETED, CANCELLED
    private Timestamp createdAt;

    public Booking() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public Timestamp getCheckIn() { return checkIn; }
    public void setCheckIn(Timestamp checkIn) { this.checkIn = checkIn; }

    public Timestamp getCheckOut() { return checkOut; }
    public void setCheckOut(Timestamp checkOut) { this.checkOut = checkOut; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * Returns the duration in hours (minimum 1).
     */
    public long getDurationHours() {
        if (checkIn == null || checkOut == null) return 0;
        long diff = checkOut.getTime() - checkIn.getTime();
        return Math.max(1, diff / (1000 * 60 * 60));
    }

    /**
     * Convenience: is this booking currently active?
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Convenience: is this booking completed (checked out)?
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Convenience: is this booking cancelled?
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
}