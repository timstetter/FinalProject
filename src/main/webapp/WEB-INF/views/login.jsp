<%@ page language="java" contentType="text/html; UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!-- user log in page. asks user for email and password -->
<html>
<head>
<meta charset="UTF-8">
<title>Events | Login</title>
</head>
<body>
	<img src="http://j.b5z.net/i/u/2017580/i/Events1.png"
		style="width: 200px; height: 75px;">
	<h1>Events</h1>
	<p>${message}</p>
	<form method="post" action="/login">
		<label for="email">Email</label> <input type="text" name="email" required><br />
		<label for="password">Password</label> <input type="password" 
			name="password" required><br /> <input type="submit" value="Log In"><br />
	</form>
	<a href="/register">SignUp</a>
</body>
</html>