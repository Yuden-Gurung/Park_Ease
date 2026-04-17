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
import com.parking.dao.UserDAO;
import com.parking.model.Booking;
import com.parking.model.ParkingSlot;
import com.parking.model.User;

/**
 * Servlet implementation class AdminDashboardServlet
 */
@WebServlet("/AdminDashboard")
public class AdminDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 private final UserDAO    userDAO    = new UserDAO();
	 private final SlotDAO    slotDAO    = new SlotDAO();
	 private final BookingDAO bookingDAO = new BookingDAO();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminDashboardServlet() {
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
        if (!"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/DriverDashboard");
            return;
        }
 
        // 3. Load all data from DAOs and set as request attributes
        try {
            // Stat cards
            List<User>        allUsers       = userDAO.getAllUsers();
            int               freeSlots      = slotDAO.countFreeSlots();
            int               activeBookings = bookingDAO.countActiveBookings();
            double            totalRevenue   = bookingDAO.getTotalRevenue();
 
            // Tables / maps
            List<ParkingSlot> allSlots        = slotDAO.getAllSlots();
            List<Booking>     recentBookings  = bookingDAO.getRecentBookings(10);
            List<Booking>     allBookings     = bookingDAO.getAllBookings();
 
            request.setAttribute("totalUsers",     allUsers.size());
            request.setAttribute("allUsers",        allUsers);
            request.setAttribute("freeSlots",       freeSlots);
            request.setAttribute("activeBookings",  activeBookings);
            request.setAttribute("totalRevenue",    totalRevenue);
            request.setAttribute("allSlots",        allSlots);
            request.setAttribute("recentBookings",  recentBookings);
            request.setAttribute("allBookings",     allBookings);
 
        } catch (Exception e) {
            e.printStackTrace();
            // Still forward — JSP will show empty states rather than crashing
        }
 
        // 4. Forward to JSP
        request.getRequestDispatcher("/WEB-INF/pages/AdminDashboard.jsp")
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
