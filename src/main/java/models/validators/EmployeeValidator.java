package models.validators;

import java.util.ArrayList;
import java.util.List;

import models.Employee;
import services.EmployeeService;

/**
 * 従業員インスタンスに設定されている値のバリデーションを行うクラス
 *
 */

public class EmployeeValidator {

    public static List<String> validate(
            EmployeeService service, Employee ev, Boolean codeDuplicateCheckFlag, Boolean passwordCheckFlag){
        List<String>errors = new ArrayList<String>();

        String codeError = validateCode(service, ev.getCode(), codeDuplicateCheckFlag);
        if(!codeError.equals("")) {
            errors.add(codeError);
        }

        String nameError = validateName(ev.getName());
        if(!nameError.equals("")) {
            errors.add(nameError);
        }

        String passError = validatePassword(ev.getPass(),passwordCheckFlag);
        if(!passError.equals("")) {
            errors.add(passError);
        }

        return errors;
    }

    private static String validateCode(EmployeeService service, String code, Boolean codeDuplicateCheckFlag) {

        if(code == null || code.equals("")) {
            return "社員番号を入力してください。";
        }

        if(codeDuplicateCheckFlag) {
            long employeesCount = isDuplicateEmployee(service, code);

            if(employeesCount > 0) {
                return "入力された社員番号の情報は既に存在しています。";
            }
        }

        return "";
    }

    private static  long isDuplicateEmployee(EmployeeService service, String code) {
        long employeeCount = service.countByCode(code);
        return employeeCount;
    }

    private static String validateName(String name) {
        if(name == null || name.equals("")) {
            return "氏名を入力してください。";
        }

        return "";
    }

    private static String validatePassword(String password, boolean passwordCheckFlag) {
        if(passwordCheckFlag && (password == null || password.equals(""))) {
            return "パスワードを入力してください。";
        }

        return"";
    }

}
