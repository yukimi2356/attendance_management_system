package models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//勤怠データのDTOモデル

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "attendance")
@NamedQueries({
    @NamedQuery(
            name = "attendance.getAll",
            query = "SELECT a FROM Attendance AS a ORDER BY a.id DESC"),
    @NamedQuery(
            name = "attendance.count",
            query = "SELECT COUNT(a) FROM Attendance AS a"),
    @NamedQuery(
            name = "attendance.getAllMine",
            query = "SELECT a FROM Attendance AS a WHERE a.employee = :employee ORDER BY a.id DESC"),
    @NamedQuery(
            name = "attendance.countAllMine",
            query = "SELECT COUNT(a) FROM Attendance AS a WHERE a.employee = :employee"),
    @NamedQuery(
            name = "attendance.getByIdAndDay",
            query = "SELECT a FROM Attendance AS a WHERE a.employee = :id AND a.date = :date")
})

@Entity

public class Attendance {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //登録した社員
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    //日付
    @Column(name = "date", nullable = false)
    private String date;

    //所定勤務時間
    @Column(name = "scheduled_hours")
    private Integer scheduledHours;

    //勤務状態
    @Column(name = "status", nullable = false)
    private String status;

    //出勤時刻
    @Column(name = "attended_at", nullable = true)
    private String attendedAt;

    //退勤時刻
    @Column(name = "leaved_at", nullable = true)
    private String leavedAt;

    //実勤務時刻
    @Column(name = "actual_hours", nullable = true)
    private Long actualHours;

    //遅刻時間
    @Column(name = "late", nullable = true)
    private Long late;

    //早退時間
    @Column(name = "early", nullable = true)
    private Long early;

    //時間外時間
    @Column(name = "overtime", nullable = true)
    private Long overtime;

    //深夜時間
    @Column(name = "midnight", nullable = true)
    private Long midnight;

    //修正内容
    @Column(name = "revision", nullable = true)
    private String revision;

    //備考
    @Column(name = "comment", nullable = true)
    private String comment;

    //作成日時
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    //更新日時
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
