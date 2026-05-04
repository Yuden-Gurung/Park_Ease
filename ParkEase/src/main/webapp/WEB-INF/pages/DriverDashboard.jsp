<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="com.parking.model.User" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"DRIVER".equals(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/Login");
        return;
    }
    String driverName  = user.getFullName();
    String driverEmail = user.getEmail();
    String initial     = driverName.substring(0,1).toUpperCase();
    String firstName   = driverName.split(" ")[0];
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Driver Dashboard</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="driver-body">

<aside class="sidebar">
  <div class="logo"><div class="logo-p">P</div>ParkEase</div>
  <div class="nav-gl">Menu</div>
  <a class="nav-item active" onclick="show('home')"><span class="ni">🏠</span>Home</a>
  <a class="nav-item" onclick="show('slots')"><span class="ni">🅿</span>Available Slots</a>
  <a class="nav-item" onclick="show('book')"><span class="ni">➕</span>Book a Slot</a>
  <a class="nav-item" onclick="show('mybookings')"><span class="ni">📋</span>My Bookings</a>
  <a class="nav-item" onclick="show('profile')"><span class="ni">👤</span>My Profile</a>
  <div class="sb-sp"></div>
  <div class="sb-foot">
    <div class="ub">
      <div class="avatar"><%= initial %></div>
      <div>
        <div class="u-name"><%= driverName %></div>
        <div class="u-email"><%= driverEmail %></div>
      </div>
    </div>
    <a href="${pageContext.request.contextPath}/Logout" class="logout-btn">⏻ Sign Out</a>
  </div>
</aside>

