<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="constants.ForwardConst" %>
<%@ page import="constants.AttributeConst" %>

<c:set var="action" value="${ForwardConst.ACT_ATD.getValue()}" />
<c:set var="actAtd" value="${ForwardConst.ACT_ATD.getValue()}" />
<c:set var="commIdx" value="${ForwardConst.CMD_INDEX.getValue()}" />
<c:set var="commAtd" value="${ForwardConst.CMD_ATTEND.getValue()}" />
<c:set var="commLev" value="${ForwardConst.CMD_LEAVE.getValue()}" />
<c:set var="commRst" value="${ForwardConst.CMD_REST.getValue()}" />
<c:set var="commEdit" value="${ForwardConst.CMD_EDIT.getValue()}" />

<c:import url="/WEB-INF/views/layout/app.jsp">
    <c:param name="content">
        <c:if test="${flush != null}">
            <div id="flush_success">
                <c:out value="${flush}"></c:out>
            </div>
        </c:if>


        <h2>打刻</h2>
        <c:if test="${errors != null}">
                <div id="flush_error">
                    入力内容にエラーがあります。<br />
                    <c:forEach var="error" items="${errors}">
            ・<c:out value="${error}" />
                        <br />
                    </c:forEach>
                </div>
            </c:if>
        <div id = "dakoku">
            <div id = "syukkin">
                <h3>【出勤】</h3>
                <form method="POST" action="<c:url value='?action=${action}&command=${commAtd}' />">
                        <fmt:parseDate value="${attendance.date}" pattern="yyyy-MM-dd" var="attendanceDay" type="date" />
                        <label for="${AttributeConst.ATD_DATE.getValue()}"> &emsp;日付&emsp;</label>
                        <input type="text" name="${AttributeConst.ATD_DATE.getValue()}" value="<fmt:formatDate value='${attendanceDay}' pattern='yyyy-MM-dd' />" />
                        <br /> <br />
                        <label for="${AttributeConst.ATTENDED_AT.getValue()}">出勤時刻</label>
                        <input type="text" name="${AttributeConst.ATTENDED_AT.getValue()}"value="${attendance.attendedAt}" />
                        <button type="submit">出勤</button>
                        <br /> <br />
                        <input type="hidden" name="${AttributeConst.ATD_ID.getValue()}" value="${attendance.id}" />
                        <input type="hidden" name="${AttributeConst.TOKEN.getValue()}" value="${_token}" />
                </form>
            </div>

            <div id = "taikin">
                <h3>【退勤】</h3>
                <form method="POST" action="<c:url value='?action=${action}&command=${commLev}' />">
                        <fmt:parseDate value="${attendance.date}" pattern="yyyy-MM-dd" var="attendanceDay" type="date" />
                        <label for="${AttributeConst.ATD_DATE.getValue()}"> &emsp;日付&emsp;</label>
                        <input type="text" name="${AttributeConst.ATD_DATE.getValue()}" value="<fmt:formatDate value='${attendanceDay}' pattern='yyyy-MM-dd' />" />
                        <br /> <br />
                        <label for="${AttributeConst.LEAVED_AT.getValue()}">退勤時刻</label>
                        <input type="text" name="${AttributeConst.LEAVED_AT.getValue()}" value="${attendance.leavedAt}" />
                        <button type="submit">退勤</button>
                        <br /> <br />
                        <input type="hidden" name="${AttributeConst.ATD_ID.getValue()}" value="${attendance.id}" />
                        <input type="hidden" name="${AttributeConst.TOKEN.getValue()}" value="${_token}" />
                </form>
            </div>

            <div id = "yasumi">
                <h3>【有休・欠勤】</h3>
                <form method="POST" action="<c:url value='?action=${action}&command=${commRst}' />">
                               <fmt:parseDate value="${attendance.date}" pattern="yyyy-MM-dd" var="attendanceDay" type="date" />
                        <label for="${AttributeConst.ATD_DATE.getValue()}"> &emsp;日付&emsp;</label>
                        <input type="text" name="${AttributeConst.ATD_DATE.getValue()}" value="<fmt:formatDate value='${attendanceDay}' pattern='yyyy-MM-dd' />" />
                        <br /> <br />
                         <label for="${AttributeConst.STATUS.getValue()}"> &emsp;区分&emsp;</label>
                        <select name="${AttributeConst.STATUS.getValue()}">
                            <option value="有休">有休</option>
                            <option value="欠勤">欠勤</option>
                        </select>
                        <button type="submit">登録</button>
                        <br /> <br />
                        <input type="hidden" name="${AttributeConst.ATD_ID.getValue()}" value="${attendance.id}" />
                        <input type="hidden" name="${AttributeConst.TOKEN.getValue()}" value="${_token}" />
                        <br /><br />
                </form>
            </div>
        </div>

        <hr>

        <h2>勤怠一覧</h2>

        <h3>【期間集計】</h3>
        <form method="POST" action="<c:url value='?action=${action}&command=${commLev}' />">
            <c:if test="${errors != null}">
                <div id="flush_error">
                    入力内容にエラーがあります。<br />
                    <c:forEach var="error" items="${errors}">
            ・<c:out value="${error}" />
                        <br />
                    </c:forEach>
                </div>
            </c:if>

                <label for="aggregationperiod">期間</label>
                <input type="date" value="${aggregationperiod}" />
                ～
                <input type="date" value="${aggregationperiod}" />
                <button type="submit">表示</button>
                <br /> <br />
                <input type="hidden" name="${AttributeConst.ATD_ID.getValue()}"value="${report.id}" />
                <input type="hidden" name="${AttributeConst.TOKEN.getValue()}" value="${_token}" />
        </form>
        <table id="attendance_list">
            <tbody>
                <tr>
                    <th>社員番号</th>
                    <th>氏名</th>
                    <th>部署</th>
                    <th>出勤日数</th>
                    <th>有休日数</th>
                    <th>欠勤日数</th>
                    <th>勤務時間</th>
                    <th>遅刻時間</th>
                    <th>早退時間</th>
                    <th>時間外時間</th>
                    <th>深夜時間</th>
                </tr>
                <tr>
                    <td><c:out value="${sessionScope.login_employee.code}" /></td>
                    <td><c:out value="${sessionScope.login_employee.name}" /></td>
                    <td><c:choose>
                            <c:when test="${sessionScope.login_employee.division == AttributeConst.EMP_SALE}">営業部</c:when>
                            <c:when test="${sessionScope.login_employee.division == AttributeConst.EMP_DEV}">開発部</c:when>
                            <c:when test="${sessionScope.login_employee.division == AttributeConst.EMP_MAN}">管理部</c:when>
                            <c:otherwise></c:otherwise>
                        </c:choose></td>
                    <td><c:out value="${syukkin}" /></td>
                    <td><c:out value="${yukyu}" /></td>
                    <td><c:out value="${kekkin}" /></td>
                    <td><c:out value="${total_attendance}" /></td>
                    <td><c:out value="${total_late}" /></td>
                    <td><c:out value="${total_early}" /></td>
                    <td><c:out value="${total_overtime}" /></td>
                    <td><c:out value="${total_midnight}" /></td>
                </tr>
            </tbody>
        </table>
        <br /> <br />

        <h3>【個人勤怠詳細】</h3>
        <table id="attendance_list">
            <tbody>
                <tr>
                    <th>日付</th>
                    <th>勤務</th>
                    <th>出勤時刻</th>
                    <th>退勤時刻</th>
                    <th>定内勤務時間</th>
                    <th>遅刻</th>
                    <th>早退</th>
                    <th>時間外</th>
                    <th>深夜時間</th>
                    <th>修正内容</th>
                    <th>備考</th>
                </tr>
                <c:forEach var="attendance" items="${attendances}" varStatus="status">
                    <tr class="row${status.count % 2}">
                        <td class="attendance_action"><a href="<c:url value='?action=${actAtd}&command=${commEdit}&id=${attendance.id}' />">
                        <c:out value= '${attendance.date}' /></a></td>
                        <td><c:out value="${attendance.status}" /></td>
                        <td><c:out value="${attendance.attendedAt}" /></td>
                        <td><c:out value="${attendance.leavedAt}" /></td>
                        <td><c:out value="${attendance.actualHours}" /></td>
                        <td><c:out value="${attendance.late}" /></td>
                        <td><c:out value="${attendance.early}" /></td>
                        <td><c:out value="${attendance.overtime}" /></td>
                        <td><c:out value="${attendance.midnight}" /></td>
                        <td><c:out value="${attendance.revision}" /></td>
                        <td><c:out value="${attendance.comment}" /></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <div id="pagination">
            （全 ${attendances_count} 件）<br />
            <c:forEach var="i" begin="1" end="${((attendances_count - 1) / maxRow) + 1}" step="1">
                <c:choose>
                    <c:when test="${i == page}">
                        <c:out value="${i}" />&nbsp;
                    </c:when>
                    <c:otherwise>
                        <a href="<c:url value='?action=${actAtd}&command=${commIdx}&page=${i}' />"><c:out value="${i}" /></a>&nbsp;
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>

    </c:param>
</c:import>