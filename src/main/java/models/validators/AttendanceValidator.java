package models.validators;

import java.util.ArrayList;
import java.util.List;

import models.Attendance;

/**
 * 勤怠インスタンスに設定されている値のバリデーションを行うクラス
 */

public class AttendanceValidator {

    /**
     * 勤怠インスタンスの各項目についてバリデーションを行う
     * @param a 勤怠インスタンス
     * @return エラーのリスト
     */
    public static List<String> validate(Attendance a) {
        List<String> errors = new ArrayList<String>();

        //出勤打刻のチェック
        String attendedAtError = validateAttend(a.getAttendedAt());
        if (!attendedAtError.equals("")) {
            errors.add(attendedAtError);
        }
        return errors;
    }

    /**
     * 出勤打刻に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param attendedAt 出勤打刻
     * @return エラーメッセージ
     */
    private static String validateAttend(String attendedAt) {
        if (attendedAt == null || attendedAt.equals("")) {
            return "出勤打刻がありません";
        }

        //入力値がある場合は空文字を返却
        return "";
    }

}