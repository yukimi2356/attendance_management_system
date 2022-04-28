package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.servlet.ServletException;

import constants.AttributeConst;
import constants.ForwardConst;
import models.Attendance;
import models.Employee;
import services.AttendanceService;

public class AttendanceAction extends ActionBase {

    private AttendanceService service;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new AttendanceService();

        //メソッドを実行
        invoke();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        //セッションからログイン中の従業員情報を取得
        Employee loginEmployee = (Employee) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員が作成した勤怠データを、指定されたページ数の一覧画面に表示する分取得する
        int page = getPage();
        List<Attendance> attendances = service.getMinePerPage(loginEmployee, page);

        //ログイン中の従業員が作成した勤怠データの件数を取得
        long myAttendancesCount = service.countAllMine(loginEmployee);

        putRequestScope(AttributeConst.ATTENDANCES, attendances); //取得した勤怠データ
        putRequestScope(AttributeConst.ATD_COUNT, myAttendancesCount); //ログイン中の従業員が作成した勤怠の数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, 50); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン

        //勤怠情報の空インスタンスに、今日の日付と現時刻を設定する
        Attendance a = new Attendance();
        LocalDate ld = LocalDate.now();
        String day = ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        a.setDate(day);
        LocalDateTime ldt = LocalDateTime.now();
        String now = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        a.setAttendedAt(now);
        a.setLeavedAt(now);

        putRequestScope(AttributeConst.ATTENDANCE, a); //日付と現時刻を設定済みの勤怠インスタンス

        //一覧画面を表示
        forward(ForwardConst.FW_ATD_INDEX);
    }

    /**
     * 出勤打刻を行う
     * @throws ServletException
     * @throws IOException
     */
    public void attend() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //勤怠の日付が入力されていなければ、今日の日付を設定
            String day = null;
            if (getRequestParam(AttributeConst.ATD_DATE) == null
                    || getRequestParam(AttributeConst.ATD_DATE).equals("")) {
                day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                day = getRequestParam(AttributeConst.ATD_DATE);
            }

            //セッションからログイン中の従業員情報を取得
            Employee e = (Employee) getSessionScope(AttributeConst.LOGIN_EMP);

            //社員番号と日付を条件に勤怠データを1件取得する
            Attendance a = service.findIdAndDay(e, getRequestParam(AttributeConst.ATD_DATE));

            //同じ社員番号と日付の出勤打刻がなければ登録する
            if (a == null) {

                String attendTime = getRequestParam(AttributeConst.ATTENDED_AT);

                //パラメータの値をもとに勤怠情報のインスタンスを作成する
                a = new Attendance(
                        null,
                        e, //ログインしている従業員を、勤怠作成者として登録する
                        day,
                        8,
                        "出勤",
                        attendTime,
                        null,
                        (Long) null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

                //遅刻時間を求める
                LocalDateTime attendedAt = LocalDateTime.parse(getRequestParam(AttributeConst.ATTENDED_AT),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime startTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 08:30",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long late = ChronoUnit.MINUTES.between(startTime, attendedAt);
                if (late > 0) {
                    a.setLate(late);
                }

                //勤怠情報登録
                List<String> errors = service.create(a);

                if (errors.size() > 0) {
                    //登録中にエラーがあった場合

                    putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                    putRequestScope(AttributeConst.ATTENDANCE, a);//入力された勤怠情報
                    putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                    //新規登録画面を再表示
                    forward(ForwardConst.FW_ATD_INDEX);

                } else {
                    //登録中にエラーがなかった場合

                    //セッションに登録完了のフラッシュメッセージを設定
                    putSessionScope(AttributeConst.FLUSH, "登録が完了しました。");

                    //一覧画面にリダイレクト
                    redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);
                }
            } else {
                //既に出勤打刻がある場合
                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, "出勤打刻済です。");

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 退勤打刻を行う
     * @throws ServletException
     * @throws IOException
     */
    public void leave() throws ServletException, IOException {

        if (checkToken()) {
            //セッションからログイン中の従業員情報を取得
            Employee e = (Employee) getSessionScope(AttributeConst.LOGIN_EMP);

            //社員番号と日付を条件に勤怠データを1件取得する
            Attendance a = service.findIdAndDay(e, getRequestParam(AttributeConst.ATD_DATE));

            //社員番号と日付に一致したデータがあれば追加登録
            if (a != null) {

                //入力された退勤時刻を設定する
                a.setLeavedAt(getRequestParam(AttributeConst.LEAVED_AT));

                //実勤務時間を求める
                LocalDateTime attendedAt = LocalDateTime.parse(a.getAttendedAt(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime leavedAt = LocalDateTime.parse(getRequestParam(AttributeConst.LEAVED_AT),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long actualHours = ChronoUnit.MINUTES.between(attendedAt, leavedAt) - 60; //休憩1ｈを除く
                a.setActualHours(actualHours);

                //早退時間を求める
                LocalDateTime endTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 17:30",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long early = ChronoUnit.MINUTES.between(leavedAt, endTime);
                if (early > 0) {
                    a.setEarly(early);
                }

                //時間外時間を求める
                Long overtime = ChronoUnit.MINUTES.between(endTime, leavedAt);
                a.setOvertime(overtime);

                //深夜時間を求める
                LocalDateTime midnightStartTime = LocalDateTime.parse(
                        getRequestParam(AttributeConst.ATD_DATE) + " 22:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long midnight = ChronoUnit.MINUTES.between(midnightStartTime, leavedAt);
                a.setMidnight(midnight);

                //勤怠データを更新する
                List<String> errors = service.update(a);

                if (errors.size() > 0) {
                    //更新中にエラーが発生した場合

                    putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                    putRequestScope(AttributeConst.ATTENDANCE, a); //入力された勤怠情報
                    putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                    //新規登録画面を再表示
                    forward(ForwardConst.FW_ATD_INDEX);

                } else {
                    //更新中にエラーがなかった場合

                    //セッションに更新完了のフラッシュメッセージを設定
                    putSessionScope(AttributeConst.FLUSH, "更新が完了しました。");

                    //一覧画面にリダイレクト
                    redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);

                }
            } else {
                //社員番号と日付に一致したデータがなければ新規登録
                //CSRF対策 tokenのチェック
                if (checkToken()) {

                    //勤怠の日付が入力されていなければ、今日の日付を設定
                    String day = null;
                    if (getRequestParam(AttributeConst.ATD_DATE) == null
                            || getRequestParam(AttributeConst.ATD_DATE).equals("")) {
                        day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else {
                        day = getRequestParam(AttributeConst.ATD_DATE);
                    }

                    String leavedTime = getRequestParam(AttributeConst.LEAVED_AT);

                    //パラメータの値をもとに勤怠情報のインスタンスを作成する
                    a = new Attendance(
                            null,
                            e, //ログインしている従業員を、勤怠作成者として登録する
                            day,
                            8,
                            "出勤",
                            null,
                            leavedTime,
                            (Long) null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

                    //勤怠情報登録
                    List<String> errors = service.create(a);

                    if (errors.size() > 0) {
                        //登録中にエラーがあった場合

                        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                        putRequestScope(AttributeConst.ATTENDANCE, a);//入力された勤怠情報
                        putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                        //新規登録画面を再表示
                        forward(ForwardConst.FW_ATD_INDEX);

                    } else {
                        //登録中にエラーがなかった場合

                        //セッションに登録完了のフラッシュメッセージを設定
                        putSessionScope(AttributeConst.FLUSH, "登録が完了しました。");

                        //一覧画面にリダイレクト
                        redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);
                    }
                }
            }
        }
    }
}
