package constants;

public enum AttributeConst {

    //フラッシュメッセージ
    FLUSH("flush"),

    //一覧画面共通
    MAX_ROW("maxRow"),
    PAGE("page"),

    //入力フォーム共通
    TOKEN("_token"),
    ERR("errors"),

    //ログイン中の従業員
    LOGIN_EMP("login_employee"),

    //ログイン画面
    LOGIN_ERR("loginError"),

    //従業員管理
    EMPLOYEE("employee"),
    EMPLOYEES("employees"),
    EMP_COUNT("employees_count"),
    EMP_ID("id"),
    EMP_CODE("code"),
    EMP_DIV("division"),
    EMP_PASS("password"),
    EMP_NAME("name"),
    EMP_ADMIN_FLG("admin_flag"),

    //部署
    EMP_SALE("sales"),
    EMP_DEV("development"),
    EMP_MAN("management"),

    //管理者フラグ
    ROLE_ADMIN(2),
    ROLE_APPROVAL(1),
    ROLE_GENERAL(0),

    //削除フラグ
    DEL_FLAG_TRUE(1),
    DEL_FLAG_FALSE(0),

    //勤怠管理
    ATTENDANCE("attendance"),
    ATTENDANCES("attendances"),
    ATD_COUNT("attendances_count"),
    ATD_ID("id"),
    ATD_DATE("attendance_date"),
    ATTENDED_AT("attended_at"),
    LEAVED_AT("leaved_at"),
    ACTUAL_HOURS("actual_hours");

    private final String text;
    private final Integer i;

    private AttributeConst(final String text) {
        this.text = text;
        this.i = null;
    }

    private AttributeConst(final Integer i) {
        this.text = null;
        this.i = i;
    }

    public String getValue() {
        return this.text;
    }

    public Integer getIntegerValue() {
        return this.i;
    }

}