<main class="main">

  <!-- HOME -->
  <section id="home" class="section-page">
    <div class="page-header">
      <div class="page-title">Hello, <span><%= firstName %></span> 👋</div>
      <div class="page-sub">Here's a quick look at your parking activity today.</div>
    </div>
    <div class="stats-grid">
      <div class="stat-card blue">
        <div class="stat-icon si-b">🅿</div>
        <div><div class="stat-value"><c:out value="${freeCount}"/></div><div class="stat-label">Slots Available</div></div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon si-g">📋</div>
        <div><div class="stat-value"><c:out value="${totalCount}"/></div><div class="stat-label">My Total Bookings</div></div>
      </div>
      <div class="stat-card amber">
        <div class="stat-icon si-a">🚗</div>
        <div><div class="stat-value"><c:out value="${activeCount}"/></div><div class="stat-label">Active Now</div></div>
      </div>
    </div>

    <c:if test="${not empty bookingSuccess}">
      <div class="toast success" style="display:block;margin-bottom:18px"><c:out value="${bookingSuccess}"/></div>
    </c:if>
    <c:if test="${not empty bookingError}">
      <div class="toast error" style="display:block;margin-bottom:18px"><c:out value="${bookingError}"/></div>
    </c:if>

    <div class="card">
      <div class="card-header">
        <div class="card-title">Parking Map</div>
        <a class="card-badge" onclick="show('book')">+ Book Now</a>
      </div>
      <div class="slot-grid">
        <c:forEach var="s" items="${allSlots}">
          <c:choose>
            <c:when test="${s.status == 'FREE'}">
              <div class="slot free"><span class="si">🟢</span><c:out value="${s.slotNumber}"/></div>
            </c:when>
            <c:otherwise>
              <div class="slot booked"><span class="si">🔴</span><c:out value="${s.slotNumber}"/></div>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </div>
      <div class="slot-legend">
        <span><span class="dot dot-g"></span>Available</span>
        <span><span class="dot dot-r"></span>Occupied</span>
        <span><span class="dot dot-b"></span>Selected</span>
      </div>
    </div>
  </section>

  <!-- SLOTS -->
  <section id="slots" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">Available <span>Slots</span></div>
      <div class="page-sub">Click a green slot to pre-select it for booking.</div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">Parking Map</div>
        <span id="sel-label" style="font-size:0.84rem;color:var(--driver-muted)">No slot selected</span>
      </div>
      <div class="slot-grid" id="main-slots">
        <c:forEach var="s" items="${allSlots}">
          <c:choose>
            <c:when test="${s.status == 'FREE'}">
              <div class="slot free" data-slot="${s.slotNumber}"
                   onclick="selectSlot('${s.slotNumber}',this)">
                <span class="si">🟢</span><c:out value="${s.slotNumber}"/>
              </div>
            </c:when>
            <c:otherwise>
              <div class="slot booked">
                <span class="si">🔴</span><c:out value="${s.slotNumber}"/>
              </div>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </div>
      <div class="slot-legend">
        <span><span class="dot dot-g"></span>Available</span>
        <span><span class="dot dot-r"></span>Occupied</span>
        <span><span class="dot dot-b"></span>Selected</span>
      </div>
    </div>
  </section>

  <!-- BOOK -->
  <section id="book" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">Book a <span>Slot</span></div>
      <div class="page-sub">Reserve your parking space in seconds.</div>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">Booking Form</div></div>
      <div class="toast" id="toast"></div>
      <form action="${pageContext.request.contextPath}/Booking" method="post" onsubmit="return validateBooking()">
        <div class="book-form">
          <div class="fg">
            <label>Select Slot</label>
            <select id="slot-select" name="slotNumber" required>
              <option value="">— Choose a slot —</option>
              <c:forEach var="s" items="${allSlots}">
                <c:if test="${s.status == 'FREE'}">
                  <option value="${s.slotNumber}"><c:out value="${s.slotNumber}"/> – Available</option>
                </c:if>
              </c:forEach>
            </select>
          </div>
          <div class="fg"><label>Check-In</label><input type="datetime-local" id="checkin" name="checkIn" required></div>
          <div class="fg"><label>Check-Out</label><input type="datetime-local" id="checkout" name="checkOut" required></div>
        </div>
        <div class="book-actions">
          <button type="submit" class="btn-primary">Confirm Booking</button>
          <span class="btn-hint">Slot held for 15 min after booking.</span>
        </div>
      </form>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">Pricing</div></div>
      <div class="pricing-grid">
        <div class="price-card"><div class="price-icon">🕐</div><div class="price-val">Rs. 20/hr</div><div class="price-lbl">Standard Rate</div></div>
        <div class="price-card"><div class="price-icon">🌙</div><div class="price-val">Rs. 10/hr</div><div class="price-lbl">Night (10pm–6am)</div></div>
        <div class="price-card"><div class="price-icon">📅</div><div class="price-val">Rs. 150/day</div><div class="price-lbl">Full Day Rate</div></div>
      </div>
    </div>
  </section>

  <!-- MY BOOKINGS -->
  <section id="mybookings" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">My <span>Bookings</span></div>
      <div class="page-sub">All your past and current parking reservations.</div>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">Booking History</div></div>
      <table>
        <thead>
          <tr><th>#</th><th>Slot</th><th>Check-In</th><th>Check-Out</th><th>Duration</th><th>Amount</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty myBookings}">
              <tr><td colspan="8">
                <div class="empty">
                  <div class="empty-icon">📋</div>
                  <div class="empty-text">No bookings yet. <a onclick="show('book')">Book your first slot →</a></div>
                </div>
              </td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="b" items="${myBookings}" varStatus="st">
                <tr>
                  <td>${st.index + 1}</td>
                  <td><strong><c:out value="${b.slotNumber}"/></strong></td>
                  <td><fmt:formatDate value="${b.checkIn}"  pattern="dd MMM yyyy, hh:mm a"/></td>
                  <td><fmt:formatDate value="${b.checkOut}" pattern="dd MMM yyyy, hh:mm a"/></td>
                  <td><c:out value="${b.durationHours}"/> hr(s)</td>
                  <td>Rs. <c:out value="${b.amount}"/></td>
                  <td>
                    <c:choose>
                      <c:when test="${b.status == 'ACTIVE'}"><span class="badge badge-active">Active</span></c:when>
                      <c:when test="${b.status == 'COMPLETED'}"><span class="badge badge-completed">Completed</span></c:when>
                      <c:otherwise><span class="badge badge-cancelled">Cancelled</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
					<c:if test="${b.status == 'ACTIVE'}">
					  <%-- Check Out button --%>
					  <form action="${pageContext.request.contextPath}/CancelBooking" method="post"
					        onsubmit="return confirm('Check out from this slot?')" style="display:inline">
					    <input type="hidden" name="bookingId" value="${b.id}">
					    <input type="hidden" name="slotId"    value="${b.slotId}">
					    <input type="hidden" name="action"    value="checkout">
					    <button type="submit" class="btn-primary" style="padding:6px 12px;font-size:0.8rem">Check Out</button>
					  </form>
					
					  <%-- Cancel button --%>
					  <form action="${pageContext.request.contextPath}/CancelBooking" method="post"
					        onsubmit="return confirm('Cancel this booking?')" style="display:inline">
					    <input type="hidden" name="bookingId" value="${b.id}">
					    <input type="hidden" name="slotId"    value="${b.slotId}">
					    <input type="hidden" name="action"    value="cancel">
					    <button type="submit" class="cancel-btn">Cancel</button>
					  </form>
					</c:if>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </section>

  <!-- PROFILE -->
  <section id="profile" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">My <span>Profile</span></div>
      <div class="page-sub">Manage your account information and security.</div>
    </div>

    <!-- Account Info Card (read-only) -->
    <div class="card" style="margin-bottom:22px">
      <div class="profile-wrap">
        <div class="profile-top">
          <div class="avatar-lg"><%= initial %></div>
          <div>
            <div class="p-name"><%= driverName %></div>
            <div class="p-email"><%= driverEmail %></div>
            <div class="p-role">Driver</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Card -->
    <div class="card">
      <div class="card-header"><div class="card-title">🔒 Change Password</div></div>
      <div class="profile-wrap">

        <%-- Success banner (redirect param) --%>
        <c:if test="${param.passwordChanged == 'true'}">
          <div class="toast success" style="display:block;margin:0 0 18px">
            ✅ Password changed successfully!
          </div>
        </c:if>

        <%-- Error from servlet forward --%>
        <c:if test="${not empty passwordError}">
          <div class="toast error" style="display:block;margin:0 0 18px">
            <c:out value="${passwordError}"/>
          </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/ResetPassword" method="post"
              onsubmit="return validateResetPassword()">
          <div class="profile-grid">
            <div class="pf">
              <label>Current Password</label>
              <input type="password" id="cur-pw" name="currentPassword"
                     placeholder="Enter your current password" required>
            </div>
            <div class="pf">
              <label>New Password</label>
              <input type="password" id="new-pw" name="newPassword"
                     placeholder="At least 6 characters" required>
            </div>
            <div class="pf">
              <label>Confirm New Password</label>
              <input type="password" id="con-pw" name="confirmPassword"
                     placeholder="Repeat new password" required>
            </div>
          </div>
          <div class="profile-actions">
            <button type="submit" class="btn-primary">Update Password</button>
          </div>
        </form>
      </div>
    </div>
  </section>

