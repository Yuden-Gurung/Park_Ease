package com.parking.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import com.parking.dao.UserDAO;
import com.parking.model.User;
import com.parking.util.PasswordUtil;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/Register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 HttpSession session = request.getSession(false);
	        if (session != null && session.getAttribute("user") != null) {
	            User user = (User) session.getAttribute("user");
	            if ("ADMIN".equals(user.getRole())) {
	                response.sendRedirect(request.getContextPath() + "/AdminDashboard");
	            } else {
	                response.sendRedirect(request.getContextPath() + "/DriverDashboard");
	            }
	            return;
	        }
	        // FIX: leading slash on dispatcher path
	        request.getRequestDispatcher("/WEB-INF/pages/Register.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 String fullName        = request.getParameter("fullName");
	        String email           = request.getParameter("email");
	        String password        = request.getParameter("password");
	        String confirmPassword = request.getParameter("confirmPassword");
	 
	        // ── Validation ──────────────────────────────────────────────────────
	        if (fullName == null || fullName.trim().isEmpty()) {
	            forwardWithError(request, response, "Full name is required.");
	            return;
	        }
	        if (!fullName.trim().matches("[a-zA-Z ]+")) {
	            forwardWithError(request, response, "Full name must contain letters only.");
	            return;
	        }
	        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
	            forwardWithError(request, response, "Invalid email address.");
	            return;
	        }
	        if (password == null || password.length() < 6) {
	            forwardWithError(request, response, "Password must be at least 6 characters.");
	            return;
	        }
	        if (!password.equals(confirmPassword)) {
	            forwardWithError(request, response, "Passwords do not match.");
	            return;
	        }
	 
	        try {
	            UserDAO userDAO = new UserDAO();
	 
	            if (userDAO.emailExists(email.trim())) {
	                forwardWithError(request, response, "An account with this email already exists.");
	                return;
	            }
	 
	            User user = new User();
	            user.setFullName(fullName.trim());
	            user.setEmail(email.trim());
	            user.setPassword(PasswordUtil.hashPassword(password));
	            // role defaults to DRIVER in the SQL INSERT inside registerUser()
	            userDAO.registerUser(user);
	 
	            response.sendRedirect(request.getContextPath() + "/Login?registered=true");
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	            forwardWithError(request, response, "Something went wrong. Please try again.");
	            }
	}

	 private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
             String message) throws ServletException, IOException {
		 	request.setAttribute("error", message);

		 	request.getRequestDispatcher("/WEB-INF/pages/Register.jsp").forward(request, response);
	 }
}