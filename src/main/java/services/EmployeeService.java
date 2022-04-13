package services;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.NoResultException;

import models.Employee;
import models.validators.EmployeeValidator;
import utils.EncryptUtil;

public class EmployeeService extends ServiceBase {

    /**
     * 指定されたページ数の一覧画面に表示するデータを取得し、EmployeeViewのリストで返却する
     * @param page ページ数
     * @return 表示するデータのリスト
     */
    public List<Employee> getPerPage(int page) {
        List<Employee> employees = em.createNamedQuery("employee.getAll", Employee.class)
                .setFirstResult(15 * (page - 1))
                .setMaxResults(15)
                .getResultList();

        return employees;
    }

    /**
     * 従業員テーブルのデータの件数を取得し、返却する
     * @return 従業員テーブルのデータの件数
     */
    public long countAll() {
        long empCount = (long) em.createNamedQuery("employee.count", Long.class)
                .getSingleResult();

        return empCount;
    }

    /**
     * 社員番号、パスワードを条件に取得したデータをEmployeeのインスタンスで返却する
     * @param code 社員番号
     * @param plainPass パスワード文字列
     * @param pepper pepper文字列
     * @return 取得データのインスタンス 取得できない場合null
     */
    public Employee findOne(String code, String plainPass, String pepper) {
        Employee e = null;
        try {
            //パスワードのハッシュ化
            String pass = EncryptUtil.getPasswordEncrypt(plainPass, pepper);

            //社員番号とハッシュ化済パスワードを条件に未削除の従業員を1件取得する
            e = em.createNamedQuery("employee.getByCodeAndPass", Employee.class)
                    .setParameter("code", code)
                    .setParameter("pass", pass)
                    .getSingleResult();

        } catch (NoResultException ex) {
        }

        return e;

    }

    /**
     * idを条件に取得したデータをEmployeeのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    public Employee findOne(int id) {
        Employee e = findOneInternal(id);
        return e;
    }

    /**
     * 社員番号を条件に該当するデータの件数を取得し、返却する
     * @param code 社員番号
     * @return 該当するデータの件数
     */
    public long countByCode(String code) {

        //指定した社員番号を保持する従業員の件数を取得する
        long employees_count = (long) em.createNamedQuery("employee.countRegisteredByCode", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        return employees_count;
    }

    /**
     * 画面から入力された従業員の登録内容を元にデータを1件作成し、従業員テーブルに登録する
     * @param ev 画面から入力された従業員の登録内容
     * @param pepper pepper文字列
     * @return バリデーションや登録処理中に発生したエラーのリスト
     */
    public List<String> create(Employee ev, String pepper) {

        //パスワードをハッシュ化して設定
        String pass = EncryptUtil.getPasswordEncrypt(ev.getPass(), pepper);
        ev.setPass(pass);

        //登録日時、更新日時は現在時刻を設定する
        LocalDateTime now = LocalDateTime.now();
        ev.setCreatedAt(now);
        ev.setUpdatedAt(now);

        //登録内容のバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, ev, true, true);

        //バリデーションエラーがなければデータを登録する
        if (errors.size() == 0) {
            create(ev);
        }

        //エラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * 画面から入力された従業員の更新内容を元にデータを1件作成し、従業員テーブルを更新する
     * @param ev 画面から入力された従業員の登録内容
     * @param pepper pepper文字列
     * @return バリデーションや更新処理中に発生したエラーのリスト
     */
    public List<String> update(Employee ev, String pepper) {

        //idを条件に登録済みの従業員情報を取得する
        Employee savedEmp = findOne(ev.getId());

        boolean validateCode = false;
        if (!savedEmp.getCode().equals(ev.getCode())) {
            //社員番号を更新する場合

            //社員番号についてのバリデーションを行う
            validateCode = true;
            //変更後の社員番号を設定する
            savedEmp.setCode(ev.getCode());
        }

        boolean validatePass = false;
        if (ev.getPass() != null && !ev.getPass().equals("")) {
            //パスワードに入力がある場合

            //パスワードについてのバリデーションを行う
            validatePass = true;

            //変更後のパスワードをハッシュ化し設定する
            savedEmp.setPass(
                    EncryptUtil.getPasswordEncrypt(ev.getPass(), pepper));
        }

        savedEmp.setName(ev.getName()); //変更後の氏名を設定する
        savedEmp.setAdminFlag(ev.getAdminFlag()); //変更後の管理者フラグを設定する

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //更新内容についてバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, savedEmp, validateCode, validatePass);

        //バリデーションエラーがなければデータを更新する
        if (errors.size() == 0) {
            update(savedEmp);
        }

        //エラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * idを条件に従業員データを論理削除する
     * @param id
     */
    public void destroy(Integer id) {

        //idを条件に登録済みの従業員情報を取得する
        Employee savedEmp = findOne(id);

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //論理削除フラグをたてる
        savedEmp.setDeleteFlag(1);

        //更新処理を行う
        update(savedEmp);

    }

    /**
     * 社員番号とパスワードを条件に検索し、データが取得できるかどうかで認証結果を返却する
     * @param code 社員番号
     * @param plainPass パスワード
     * @param pepper pepper文字列
     * @return 認証結果を返却す(成功:true 失敗:false)
     */
    public Boolean validateLogin(String code, String plainPass, String pepper) {

        boolean isValidEmployee = false;
        if (code != null && !code.equals("") && plainPass != null && !plainPass.equals("")) {
            Employee e = findOne(code, plainPass, pepper);

            if (e != null && e.getId() != null) {

                //データが取得できた場合、認証成功
                isValidEmployee = true;
            }
        }

        //認証結果を返却する
        return isValidEmployee;
    }

    /**
     * idを条件にデータを1件取得し、Employeeのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    private Employee findOneInternal(int id) {
        Employee e = em.find(Employee.class, id);

        return e;
    }

    /**
     * 従業員データを1件登録する
     * @param ev 従業員データ
     * @return 登録結果(成功:true 失敗:false)
     */
    private void create(Employee ev) {

        em.getTransaction().begin();
        em.persist(ev);
        em.getTransaction().commit();

    }

    /**
     * 従業員データを更新する
     * @param ev 画面から入力された従業員の登録内容
     */
    private void update(Employee ev) {

        em.getTransaction().begin();
        Employee e = findOneInternal(ev.getId());
        em.getTransaction().commit();

    }

}