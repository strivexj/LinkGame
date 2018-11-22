package com.strivexj.linkgame;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by cwj on 11/22/18 20:41
 */
@Entity
public class Ranking {
    private String userName;

    @Id(autoincrement = true)
    private Long id;
    private String date;
    private long record;

    @Generated(hash = 1405516485)
    public Ranking(String userName, Long id, String date, long record) {
        this.userName = userName;
        this.id = id;
        this.date = date;
        this.record = record;
    }

    public Ranking(String userName, long record, String date) {
        this.userName = userName;
        this.date = date;
        this.record = record;
    }

    @Generated(hash = 1361760905)
    public Ranking() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }
}
