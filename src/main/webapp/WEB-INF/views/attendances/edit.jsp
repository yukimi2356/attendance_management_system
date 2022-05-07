<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="constants.AttributeConst" %>
<%@ page import="constants.ForwardConst" %>

<c:set var="action" value="${ForwardConst.ACT_ATD.getValue()}" />
<c:set var="commIdx" value="${ForwardConst.CMD_INDEX.getValue()}" />
<c:set var="commUpd" value="${ForwardConst.CMD_UPDATE.getValue()}" />

<c:import url="/WEB-INF/views/layout/app.jsp">
    <c:param name="content">

        <h2>${attendance.date} の勤怠データ編集ページ</h2>
        <form method="POST"
            action="<c:url value='?action=${action}&command=${commUpd}' />">
            <label for="${AttributeConst.ATD_DATE.getValue()}">日付</label><br />
            <input type="text" name="${AttributeConst.ATD_DATE.getValue()}" value="${attendance.date}" />
            <br /><br />

            <label for="${AttributeConst.STATUS.getValue()}">勤務</label><br />
            <input type="text" name="${AttributeConst.STATUS.getValue()}" value="${attendance.status}" />
            <br /><br />

            <label for="${AttributeConst.ATTENDED_AT.getValue()}">出勤時刻</label><br />
            <input type="text" name="${AttributeConst.ATTENDED_AT.getValue()}" value="${attendance.attendedAt}" />
            <br /><br />

            <label for="${AttributeConst.LEAVED_AT.getValue()}">退勤時刻</label><br />
            <input type="text" name="${AttributeConst.LEAVED_AT.getValue()}" value="${attendance.leavedAt}" />
            <br /><br />

            <label for="${AttributeConst.ACTUAL_HOURS.getValue()}">定内勤務時間</label><br />
            <input type="text" name="${AttributeConst.ACTUAL_HOURS.getValue()}" value="${attendance.actualHours}" />
            <br /><br />

            <label for="${AttributeConst.LATE.getValue()}">遅刻</label><br />
            <input type="text" name="${AttributeConst.LATE.getValue()}" value="${attendance.late}" />
            <br /><br />

            <label for="${AttributeConst.EARLY.getValue()}">早退</label><br />
            <input type="text" name="${AttributeConst.EARLY.getValue()}" value="${attendance.early}" />
            <br /><br />

            <label for="${AttributeConst.OVERTIME.getValue()}">時間外</label><br />
            <input type="text" name="${AttributeConst.OVERTIME.getValue()}" value="${attendance.overtime}" />
            <br /><br />

            <label for="${AttributeConst.MIDNIGHT.getValue()}">深夜</label><br />
            <input type="text" name="${AttributeConst.MIDNIGHT.getValue()}" value="${attendance.midnight}" />
            <br /><br />

            <label for="${AttributeConst.REVISION.getValue()}">修正内容</label><br />
            <input type="text" name="${AttributeConst.REVISION.getValue()}" value="${attendance.revision}" />
            <br /><br />

            <label for="${AttributeConst.COMMENT.getValue()}">備考</label><br />
            <input type="text" name="${AttributeConst.COMMENT.getValue()}" value="${attendance.comment}" />
            <br /><br />

            <input type="hidden" name="${AttributeConst.ATD_ID.getValue()}" value="${attendance.id}" />
            <input type="hidden" name="${AttributeConst.TOKEN.getValue()}" value="${_token}" />
            <button type="submit">登録</button>
        </form>


        <p>
            <a href="<c:url value='?action=${action}&command=${commIdx}' />">一覧に戻る</a>
        </p>
    </c:param>
</c:import>
