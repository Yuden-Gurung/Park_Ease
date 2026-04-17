package com.parking.dao;

import com.parking.config.DBConfig;
import com.parking.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public void createBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, slot_id, check_in, check_out, amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getSlotId());
            ps.setTimestamp(3, booking.getCheckIn());
            ps.setTimestamp(4, booking.getCheckOut());
            ps.setDouble(5, booking.getAmount());
            ps.executeUpdate();
        }
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name AS driver_name, s.slot_number " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN parking_slots s ON b.slot_id = s.id " +
                     "ORDER BY b.created_at DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Booking> getBookingsByUser(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name AS driver_name, s.slot_number " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN parking_slots s ON b.slot_id = s.id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.created_at DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Booking> getRecentBookings(int limit) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name AS driver_name, s.slot_number " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN parking_slots s ON b.slot_id = s.id " +
                     "ORDER BY b.created_at DESC LIMIT ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countActiveBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'ACTIVE'";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countUserActiveBookings(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countUserTotalBookings(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE user_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM bookings WHERE status != 'CANCELLED'";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    /**
     * Cancels a booking and frees the slot in a single transaction.
     * Sets booking status → CANCELLED.
     */
    public void cancelBooking(int bookingId, int slotId) throws SQLException {
        runBookingTransaction(bookingId, slotId, "CANCELLED");
    }

    /**
     * FIX: New method — completes a booking (driver checks out) and frees the slot.
     * Sets booking status → COMPLETED.
     */
    public void completeBooking(int bookingId, int slotId) throws SQLException {
        runBookingTransaction(bookingId, slotId, "COMPLETED");
    }

    /**
     * Shared transaction: update booking status + free the parking slot atomically.
     */
    private void runBookingTransaction(int bookingId, int slotId, String newStatus)
            throws SQLException {
        Connection conn = null;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false);

            String sql1 = "UPDATE bookings SET status = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, newStatus);
                ps.setInt(2, bookingId);
                ps.executeUpdate();
            }

            String sql2 = "UPDATE parking_slots SET status = 'FREE' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, slotId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, u.full_name AS driver_name, s.slot_number " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN parking_slots s ON b.slot_id = s.id " +
                     "WHERE b.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setSlotNumber(rs.getString("slot_number"));
        b.setDriverName(rs.getString("driver_name"));
        b.setCheckIn(rs.getTimestamp("check_in"));
        b.setCheckOut(rs.getTimestamp("check_out"));
        b.setAmount(rs.getDouble("amount"));
        b.setStatus(rs.getString("status"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}