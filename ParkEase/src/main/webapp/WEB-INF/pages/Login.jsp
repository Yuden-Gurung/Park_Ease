<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Log In</title>
<link href="https://fonts.googleapis.com/css2?family=Clash+Display:wght@500;600;700&family=Cabinet+Grotesk:wght@300;400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-body login-page">
<div class="bg-canvas">
  <div class="orb orb-1"></div>
  <div class="orb orb-2"></div>
  <div class="orb orb-3"></div>
</div>
<div class="bg-grid"></div>

<div class="layout login-layout">
  <div class="brand-panel">
    <div class="logo"><div class="logo-p">P</div>ParkEase</div>
    <div class="brand-body">
      <div class="brand-headline">Smart parking<br>for <em>everyone.</em></div>
      <div class="brand-sub">Reserve your spot in seconds. Real-time availability, instant confirmation.</div>
      <div class="feature-list">
        <div class="feature-item"><div class="feature-dot">🅿</div>Real-time slot availability</div>
        <div class="feature-item"><div class="feature-dot">⚡</div>Instant booking confirmation</div>
        <div class="feature-item"><div class="feature-dot">🔒</div>Secure account management</div>
      </div>
    </div>
    <div class="brand-footer">© 2025 ParkEase. All rights reserved.</div>
  </div>

  <div class="form-panel">
    <div class="form-eyebrow">Welcome Back</div>
    <div class="form-title">Sign in to<br>your account</div>
    <div class="form-sub">Enter your credentials to continue</div>

    <c:if test="${param.registered == 'true'}">
      <div class="alert alert-success">✓ Account created successfully! You can now sign in.</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-error">⚠ <c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/Login" method="post">
      <div class="field">
        <label>Email Address</label>
        <input type="email" name="email" required placeholder="you@example.com" autocomplete="email">
      </div>
      <div class="field">
        <label>Password</label>
        <input type="password" name="password" required placeholder="••••••••" autocomplete="current-password">
      </div>
      <button type="submit" class="btn-submit">Sign In →</button>
    </form>
    <div class="switch-link">No account? <a href="${pageContext.request.contextPath}/Register">Create one free</a></div>
  </div>
</div>
</body>
</html>
