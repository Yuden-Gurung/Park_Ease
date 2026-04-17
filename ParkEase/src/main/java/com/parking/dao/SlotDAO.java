package com.parking.dao;

import com.parking.config.DBConfig;
import com.parking.model.ParkingSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SlotDAO {

    public List<ParkingSlot> getAllSlots() throws SQLException {
        List<ParkingSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM parking_slots ORDER BY slot_number";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                slots.add(mapRow(rs));
            }
        }
        return slots;
    }

    public List<ParkingSlot> getFreeSlots() throws SQLException {
        List<ParkingSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM parking_slots WHERE status = 'FREE' ORDER BY slot_number";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                slots.add(mapRow(rs));
            }
        }
        return slots;
    }

    public int countFreeSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE status = 'FREE'";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countTotalSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public ParkingSlot findBySlotNumber(String slotNumber) throws SQLException {
        String sql = "SELECT * FROM parking_slots WHERE slot_number = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void updateSlotStatus(int slotId, String status) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, slotId);
            ps.executeUpdate();
        }
    }

    public void releaseSlot(int slotId) throws SQLException {
        updateSlotStatus(slotId, "FREE");
    }

    private ParkingSlot mapRow(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setId(rs.getInt("id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setStatus(rs.getString("status"));
        return slot;
    }
}