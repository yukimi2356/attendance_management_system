package models;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    private LocalDate date;

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
    @Column(name = "actual_hours")
    private Integer actualHours;

    //遅刻時間
    @Column(name = "late", nullable = true)
    private Integer late;

    //早退時間
    @Column(name = "early", nullable = true)
    private Integer early;

    //時間外時間
    @Column(name = "overtime", nullable = true)
    private Integer overtime;

    //深夜時間
    @Column(name = "midnight", nullable = true)
    private Integer midnight;

    //修正内容
    @Column(name = "revision", nullable = true)
    private String revision;

    //備考
    @Column(name = "comment", nullable = true)
    private String comment;



}
