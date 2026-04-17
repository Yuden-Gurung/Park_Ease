<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Register</title>
<link href="https://fonts.googleapis.com/css2?family=Clash+Display:wght@500;600;700&family=Cabinet+Grotesk:wght@300;400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-body register-page">
<div class="bg-canvas">
  <div class="orb orb-1"></div>
  <div class="orb orb-2"></div>
  <div class="orb orb-3"></div>
</div>
<div class="bg-grid"></div>

<div class="layout register-layout">
  <div class="brand-panel">
    <div class="logo"><div class="logo-p">P</div>ParkEase</div>
    <div class="brand-body">
      <div class="brand-headline">Join the<br>smarter <em>way</em><br>to park.</div>
      <div class="brand-sub">Create your free account in under a minute.</div>
      <div class="step-list">
        <div class="step-item">
          <div class="step-num">1</div>
          <div class="step-text"><strong>Create account</strong>Fill in your details below</div>
        </div>
        <div class="step-item">
          <div class="step-num">2</div>
          <div class="step-text"><strong>Browse slots</strong>See real-time availability</div>
        </div>
        <div class="step-item">
          <div class="step-num">3</div>
          <div class="step-text"><strong>Book & park</strong>Confirm in seconds</div>
        </div>
      </div>
    </div>
    <div class="brand-footer">© 2025 ParkEase. All rights reserved.</div>
  </div>

  <div class="form-panel">
    <div class="form-eyebrow">Get Started</div>
    <div class="form-title">Create your<br>account</div>
    <div class="form-sub">Free forever for drivers</div>

    <c:if test="${not empty error}">
      <div class="alert alert-error">⚠ <c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/Register" method="post" onsubmit="return validateForm()">
      <div class="fields-grid">
        <div class="field full">
          <label>Full Name</label>
          <input type="text" name="fullName" id="fullName" required placeholder="John Doe" autocomplete="name">
        </div>
        <div class="field full">
          <label>Email Address</label>
          <input type="email" name="email" id="email" required placeholder="you@example.com" autocomplete="email">
        </div>
        <div class="field">
          <label>Password</label>
          <input type="password" name="password" id="password" required placeholder="Min. 6 characters" oninput="checkStrength(this.value)">
          <div class="strength-bar">
            <div class="strength-seg" id="s1"></div>
            <div class="strength-seg" id="s2"></div>
            <div class="strength-seg" id="s3"></div>
            <div class="strength-seg" id="s4"></div>
          </div>
          <div class="strength-label" id="strength-label"></div>
        </div>
        <div class="field">
          <label>Confirm Password</label>
          <input type="password" name="confirmPassword" id="confirmPassword" required placeholder="Repeat password">
        </div>
      </div>
      <button type="submit" class="btn-submit">Create Account →</button>
    </form>
    <div class="switch-link">Already have an account? <a href="${pageContext.request.contextPath}/Login">Sign in</a></div>
  </div>
</div>

<script>
  function checkStrength(val) {
    const segs = [document.getElementById('s1'),document.getElementById('s2'),document.getElementById('s3'),document.getElementById('s4')];
    const lbl  = document.getElementById('strength-label');
    const colors = ['#f87171','#f59e0b','#e8b84b','#2dd4bf'];
    const labels = ['Weak','Fair','Good','Strong'];
    let score = 0;
    if (val.length >= 6) score++;
    if (val.length >= 10) score++;
    if (/[A-Z]/.test(val) && /[0-9]/.test(val)) score++;
    if (/[^a-zA-Z0-9]/.test(val)) score++;
    segs.forEach((s,i) => s.style.background = i < score ? colors[score-1] : 'rgba(255,255,255,0.1)');
    lbl.textContent = val.length ? labels[score-1] || '' : '';
    lbl.style.color = score > 0 ? colors[score-1] : 'var(--muted)';
  }

  function validateForm() {
    const pw  = document.getElementById('password').value;
    const cpw = document.getElementById('confirmPassword').value;
    if (pw !== cpw) {
      document.getElementById('confirmPassword').classList.add('error');
      alert('Passwords do not match.');
      return false;
    }
    return true;
  }
</script>
</body>
</html>
