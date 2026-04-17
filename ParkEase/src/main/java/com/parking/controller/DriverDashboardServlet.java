package com.parking.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.util.List;

import com.parking.dao.BookingDAO;
import com.parking.dao.SlotDAO;
import com.parking.model.Booking;
import com.parking.model.ParkingSlot;
import com.parking.model.User;

/**
 * Servlet implementation class DriverDashboardServlet
 */
@WebServlet("/DriverDashboard")
public class DriverDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final SlotDAO    slotDAO    = new SlotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DriverDashboardServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
 
        // 2. Role guard
        User user = (User) session.getAttribute("user");
        if (!"DRIVER".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            return;
        }
 
        // 3. Load all data from DAOs and set as request attributes
        try {
            int               freeCount   = slotDAO.countFreeSlots();
            int               totalCount  = bookingDAO.countUserTotalBookings(user.getId());
            int               activeCount = bookingDAO.countUserActiveBookings(user.getId());
            List<ParkingSlot> allSlots    = slotDAO.getAllSlots();
            List<Booking>     myBookings  = bookingDAO.getBookingsByUser(user.getId());
 
            request.setAttribute("freeCount",   freeCount);
            request.setAttribute("totalCount",  totalCount);
            request.setAttribute("activeCount", activeCount);
            request.setAttribute("allSlots",    allSlots);
            request.setAttribute("myBookings",  myBookings);
 
        } catch (Exception e) {
            e.printStackTrace();
            // Still forward — JSP will show empty states rather than crashing
        }
 
        // 4. Forward to JSP
        request.getRequestDispatcher("/WEB-INF/pages/DriverDashboard.jsp")
               .forward(request, response);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
