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
import com.parking.model.Booking;
import com.parking.model.User;
/**
 * Servlet implementation class CancelBookingServelt
 */
@WebServlet("/CancelBooking")
public class CancelBookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CancelBookingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.sendRedirect(request.getContextPath() + "/DriverDashboard");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
 
        User user = (User) session.getAttribute("user");
 
        // 2. Determine the action: "cancel" or "checkout"
        String action      = request.getParameter("action");      // "cancel" | "checkout"
        String bookingIdStr = request.getParameter("bookingId");
        String slotIdStr    = request.getParameter("slotId");
 
        if (bookingIdStr == null || slotIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/DriverDashboard");
            return;
        }
 
        try {
            int bookingId = Integer.parseInt(bookingIdStr);
            int slotId    = Integer.parseInt(slotIdStr);
 
            BookingDAO bookingDAO = new BookingDAO();
 
            // 3. Ownership check — make sure this booking belongs to this user
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null || booking.getUserId() != user.getId()) {
                response.sendRedirect(request.getContextPath() + "/DriverDashboard");
                return;
            }
 
            // FIX: Only allow action on ACTIVE bookings
            if (!"ACTIVE".equals(booking.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/DriverDashboard?alreadyDone=true");
                return;
            }
 
            if ("checkout".equals(action)) {
                // FIX: Proper checkout — marks booking COMPLETED, frees slot
                bookingDAO.completeBooking(bookingId, slotId);
                response.sendRedirect(request.getContextPath() + "/DriverDashboard?checkedout=true");
            } else {
                // Default: cancel — marks booking CANCELLED, frees slot
                bookingDAO.cancelBooking(bookingId, slotId);
                response.sendRedirect(request.getContextPath() + "/DriverDashboard?cancelled=true");
            }
 
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DriverDashboard?error=true");
        }
	}

}
