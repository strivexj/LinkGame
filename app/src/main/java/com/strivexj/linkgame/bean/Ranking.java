package com.strivexj.linkgame.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by cwj on 11/22/18 20:41
 */
@Entity
public class Ranking {
    @Id(autoincrement = true)
    private Long id;


    private String username;
    private String date;
    private int record;
    private int type;


    public Ranking(String username, int record, String date, int type) {
        this.username = username;
        this.date = date;
        this.record = record;
        this.type = type;
    }

    @Generated(hash = 1361760905)
    public Ranking() {
    }

    @Generated(hash = 1747706024)
    public Ranking(Long id, String username, String date, int record, int type) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.record = record;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

}
