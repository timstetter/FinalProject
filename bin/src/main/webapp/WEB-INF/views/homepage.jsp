<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
<link rel="stylesheet" href="/css/MainStyles.css">
</head>
<body>
<img src="http://j.b5z.net/i/u/2017580/i/Events1.png" style="width:200px;height:75px;">
<a href="/index"> <button type="submit">Go Home</button> </a> 
<a id ="home" href="/">Return To Search</a>
<br />
<div class="row">
<c:forEach var="business" items="${businesses}">
  <div class="column">
<img src="${business.image_url}"><br />
<a href="${business.url}">${business.name}</a>
  </div>
  </c:forEach>
</div>
 <br />
  <form action="/random" method="post">
   <input type="submit" value="Random Pick">
  </form>
  <br />
  <c:if test="${not empty businesschoice}">
  <div>
<img src="${businesschoice.image_url}"><br />
<a href="${businesschoice.url}">${businesschoice.name}</a>
  </div>
  </c:if>
</body>
<br />
</html>