package com.parking.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.parking.dao.BookingDAO;
import com.parking.dao.SlotDAO;
import com.parking.model.Booking;
import com.parking.model.ParkingSlot;
import com.parking.model.User;

/**
 * Servlet implementation class BookingServlet
 */
@WebServlet("/Booking")
public class BookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final double RATE_STANDARD = 20.0;  // per hour, 6am–10pm
    private static final double RATE_NIGHT    = 10.0;  // per hour, 10pm–6am
    private static final double RATE_DAY      = 150.0; // flat per 24-hour day  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BookingServlet() {
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
 
        // 2. Get form values
        String slotNumber  = request.getParameter("slotNumber");
        String checkInStr  = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
 
        // 3. Null / empty validation
        if (slotNumber == null || slotNumber.trim().isEmpty() ||
            checkInStr  == null || checkInStr.trim().isEmpty()  ||
            checkOutStr == null || checkOutStr.trim().isEmpty()) {
            forwardWithError(request, response, "Please fill in all fields.");
            return;
        }
 
        try {
            LocalDateTime checkIn  = LocalDateTime.parse(request.getParameter("checkIn"));
            LocalDateTime checkOut = LocalDateTime.parse(request.getParameter("checkOut"));
            LocalDateTime nowInNepal = LocalDateTime.now(ZoneId.of("Asia/Kathmandu"));

         // Allow check-in within the current minute (buffer of 1 minute back)
         if (checkIn.isBefore(nowInNepal.minusMinutes(1))) {
             forwardWithError(request, response, "Check-in time must be in the future.");
             return;
            }
 
            // FIX 2: checkOut must be after checkIn
            if (!checkOut.isAfter(checkIn)) {
                forwardWithError(request, response, "Check-out time must be after check-in time.");
                return;
            }
 
            // 4. Slot lookup & availability
            SlotDAO slotDAO = new SlotDAO();
            ParkingSlot slot = slotDAO.findBySlotNumber(slotNumber);
 
            if (slot == null) {
                forwardWithError(request, response, "Slot '" + slotNumber + "' not found.");
                return;
            }
 
            if (!"FREE".equals(slot.getStatus())) {
                forwardWithError(request, response, "Sorry, slot " + slotNumber + " is already booked.");
                return;
            }
 
            // 5. FIX 3: Correct amount calculation
            double amount = calculateAmount(checkIn, checkOut);
 
            // 6. Create booking
            Booking booking = new Booking();
            booking.setUserId(user.getId());
            booking.setSlotId(slot.getId());
            booking.setCheckIn(Timestamp.valueOf(checkIn));
            booking.setCheckOut(Timestamp.valueOf(checkOut));
            booking.setAmount(amount);
 
            BookingDAO bookingDAO = new BookingDAO();
            bookingDAO.createBooking(booking);
 
            // 7. Mark slot as BOOKED
            slotDAO.updateSlotStatus(slot.getId(), "BOOKED");
 
            // 8. Redirect with success
            response.sendRedirect(request.getContextPath() + "/DriverDashboard?booked=true");
 
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DriverDashboard?error=true");
        }
    }
 
    /**
     * Calculates the total parking charge between two times.
     *
     * Logic:
     *  - Walk through the booking hour-by-hour.
     *  - Each hour 10pm–6am  → RATE_NIGHT  (₹10/hr)
     *  - Each hour 6am–10pm  → RATE_STANDARD (₹20/hr)
     *  - For every complete 24-hour block, use the flat RATE_DAY (₹150/day)
     *    if it is cheaper than paying hourly for the same period.
     */
    private double calculateAmount(LocalDateTime checkIn, LocalDateTime checkOut) {
        long totalMinutes = java.time.Duration.between(checkIn, checkOut).toMinutes();
        if (totalMinutes < 60) totalMinutes = 60; // minimum 1 hour charge
 
        // FIX 3: correct day-rate override
        long fullDays    = totalMinutes / (24 * 60);
 
        // Charge full days at the flat daily rate
        double amount = fullDays * RATE_DAY;
 
        // Charge the remaining partial day hour-by-hour
        LocalDateTime cursor = checkIn.plusDays(fullDays);
        LocalDateTime end    = checkOut;
 
        while (cursor.isBefore(end)) {
            LocalDateTime next = cursor.plusHours(1);
            if (next.isAfter(end)) next = end;
 
            int hour = cursor.getHour();
            boolean isNight = (hour >= 22 || hour < 6);
 
            double hours = java.time.Duration.between(cursor, next).toMinutes() / 60.0;
            amount += hours * (isNight ? RATE_NIGHT : RATE_STANDARD);
 
            cursor = next;
        }
 
        return Math.round(amount * 100.0) / 100.0; // round to 2 decimal places
    }
 
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("bookingError", message);
        request.getRequestDispatcher("/WEB-INF/pages/DriverDashboard.jsp").forward(request, response);
    }
 
}
