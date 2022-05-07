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
        //
        //        //ログイン中の従業員が作成した勤怠データを、指定されたページ数の一覧画面に表示する分取得する
        //        int page = getPage();
        //        List<Attendance> attendances = service.getMinePerPage(loginEmployee, page);

        //ログイン中の従業員が作成した勤怠データを取得する
        List<Attendance> attendances = service.getAllMine(loginEmployee);

        //ログイン中の従業員が作成した勤怠データの件数を取得
        long myAttendancesCount = service.countAllMine(loginEmployee);

        putRequestScope(AttributeConst.ATTENDANCES, attendances); //取得した勤怠データ
        putRequestScope(AttributeConst.ATD_COUNT, myAttendancesCount); //ログイン中の従業員が作成した勤怠の数
        //putRequestScope(AttributeConst.PAGE, page); //ページ数
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

        //指定期間における項目ごとの集計時間を求める
        int syukkin = 0; //出勤日数
        int yukyu = 0;//有休日数
        int kekkin = 0;//欠勤日数
        long totalAttendance = 0;//勤務時間
        long totalLate = 0;//遅刻
        long totalEarly = 0;//早退
        long totalOvertime = 0;//時間外
        long totalMidnight = 0;//深夜

        for (Attendance roudou : attendances) {
            if (roudou.getStatus().equals("出勤")) {
                syukkin++;
            }
            if (roudou.getStatus().equals("有休")) {
                yukyu++;
            }
            if (roudou.getStatus().equals("欠勤")) {
                kekkin++;
            }
            totalAttendance += roudou.getActualHours();
            totalEarly += roudou.getEarly();
            totalOvertime += roudou.getOvertime();
            totalMidnight += roudou.getMidnight();
        }
        putRequestScope(AttributeConst.SYUKKIN, syukkin);
        putRequestScope(AttributeConst.YUKYU, yukyu);
        putRequestScope(AttributeConst.KEKKIN, kekkin);
        putRequestScope(AttributeConst.TOTAL_ATTENDANCE, totalAttendance);
        putRequestScope(AttributeConst.TOTAL_LATE, totalLate);
        putRequestScope(AttributeConst.TOTAL_EARLY, totalEarly);
        putRequestScope(AttributeConst.TOTAL_OVERTIME, totalOvertime);
        putRequestScope(AttributeConst.TOTAL_MIDNIGHT, totalMidnight);

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
                        "出勤",
                        attendTime,
                        null,
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        "-",
                        "-",
                        null,
                        null);

                //遅刻時間を求める
                LocalDateTime attendedAt = LocalDateTime.parse(getRequestParam(AttributeConst.ATTENDED_AT),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime startTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 09:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long late = ChronoUnit.MINUTES.between(startTime, attendedAt);
                //出勤が13時以降の場合は休憩時間1h分を遅刻時間から除く
                if (late > 240) {
                    late = late - 60;
                }
                if (late > 0) {
                    a.setLate(late);
                } else {
                    a.setLate((long) 0);
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

                //定内勤務時間を求める
                LocalDateTime attendedAt = LocalDateTime.parse(a.getAttendedAt(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime leavedAt = LocalDateTime.parse(getRequestParam(AttributeConst.LEAVED_AT),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime startTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 09:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long beforeStartTime = ChronoUnit.MINUTES.between(attendedAt, startTime);
                LocalDateTime endTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 18:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Long afterEndTime = ChronoUnit.MINUTES.between(endTime, leavedAt);

                Long actualHours = ChronoUnit.MINUTES.between(attendedAt, leavedAt); //遅刻あり早退あり
                if (beforeStartTime > 0 && afterEndTime > 0) { //遅刻なし早退なし
                    actualHours = ChronoUnit.MINUTES.between(startTime, endTime);
                } else if (beforeStartTime > 0 && afterEndTime < 0) {//遅刻なし早退あり
                    actualHours = ChronoUnit.MINUTES.between(startTime, leavedAt);
                } else if (beforeStartTime < 0 && afterEndTime > 0) { //遅刻あり早退なし
                    actualHours = ChronoUnit.MINUTES.between(attendedAt, endTime);
                }
                //定内勤務時間が300分を超える場合は休憩1ｈを除く
                if (actualHours > 300) {
                    actualHours = actualHours - 60;

                    a.setActualHours(actualHours);

                    //早退時間を求める
                    Long early = ChronoUnit.MINUTES.between(leavedAt, endTime);
                    //早退が13時以前の場合は休憩時間1h分を除く
                    if (early > 300) {
                        early = early - 60;
                    }
                    if (early > 0) {
                        a.setEarly(early);
                    } else {
                        a.setEarly((long) 0);
                    }

                    //時間外時間を求める
                    Long overtime = ChronoUnit.MINUTES.between(endTime, leavedAt);
                    if (overtime > 0) {
                        a.setOvertime(overtime);
                    } else {
                        a.setOvertime((long) 0);
                    }

                    //深夜時間を求める
                    LocalDateTime midnightStartTime = LocalDateTime.parse(
                            getRequestParam(AttributeConst.ATD_DATE) + " 22:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    Long midnight = ChronoUnit.MINUTES.between(midnightStartTime, leavedAt);
                    if (midnight > 0) {
                        a.setMidnight(midnight);
                    } else {
                        a.setMidnight((long) 0);
                    }

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
                                "出勤",
                                null,
                                leavedTime,
                                (long) 0,
                                (long) 0,
                                (long) 0,
                                (long) 0,
                                (long) 0,
                                "-",
                                "-",
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

    /**
     * 有休欠勤登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void rest() throws ServletException, IOException {

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

                String rest = getRequestParam(AttributeConst.STATUS);

                System.out.println("rest =" + rest);

                //パラメータの値をもとに勤怠情報のインスタンスを作成する
                a = new Attendance(
                        null,
                        e, //ログインしている従業員を、勤怠作成者として登録する
                        day,
                        rest,
                        "-",
                        "-",
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        (long) 0,
                        "-",
                        "-",
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
            } else {
                //既に出勤打刻がある場合
                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, "登録済です。");

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException {

        //idを条件に勤怠データを取得する
        Attendance a = service.findOne(toNumber(getRequestParam(AttributeConst.ATD_ID)));

        //セッションからログイン中の従業員情報を取得
        Employee e = (Employee) getSessionScope(AttributeConst.LOGIN_EMP);

        if (a == null || e.getId() != a.getEmployee().getId()) {
            //該当の勤怠データが存在しない、または
            //ログインしている従業員が勤怠の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.ATTENDANCE, a); //取得した日報データ

            //編集画面を表示
            forward(ForwardConst.FW_ATD_EDIT);
        }

    }

    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //idを条件に勤怠データを取得する
            Attendance a = service.findOne(toNumber(getRequestParam(AttributeConst.ATD_ID)));

            System.out.println("attendance =" + a);

            //入力された勤怠内容を設定する
            a.setDate(getRequestParam(AttributeConst.ATD_DATE));
            a.setStatus(getRequestParam(AttributeConst.STATUS));
            a.setAttendedAt(getRequestParam(AttributeConst.ATTENDED_AT));
            a.setAttendedAt(getRequestParam(AttributeConst.ATTENDED_AT));
            a.setLeavedAt(getRequestParam(AttributeConst.LEAVED_AT));
            a.setActualHours(Long.parseLong(getRequestParam(AttributeConst.ACTUAL_HOURS)));
            a.setLate(Long.parseLong(getRequestParam(AttributeConst.LATE)));
            a.setEarly(Long.parseLong(getRequestParam(AttributeConst.EARLY)));
            a.setOvertime(Long.parseLong(getRequestParam(AttributeConst.OVERTIME)));
            a.setMidnight(Long.parseLong(getRequestParam(AttributeConst.MIDNIGHT)));
            a.setComment(getRequestParam(AttributeConst.COMMENT));
            a.setRevision(getRequestParam(AttributeConst.REVISION));

            //定内勤務時間を求める
            LocalDateTime attendedAt = LocalDateTime.parse(a.getAttendedAt(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime leavedAt = LocalDateTime.parse(getRequestParam(AttributeConst.LEAVED_AT),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime startTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 09:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Long beforeStartTime = ChronoUnit.MINUTES.between(attendedAt, startTime);
            LocalDateTime endTime = LocalDateTime.parse(getRequestParam(AttributeConst.ATD_DATE) + " 18:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Long afterEndTime = ChronoUnit.MINUTES.between(endTime, leavedAt);
            Long actualHours = ChronoUnit.MINUTES.between(attendedAt, leavedAt); //遅刻あり早退あり
            if (beforeStartTime > 0 && afterEndTime > 0) { //遅刻なし早退なし
                actualHours = ChronoUnit.MINUTES.between(startTime, endTime);
            } else if (beforeStartTime > 0 && afterEndTime < 0) {//遅刻なし早退あり
                actualHours = ChronoUnit.MINUTES.between(startTime, leavedAt);
            } else if (beforeStartTime < 0 && afterEndTime > 0) { //遅刻あり早退なし
                actualHours = ChronoUnit.MINUTES.between(attendedAt, endTime);
            }
            //定内勤務時間が300分を超える場合は休憩1ｈを除く
            if (actualHours > 300) {
                actualHours = actualHours - 60;
            }

            a.setActualHours(actualHours);

            //遅刻時間を求める
            Long late = ChronoUnit.MINUTES.between(startTime, attendedAt);
            //出勤が13時以降の場合は休憩時間1h分を遅刻時間から除く
            if (late > 240) {
                late = late - 60;
            }
            if (late > 0) {
                a.setLate(late);
            } else {
                a.setLate((long) 0);
            }

            //早退時間を求める
            Long early = ChronoUnit.MINUTES.between(leavedAt, endTime);
            //早退が13時以前の場合は休憩時間1h分を除く
            if (early > 300) {
                early = early - 60;
            }
            if (early > 0) {
                a.setEarly(early);
            } else {
                a.setEarly((long) 0);
            }

            //時間外時間を求める
            Long overtime = ChronoUnit.MINUTES.between(endTime, leavedAt);
            if (overtime > 0) {
                a.setOvertime(overtime);
            } else {
                a.setOvertime((long) 0);
            }

            //深夜時間を求める
            LocalDateTime midnightStartTime = LocalDateTime.parse(
                    getRequestParam(AttributeConst.ATD_DATE) + " 22:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Long midnight = ChronoUnit.MINUTES.between(midnightStartTime, leavedAt);

            if (midnight > 0) {
                a.setMidnight(midnight);
            } else {
                a.setMidnight((long) 0);
            }

            //勤怠データを更新する
            List<String> errors = service.update(a);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.ATTENDANCE, a); //入力された勤怠情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_ATD_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, "更新が完了しました。");

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_ATD, ForwardConst.CMD_INDEX);

            }
        }
    }

}
