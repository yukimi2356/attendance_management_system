package models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//従業員データのDTOモデル

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "employees")
@NamedQueries({
    @NamedQuery(
            name = "employee.getAll",
            query = "SELECT e FROM Employee AS e ORDER BY e.id DESC"),
    @NamedQuery(
            name = "employee.count",
            query = "SELECT COUNT(e) FROM Employee AS e"),
    @NamedQuery(
            name = "employee.countRegisteredByCode",
            query = "SELECT COUNT(e) FROM Employee AS e WHERE e.code = :" + "code"),
    @NamedQuery(
            name = "employee.getByCodeAndPass",
            query = "SELECT e FROM Employee AS e WHERE e.deleteFlag = 0 AND e.code = :" + "code" + " AND e.pass = :" + "pass")
})

@Entity
public class Employee {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //社員番号
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    //氏名
    @Column(name = "name", nullable = false)
    private String name;

    //所属
    @Column(name = "division", nullable = false)
    private String division;

    //管理者フラグ（一般:0 / 承認者:1 / 管理者:2)
    @Column(name = "admin_flag", nullable = false)
    private Integer adminFlag;

    //削除フラグ（在籍:0 / 退職:1）
    @Column(name = "delete_flag", nullable = false)
    private Integer deleteFlag;

    //パスワード
    @Column(name = "pass", length = 64, nullable = false)
    private String pass;

    //作成日時
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    //更新日時
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
