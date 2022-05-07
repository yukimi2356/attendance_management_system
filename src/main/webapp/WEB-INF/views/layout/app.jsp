<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="constants.ForwardConst" %>
<%@ page import="constants.AttributeConst" %>

<c:set var="actTop" value="${ForwardConst.ACT_TOP.getValue()}" />
<c:set var="actEmp" value="${ForwardConst.ACT_EMP.getValue()}" />
<c:set var="actAtd" value="${ForwardConst.ACT_ATD.getValue()}" />
<c:set var="actAuth" value="${ForwardConst.ACT_AUTH.getValue()}" />

<c:set var="commIdx" value="${ForwardConst.CMD_INDEX.getValue()}" />
<c:set var="commOut" value="${ForwardConst.CMD_LOGOUT.getValue()}" />
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8">
        <title><c:out value="日報管理システム" /></title>
        <link rel="stylesheet" href="<c:url value='/css/reset.css' />">
        <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    </head>
    <style>
        @charset "UTF-8";

body {
    color: #333333;
    font-family: "Hiragino Kaku Gothic Pro",Meiryo,"MS PGothic",Helvetica,Arial,sans-serif;
}

#header {
    width: 100%;
    height: 70px;
    background-color: #333333;
}

#content {
    width: 94%;
    margin-top: 15px;
    padding-left: 3%;
}

h1 {
    font-size: 24px;
    display: inline;
}

h2 {
    font-size: 36px;
    margin-bottom: 15px;
}

li {
    margin-top: 10px;
    margin-bottom: 10px;
}

p {
    margin-top: 15px;
    margin-bottom: 15px;
}

a {
    text-decoration: none;
    color: #24738e;
}

table, tr, th, td {
    border: 1px solid #cccccc;
}

table {
    width: 100%;
    table-layout: fixed;
}

th {
    width: 26%;
    padding: 10px 2%;
}

td {
    width: 66%;
    padding: 10px 2%;
}

button {
    font-size: 14px;
    padding: 5px 10px;
}

#footer {
    text-align: center;
}

#flush_success {
    width: 100%;
    padding-top: 28px;
    padding-left: 2%;
    padding-bottom: 28px;
    margin-bottom: 15px;
    color: #155724;
    background-color: #d4edda;

}

#flush_error {
    width: 100%;
    padding-top: 28px;
    padding-left: 2%;
    padding-bottom: 15px;
    color: #721c24;
    background-color: #f8d7da;
}

table#employee_list th {
    width: 30%;
    padding: 10px 2%;
}

table#employee_list td {
    width: 29%;
    padding: 10px 2%;
}

tr.row1 {
        background-color: #f2f2f2;
}
tr.row0 {
        background-color: #ffffff;
}

select {
    height: 30px;
}

#header_menu {
    width: 57%;
    padding-top: 17px;
    padding-left: 3%;
    display: inline-block;
}

#employee_name {
    color: #cccccc;
    width: 36%;
    padding-right: 3%;
    text-align: right;
    display: inline-block;
}

#header a {
    color: #eeeeee;
}

#dakoku {
 height: 200px;
 display: flex;
 justify-content:center;
}

#syukkin{
  width: 34%;
}

#taikin{
  width: 33%;
}

#yasumi{
  width: 33%;
}


h1 a {
    color: #eeeeee;
}

table#attendance_list th {
    font-weight: bold;
    font-size: 16px;
    padding: 6px;
}
table#attendance_list td {
    font-size: 16px;
    padding: 6px;
}

table#attendance_list .attendance_name {
    width: 20%;
    padding: 10px 2%;
}
table#attendance_list .attendance_date {
    width: 20%;
}

table#attendance_list .attendance_title {
    width: 37%;
}

table#attendance_list .attendance_action {
    width: 13%;
}

pre {
    font-family: "Hiragino Kaku Gothic Pro",Meiryo,"MS PGothic",Helvetica,Arial,sans-serif;
}

h3 {
    font-size: larger;
}



    </style>
<body>
    <div id="wrapper">
        <div id="header">
            <div id="header_menu">
                <h1><a href="<c:url value='/?action=${actTop}&command=${commIdx}' />">勤怠管理システム</a></h1>&nbsp;&nbsp;&nbsp;
                <c:if test="${sessionScope.login_employee != null}">
                    <c:if test="${sessionScope.login_employee.adminFlag == AttributeConst.ROLE_ADMIN.getIntegerValue()}">
                        <a href="<c:url value='?action=${actEmp}&command=${commIdx}' />">従業員管理</a>&nbsp;
                    </c:if>
                    <a href="<c:url value='?action=${actAtd}&command=${commIdx}' />">勤怠管理</a>&nbsp;
                </c:if>
            </div>
            <c:if test="${sessionScope.login_employee != null}">
                <div id="employee_name">
                    <c:out value="${sessionScope.login_employee.name}" />
                    &nbsp;さん&nbsp;&nbsp;&nbsp;
                    <a href="<c:url value='?action=${actAuth}&command=${commOut}' />">ログアウト</a>
                </div>
            </c:if>
        </div>
        <div id="content">${param.content}</div>
        <div id="footer">by YAMADA Yukimi.</div>
    </div>
</body>
</html>









