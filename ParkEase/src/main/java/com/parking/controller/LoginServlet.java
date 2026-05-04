package com.parking.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import com.parking.dao.UserDAO;
import com.parking.model.User;
import com.parking.util.PasswordUtil;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/Login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
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
	            redirectByRole((User) session.getAttribute("user"), request, response);
	            return;
	        }
	        String rememberedEmail = "";
	        Cookie[] cookies = request.getCookies();
	        if (cookies != null) {
	            for (Cookie c : cookies) {
	                if ("rememberedEmail".equals(c.getName())) {
	                    rememberedEmail = c.getValue();
	                    break;
	                }
	            }
	        }
	        request.setAttribute("rememberedEmail", rememberedEmail);
	        request.getRequestDispatcher("/WEB-INF/pages/Login.jsp").forward(request, response);
	        // FIX: leading slash so it works regardless of servlet path depth
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String email    = request.getParameter("email");
        String password = request.getParameter("password");
 
        // Basic null/empty guard before hitting the DB
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            forwardWithError(request, response, "Email and password are required.");
            return;
        }
 
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findByEmail(email.trim());
 
            if (user == null) {
                forwardWithError(request, response, "Invalid email or password.");
                return;
            }
 
            if (user.isLocked()) {
                forwardWithError(request, response, "Account is locked. Please contact admin.");
                return;
            }
 
            if (PasswordUtil.hashPassword(password).equals(user.getPassword())) {
                // ── Successful login ──
                userDAO.resetFailedAttempts(user.getId());
                String rememberMe = request.getParameter("rememberMe");
                if ("on".equals(rememberMe)) {
                    Cookie rememberCookie = new Cookie("rememberedEmail", email.trim());
                    rememberCookie.setMaxAge(30 * 24 * 60 * 60);
                    rememberCookie.setHttpOnly(true);
                    rememberCookie.setSecure(true);
                    rememberCookie.setPath(request.getContextPath() + "/");
                    response.addCookie(rememberCookie);
                }
                HttpSession session = request.getSession();   // creates new session
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60);      // 30 minutes
 
                redirectByRole(user, request, response);
 
            } else {
                // ── Failed attempt ──
                int attempts = user.getFailedAttempts() + 1;
                // FIX: updateFailedAttempts now also sets is_locked when attempts >= 5
                userDAO.updateFailedAttempts(user.getId(), attempts);
 
                if (attempts >= 5) {
                    forwardWithError(request, response,
                        "Account locked after 5 failed attempts. Please contact admin.");
                } else {
                    forwardWithError(request, response,
                        "Invalid email or password. " + (5 - attempts) + " attempt(s) remaining.");
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            forwardWithError(request, response, "Something went wrong. Please try again.");
        }
        String rememberMe = request.getParameter("rememberMe");
        if ("on".equals(rememberMe)) {
            Cookie rememberCookie = new Cookie("rememberedEmail", email.trim());
            rememberCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            rememberCookie.setHttpOnly(true);   // not accessible via JS
            rememberCookie.setSecure(true);     // HTTPS only
            rememberCookie.setPath(request.getContextPath() + "/");
            response.addCookie(rememberCookie);
        }
	}
	 private void redirectByRole(User user, HttpServletRequest request, HttpServletResponse response)
	            throws IOException {
	        if ("ADMIN".equals(user.getRole())) {
	            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
	        } else {
	            response.sendRedirect(request.getContextPath() + "/DriverDashboard");
	        }
	    }
	 
	    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
	    	request.setAttribute("error", message);
	    	request.getRequestDispatcher("/WEB-INF/pages/Login.jsp").forward(request, response);
	    }
}