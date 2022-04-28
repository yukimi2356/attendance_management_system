package services;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.NoResultException;

import models.Attendance;
import models.Employee;
import models.validators.AttendanceValidator;

/**
 * 日報テーブルの操作に関わる処理を行うクラス
 */

public class AttendanceService extends ServiceBase {


    /**
     * 指定した従業員が作成した日報データを、指定されたページ数の一覧画面に表示する分取得しAttendanceのリストで返却する
     * @param employee 従業員
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<Attendance> getMinePerPage(Employee employee, int page) {

        List<Attendance> Attendance = em.createNamedQuery("attendance.getAllMine", Attendance.class)
                .setParameter("employee", employee)
                .setFirstResult(15 * (page - 1))
                .setMaxResults(15)
                .getResultList();
        return Attendance;
    }

    /**
     * 指定した従業員が作成した日報データの件数を取得し、返却する
     * @param employee
     * @return 勤怠データの件数
     */
    public long countAllMine(Employee employee) {

        long count = (long) em.createNamedQuery("attendance.countAllMine", Long.class)
                .setParameter("employee", employee)
                .getSingleResult();

        return count;
    }

    /**
     * 指定されたページ数の一覧画面に表示する勤怠データを取得し、Attendanceのリストで返却する
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<Attendance> getAllPerPage(int page) {

        List<Attendance> attendance = em.createNamedQuery( "attendance.getAll", Attendance.class)
                .setFirstResult(15 * (page - 1))
                .setMaxResults(15)
                .getResultList();
        return attendance;
    }

    /**
     * 社員番号、日付を条件に取得したデータを Attendanceのインスタンスで返却する
     * @param employee_id 社員番号
     * @param date 日付
     * @return 取得データのインスタンス 取得できない場合null
     */
    public Attendance findIdAndDay(Employee e, String date) {
        Attendance a = null;
        try {

            //社員番号と日付を条件に勤怠データを1件取得する
            a = em.createNamedQuery("attendance.getByIdAndDay", Attendance.class)
                    .setParameter("id", e)
                    .setParameter("date", date)
                    .getSingleResult();

            } catch (NoResultException ex) {
        }
        return a;

    }

    /**
     * 勤怠テーブルのデータの件数を取得し、返却する
     * @return データの件数
     */
    public long countAll() {
        long attendance_count = (long) em.createNamedQuery("attendance.count", Long.class)
                .getSingleResult();
        return attendance_count;
    }

    /**
     * idを条件に取得したデータをAttendanceのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    public Attendance findOne(int id) {
        return findOneInternal(id);
    }

    /**
     * 画面から入力された勤怠の登録内容を元にデータを1件作成し、勤怠テーブルに登録する
     * @param a 勤怠の登録内容
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> create(Attendance a) {
        List<String> errors = AttendanceValidator.validate(a);
        if (errors.size() == 0) {
            LocalDateTime ldt = LocalDateTime.now();
            a.setCreatedAt(ldt);
            a.setUpdatedAt(ldt);
            createInternal(a);
        }

        //バリデーションで発生したエラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * 画面から入力された勤怠の登録内容を元に、勤怠データを更新する
     * @param a 勤怠の更新内容
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> update(Attendance a) {

        //バリデーションを行う
        List<String> errors = AttendanceValidator.validate(a);

        if (errors.size() == 0) {

            //更新日時を現在時刻に設定
            LocalDateTime ldt = LocalDateTime.now();
            a.setUpdatedAt(ldt);

            updateInternal(a);
        }

        //バリデーションで発生したエラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * idを条件にデータを1件取得する
     * @param id
     * @return 取得データのインスタンス
     */
    private Attendance findOneInternal(int id) {
        return em.find(Attendance.class, id);
    }

    /**
     * 勤怠データを1件登録する
     * @param a 勤怠データ
     */
    private void createInternal(Attendance a) {

        em.getTransaction().begin();
        em.persist(a);
        em.getTransaction().commit();

    }

    /**
     * 勤怠データを更新する
     * @param av 勤怠データ
     */
    private void updateInternal(Attendance av) {

        em.getTransaction().begin();
        Attendance a = findOneInternal(av.getId());
        em.getTransaction().commit();

    }

}
