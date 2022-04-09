package models;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//カレンダーデータのDTOモデル

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "calendar")
@Entity

public class Calendar {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    //休日設定
    @Column(name = "status", nullable = true)
    private Integer status;



}
