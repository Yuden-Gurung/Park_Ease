<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="com.parking.model.User" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"ADMIN".equals(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/Login");
        return;
    }
    String adminName = user.getFullName();
    String initial   = adminName.substring(0,1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>AdminDashboard</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-body">

<aside class="sidebar">
  <div class="logo"><div class="logo-p">P</div>ParkEase</div>
  <div class="nav-gl">Main Menu</div>
  <a class="nav-item active" onclick="show('overview')"><span class="ni">📊</span>Overview</a>
  <a class="nav-item" onclick="show('users')"><span class="ni">👥</span>Manage Users</a>
  <a class="nav-item" onclick="show('slots')"><span class="ni">🅿</span>Parking Slots</a>
  <a class="nav-item" onclick="show('bookings')"><span class="ni">📋</span>All Bookings</a>
  <a class="nav-item" onclick="show('revenue')"><span class="ni">💰</span>Revenue</a>
  <div class="sb-sp"></div>
  <div class="sb-foot">
    <div class="ub">
      <div class="avatar"><%= initial %></div>
      <div><div class="u-name"><%= adminName %></div><div class="u-role">Administrator</div></div>
    </div>
    <a href="${pageContext.request.contextPath}/Logout" class="logout-btn">⏻ Sign Out</a>
  </div>
</aside>

<main class="main">

  <!-- OVERVIEW -->
  <section id="overview" class="section-page">
    <div class="page-header">
      <div class="page-title">Admin <span>Dashboard</span></div>
      <div class="page-sub">Welcome back, <%= adminName %> — here's what's happening today.</div>
    </div>

    <%-- Global feedback toast (shown after any AdminAction redirect) --%>
    <c:choose>
      <c:when test="${param.msg == 'locked'}">
        <div class="toast error" style="display:block;margin-bottom:18px">🔒 User account has been locked.</div>
      </c:when>
      <c:when test="${param.msg == 'unlocked'}">
        <div class="toast success" style="display:block;margin-bottom:18px">🔓 User account has been unlocked.</div>
      </c:when>
      <c:when test="${param.msg == 'deleted'}">
        <div class="toast error" style="display:block;margin-bottom:18px">🗑️ User has been permanently deleted.</div>
      </c:when>
      <c:when test="${param.msg == 'released'}">
        <div class="toast success" style="display:block;margin-bottom:18px">✅ Slot has been force-released.</div>
      </c:when>
      <c:when test="${param.msg == 'cancelled'}">
        <div class="toast error" style="display:block;margin-bottom:18px">❌ Booking has been force-cancelled.</div>
      </c:when>
      <c:when test="${param.error == 'true'}">
        <div class="toast error" style="display:block;margin-bottom:18px">⚠️ An error occurred. Please try again.</div>
      </c:when>
    </c:choose>
    <div class="stats-grid">
      <div class="stat-card gold">
        <div class="stat-icon">👥</div>
        <div class="stat-value"><c:out value="${totalUsers}"/></div>
        <div class="stat-label">Total Users</div>
      </div>
      <div class="stat-card teal">
        <div class="stat-icon">🅿</div>
        <div class="stat-value"><c:out value="${freeSlots}"/></div>
        <div class="stat-label">Available Slots</div>
      </div>
      <div class="stat-card red">
        <div class="stat-icon">🚗</div>
        <div class="stat-value"><c:out value="${activeBookings}"/></div>
        <div class="stat-label">Active Bookings</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-icon">💰</div>
        <div class="stat-value">Rs.<c:out value="${totalRevenue}"/></div>
        <div class="stat-label">Total Revenue</div>
      </div>
    </div>

    <div class="two-col">
      <!-- Recent Bookings -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">Recent Bookings</div>
          <a class="card-badge" onclick="show('bookings')">View All</a>
        </div>
        <c:choose>
          <c:when test="${not empty recentBookings}">
            <table>
              <thead><tr><th>Driver</th><th>Slot</th><th>Status</th></tr></thead>
              <tbody>
                <c:forEach var="b" items="${recentBookings}">
                  <tr>
                    <td><c:out value="${b.driverName}"/></td>
                    <td><strong><c:out value="${b.slotNumber}"/></strong></td>
                    <td>
                      <c:choose>
                        <c:when test="${b.status == 'ACTIVE'}"><span class="badge badge-active">Active</span></c:when>
                        <c:when test="${b.status == 'COMPLETED'}"><span class="badge badge-completed">Completed</span></c:when>
                        <c:otherwise><span class="badge badge-cancelled">Cancelled</span></c:otherwise>
                      </c:choose>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:when>
          <c:otherwise>
            <div class="empty"><div class="empty-icon">📋</div>No bookings yet.</div>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- Slot Map -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">Slot Map</div>
          <div style="display:flex;gap:14px;font-size:0.76rem;">
            <span style="color:var(--teal)">● Free</span>
            <span style="color:var(--red)">● Booked</span>
          </div>
        </div>
        <div class="slot-grid">
          <c:forEach var="s" items="${allSlots}">
            <c:choose>
              <c:when test="${s.status == 'FREE'}">
                <div class="slot free"><c:out value="${s.slotNumber}"/></div>
              </c:when>
              <c:otherwise>
                <div class="slot booked"><c:out value="${s.slotNumber}"/></div>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </div>
      </div>
    </div>
  </section>

  <!-- USERS -->
  <section id="users" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">Manage <span>Users</span></div>
      <div class="page-sub">View, lock, or unlock driver accounts.</div>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">All Users</div></div>
      <div class="table-toolbar">
        <input class="search-input" type="text" placeholder="🔍  Search by name or email…" oninput="filterTable('users-tbody',this.value)">
      </div>
      <table>
        <thead><tr><th>#</th><th>Full Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody id="users-tbody">
          <c:choose>
            <c:when test="${not empty allUsers}">
              <c:forEach var="u" items="${allUsers}" varStatus="st">
                <tr>
                  <td>${st.index + 1}</td>
                  <td><strong><c:out value="${u.fullName}"/></strong></td>
                  <td style="color:var(--muted)"><c:out value="${u.email}"/></td>
                  <td>
                    <c:choose>
                      <c:when test="${u.role == 'ADMIN'}"><span class="badge badge-admin">Admin</span></c:when>
                      <c:otherwise><span class="badge badge-driver">Driver</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${u.locked}"><span class="badge badge-locked">Locked</span></c:when>
                      <c:otherwise><span class="badge badge-active">Active</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <div class="action-btns">
                      <c:choose>
                        <c:when test="${u.locked}">
                          <form action="${pageContext.request.contextPath}/AdminAction" method="post" style="display:inline">
                            <input type="hidden" name="action" value="unlock">
                            <input type="hidden" name="userId" value="${u.id}">
                            <button type="submit" class="btn-sm btn-unlock">Unlock</button>
                          </form>
                        </c:when>
                        <c:otherwise>
                          <form action="${pageContext.request.contextPath}/AdminAction" method="post" style="display:inline"
                                onsubmit="return confirm('Lock this account?')">
                            <input type="hidden" name="action" value="lock">
                            <input type="hidden" name="userId" value="${u.id}">
                            <button type="submit" class="btn-sm btn-lock">Lock</button>
                          </form>
                        </c:otherwise>
                      </c:choose>
                      <form action="${pageContext.request.contextPath}/AdminAction" method="post" style="display:inline"
                            onsubmit="return confirm('Delete this user permanently?')">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="userId" value="${u.id}">
                        <button type="submit" class="btn-sm btn-delete">Delete</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr><td colspan="6">
                <div class="empty"><div class="empty-icon">👥</div>No users found.</div>
              </td></tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </section>

  <!-- SLOTS -->
  <section id="slots" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">Parking <span>Slots</span></div>
      <div class="page-sub">Monitor and manage all parking spaces.</div>
    </div>
    <div class="card" style="margin-bottom:22px">
      <div class="card-header">
        <div class="card-title">Visual Slot Map</div>
        <div style="display:flex;gap:14px;font-size:0.76rem;">
          <span style="color:var(--teal)">● Free</span>
          <span style="color:var(--red)">● Booked</span>
        </div>
      </div>
      <div class="slot-grid">
        <c:forEach var="s" items="${allSlots}">
          <c:choose>
            <c:when test="${s.status == 'FREE'}">
              <div class="slot free"><c:out value="${s.slotNumber}"/></div>
            </c:when>
            <c:otherwise>
              <div class="slot booked"><c:out value="${s.slotNumber}"/></div>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </div>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">Slot Details</div></div>
      <table>
        <thead><tr><th>Slot</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty allSlots}">
              <c:forEach var="s" items="${allSlots}">
                <tr>
                  <td><strong><c:out value="${s.slotNumber}"/></strong></td>
                  <td>
                    <c:choose>
                      <c:when test="${s.status == 'FREE'}"><span class="badge badge-free">Free</span></c:when>
                      <c:otherwise><span class="badge badge-booked">Booked</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <div class="action-btns">
                      <c:if test="${s.status == 'BOOKED'}">
                        <form action="${pageContext.request.contextPath}/AdminAction" method="post" style="display:inline"
                              onsubmit="return confirm('Force-release this slot?')">
                          <input type="hidden" name="action" value="releaseSlot">
                          <input type="hidden" name="slotId" value="${s.id}">
                          <button type="submit" class="btn-sm btn-unlock">Release</button>
                        </form>
                      </c:if>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr><td colspan="3">
                <div class="empty"><div class="empty-icon">🅿</div>No slots found.</div>
              </td></tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </section>

  <!-- BOOKINGS -->
  <section id="bookings" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">All <span>Bookings</span></div>
      <div class="page-sub">Complete record of every parking booking.</div>
    </div>
    <div class="card">
      <div class="card-header"><div class="card-title">Booking Records</div></div>
      <div class="table-toolbar">
        <input class="search-input" type="text" placeholder="🔍  Search by driver or slot…" oninput="filterTable('bookings-tbody',this.value)">
      </div>
      <table>
        <thead><tr><th>#</th><th>Driver</th><th>Slot</th><th>Check-In</th><th>Check-Out</th><th>Amount</th><th>Status</th><th></th></tr></thead>
        <tbody id="bookings-tbody">
          <c:choose>
            <c:when test="${not empty allBookings}">
              <c:forEach var="b" items="${allBookings}" varStatus="st">
                <tr>
                  <td>${st.index + 1}</td>
                  <td><c:out value="${b.driverName}"/></td>
                  <td><strong><c:out value="${b.slotNumber}"/></strong></td>
                  <td><fmt:formatDate value="${b.checkIn}"  pattern="dd MMM yy, hh:mm a"/></td>
                  <td><fmt:formatDate value="${b.checkOut}" pattern="dd MMM yy, hh:mm a"/></td>
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
                      <form action="${pageContext.request.contextPath}/AdminAction" method="post" style="display:inline"
                            onsubmit="return confirm('Force-cancel this booking?')">
                        <input type="hidden" name="action" value="cancelBooking">
                        <input type="hidden" name="bookingId" value="${b.id}">
                        <input type="hidden" name="slotId" value="${b.slotId}">
                        <button type="submit" class="btn-sm btn-delete">Cancel</button>
                      </form>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr><td colspan="8">
                <div class="empty"><div class="empty-icon">📋</div>No bookings yet.</div>
              </td></tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </section>

  <!-- REVENUE -->
  <section id="revenue" class="section-page" style="display:none">
    <div class="page-header">
      <div class="page-title">Revenue <span>Overview</span></div>
      <div class="page-sub">Financial summary of parking operations.</div>
    </div>
    <div class="two-col">
      <div class="card">
        <div class="card-header"><div class="card-title">Monthly Breakdown</div></div>
        <div class="rev-bar-wrap">
          <c:forEach var="m" items="January,February,March,April,May,June,July,August,September,October,November,December">
            <div class="rev-bar-row">
              <div class="rev-bar-top">
                <span><c:out value="${m}"/></span>
                <span style="color:var(--muted)">—</span>
              </div>
              <div class="rev-bar"><div class="rev-bar-fill" style="width:0%"></div></div>
            </div>
          </c:forEach>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><div class="card-title">Summary</div></div>
        <div class="rev-row">
          <span class="rev-label">Total Collected</span>
          <span class="rev-value">Rs. <c:out value="${totalRevenue}"/></span>
        </div>
        <div class="rev-row">
          <span class="rev-label">Total Bookings</span>
          <span class="rev-value"><c:out value="${fn:length(allBookings)}"/></span>
        </div>
        <div class="rev-row">
          <span class="rev-label">Active Bookings</span>
          <span class="rev-value"><c:out value="${activeBookings}"/></span>
        </div>
        <div class="rev-row">
          <span class="rev-label">Available Slots</span>
          <span class="rev-value"><c:out value="${freeSlots}"/></span>
        </div>
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

  function filterTable(tbodyId, query) {
    var q = query.toLowerCase();
    document.querySelectorAll('#' + tbodyId + ' tr').forEach(function(row) {
      row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  }

  document.querySelectorAll('.nav-item').forEach(function(a) {
    a.addEventListener('click', function(e) { e.preventDefault(); });
  });

  // Auto-navigate to section from redirect (e.g. ?section=users after lock/delete)
  (function() {
    var params = new URLSearchParams(window.location.search);
    var sec = params.get('section');
    if (sec && document.getElementById(sec)) {
      show(sec);
    }
  })();
</script>
</body>
</html>
