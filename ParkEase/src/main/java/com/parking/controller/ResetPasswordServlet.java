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
 * Servlet implementation class ResetPasswordServlet
 */
@WebServlet("/ResetPassword")
public class ResetPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResetPasswordServlet() {
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
		// 1. Session guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
 
        User sessionUser = (User) session.getAttribute("user");
 
        // 2. Read form fields
        String currentPassword  = request.getParameter("currentPassword");
        String newPassword      = request.getParameter("newPassword");
        String confirmPassword  = request.getParameter("confirmPassword");
 
        // 3. Basic null/empty validation
        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            forwardWithError(request, response, "All password fields are required.");
            return;
        }
 
        // 4. New password length
        if (newPassword.length() < 6) {
            forwardWithError(request, response, "New password must be at least 6 characters.");
            return;
        }
 
        // 5. Confirm match
        if (!newPassword.equals(confirmPassword)) {
            forwardWithError(request, response, "New passwords do not match.");
            return;
        }
 
        try {
            UserDAO userDAO = new UserDAO();
            User dbUser = userDAO.findById(sessionUser.getId());
 
            if (dbUser == null) {
                forwardWithError(request, response, "User not found. Please log in again.");
                return;
            }
 
            // 6. Verify current password
            String hashedCurrent = PasswordUtil.hashPassword(currentPassword);
            if (!hashedCurrent.equals(dbUser.getPassword())) {
                forwardWithError(request, response, "Current password is incorrect.");
                return;
            }
 
            // 7. Prevent reuse of the same password
            String hashedNew = PasswordUtil.hashPassword(newPassword);
            if (hashedNew.equals(dbUser.getPassword())) {
                forwardWithError(request, response, "New password must be different from current password.");
                return;
            }
 
            // 8. Persist the new password
            userDAO.updatePassword(dbUser.getId(), hashedNew);
 
            // 9. Refresh session user object
            dbUser.setPassword(hashedNew);
            session.setAttribute("user", dbUser);
 
            // 10. Redirect with success flag
            response.sendRedirect(request.getContextPath() + "/DriverDashboard?passwordChanged=true");
 
        } catch (Exception e) {
            e.printStackTrace();
            forwardWithError(request, response, "Something went wrong. Please try again.");
        }
    }
 
    // ── Helpers ─────────────────────────────────────────────────────────────
 
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
 
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("passwordError", message);
        // Re-load dashboard data so the JSP renders correctly
        request.getRequestDispatcher("/WEB-INF/pages/DriverDashboard.jsp").forward(request, response);
    }
}

