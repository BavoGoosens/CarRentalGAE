<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="ds.gae.view.JSPSite"%>
<%@page import="ds.gae.view.ViewTools"%>
<%@page import="ds.gae.CarRentalModel"%>
<%@page import="ds.gae.entities.Quote"%>
<%@page import="ds.gae.entities.Log"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% 
	JSPSite currentSite = JSPSite.LOG;
	String renter = (String)session.getAttribute("renter");
	HashMap<String, List<Quote>> quotes = (HashMap<String, List<Quote>>)session.getAttribute("quotes"); 
	boolean anyQuotes = false;
%>   
 
<%@include file="_header.jsp" %>

<% 
if (currentSite != JSPSite.LOGIN && currentSite != JSPSite.PERSIST_TEST && renter == null) {
 %>
	<meta http-equiv="refresh" content="0;URL='/login.jsp'">
<% 
  request.getSession().setAttribute("lastSiteCall", currentSite);
} 
 %>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="style.css" />
	<title>Car Rental Application</title>
</head>
<body>
	<div id="mainWrapper">
		<div id="headerWrapper">
			<h1>Car Rental Application</h1>
		</div>
		<div id="navigationWrapper">
			<ul>
<% 
for (JSPSite site : JSPSite.publiclyLinkedValues()) {
	if (site == currentSite) {
 %> 
				<li><a class="selected" href="<%=site.url()%>"><%=site.label()%></a></li>
<% } else {
 %> 
				<li><a href="<%=site.url()%>"><%=site.label()%></a></li>
<% }}
 %> 

				</ul>
		</div>
		<div id="contentWrapper">
<% if (currentSite != JSPSite.LOGIN) { %>
			<div id="userProfile">
				<span>Logged-in as <%= renter %> (<a href="/login.jsp">change</a>)</span>
			</div>
<%
   }
 %>
		<div class="frameDiv">
		<h2>Log for user: <%= renter %></h2>
			
			<h3>Log</h3>
			
			<div class="group">
 			<table>
 				<tr>
 					<th>Datetime</th>
 					<th>Description</th>
 				</tr>
 			
<% 
for (Log log : CarRentalModel.get().getLogForUser(renter)) {
 %> <!-- begin of CRC loop -->
 
 				<tr>
 					<td><%= log.getDate().toString() %></td>
 					<td><%= log.getDescription() %>
 				</tr>		
 
 <% } %>
 
	 		</table>
	 		</div>
 			
			
			
		</div>

		</div>
		</div>

 
<%@include file="_footer.jsp" %>