</main>

<script>
  function show(id) {
    document.querySelectorAll('.section-page').forEach(function(s) {
      s.style.display = 'none';
    });
    document.getElementById(id).style.display = 'block';
    document.querySelectorAll('.nav-item').forEach(function(n) {
      var onclick = n.getAttribute('onclick') || '';
      n.classList.toggle('active', onclick.indexOf("'" + id + "'") !== -1);
    });
  }

  document.querySelectorAll('.nav-item').forEach(function(a) {
    a.addEventListener('click', function(e) { e.preventDefault(); });
  });

  var selectedSlot = null;
  function selectSlot(id, el) {
    document.querySelectorAll('#main-slots .slot.selected').forEach(function(s) {
      s.classList.remove('selected');
      s.classList.add('free');
    });
    if (selectedSlot === id) {
      selectedSlot = null;
      document.getElementById('sel-label').textContent = 'No slot selected';
      var sel = document.getElementById('slot-select');
      if (sel) sel.value = '';
    } else {
      selectedSlot = id;
      el.classList.remove('free');
      el.classList.add('selected');
      document.getElementById('sel-label').textContent = 'Selected: ' + id;
      var sel = document.getElementById('slot-select');
      if (sel) sel.value = id;
    }
  }

  (function() {
    var pad = function(n) { return String(n).padStart(2, '0'); };
    var fmt = function(d) {
      return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate()) +
             'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
    };
    var now = new Date(), later = new Date(now.getTime() + 7200000);
    var ci = document.getElementById('checkin');
    var co = document.getElementById('checkout');
    if (ci) ci.value = fmt(now);
    if (co) co.value = fmt(later);
  })();

  function validateBooking() {
    var s  = document.getElementById('slot-select').value;
    var ci = document.getElementById('checkin').value;
    var co = document.getElementById('checkout').value;
    if (!s)  { showToast('Please select a slot.', 'error'); return false; }
    if (!ci || !co) { showToast('Please fill in both times.', 'error'); return false; }
    if (new Date(co) <= new Date(ci)) { showToast('Check-out must be after check-in.', 'error'); return false; }
    return true;
  }

  function validateResetPassword() {
    var cur = document.getElementById('cur-pw').value.trim();
    var np  = document.getElementById('new-pw').value;
    var cp  = document.getElementById('con-pw').value;
    if (!cur) { alert('Please enter your current password.'); return false; }
    if (np.length < 6) { alert('New password must be at least 6 characters.'); return false; }
    if (np !== cp) { alert('New passwords do not match.'); return false; }
    return true;
  }

  function showToast(msg, type) {
    var t = document.getElementById('toast');
    t.textContent = msg;
    t.className = 'toast ' + type;
    t.style.display = 'block';
    setTimeout(function() { t.style.display = 'none'; }, 4000);
  }
</script>
</body>
</html>
