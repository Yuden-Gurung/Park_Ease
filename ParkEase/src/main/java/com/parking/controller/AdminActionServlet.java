package com.parking.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

import com.parking.dao.BookingDAO;
import com.parking.dao.SlotDAO;
import com.parking.dao.UserDAO;
import com.parking.model.User;

/**
 * Servlet implementation class AdminActionServlet
 */
@WebServlet("/AdminAction")
public class AdminActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final UserDAO    userDAO    = new UserDAO();
    private final SlotDAO    slotDAO    = new SlotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminActionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.sendRedirect(request.getContextPath() + "/AdminDashboard");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// 1. Session + role guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
 
        User admin = (User) session.getAttribute("user");
        if (!"ADMIN".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/DriverDashboard");
            return;
        }
 
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            return;
        }
 
        try {
            switch (action.trim()) {
 
                // ── User management ──────────────────────────────────────────
                case "lock": {
                    int userId = parseInt(request.getParameter("userId"));
                    if (userId > 0 && userId != admin.getId()) {   // can't lock yourself
                        userDAO.lockUser(userId);
                    }
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard?section=users&msg=locked");
                    break;
                }
 
                case "unlock": {
                    int userId = parseInt(request.getParameter("userId"));
                    if (userId > 0) {
                        userDAO.unlockUser(userId);
                    }
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard?section=users&msg=unlocked");
                    break;
                }
 
                case "delete": {
                    int userId = parseInt(request.getParameter("userId"));
                    if (userId > 0 && userId != admin.getId()) {   // can't delete yourself
                        userDAO.deleteUser(userId);
                    }
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard?section=users&msg=deleted");
                    break;
                }
 
                // ── Slot management ──────────────────────────────────────────
                case "releaseSlot": {
                    int slotId = parseInt(request.getParameter("slotId"));
                    if (slotId > 0) {
                        slotDAO.releaseSlot(slotId);
                    }
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard?section=slots&msg=released");
                    break;
                }
 
                // ── Booking management ───────────────────────────────────────
                case "cancelBooking": {
                    int bookingId = parseInt(request.getParameter("bookingId"));
                    int slotId    = parseInt(request.getParameter("slotId"));
                    if (bookingId > 0 && slotId > 0) {
                        bookingDAO.cancelBooking(bookingId, slotId);
                    }
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard?section=bookings&msg=cancelled");
                    break;
                }
 
                default:
                    response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            }
 
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/AdminDashboard?error=true");
        }
    }
 
    // ── Helpers ──────────────────────────────────────────────────────────────
 
    private int parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return -1; }
    }
}