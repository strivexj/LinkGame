package com.strivexj.linkgame;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.strivexj.linkgame.bean.DaoMaster;
import com.strivexj.linkgame.bean.DaoSession;

/**
 * Created by cwj on 11/22/18 21:08
 */
public class App extends Application {
    private static Context context;
    private static DaoSession daoSession;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        App.context = context;
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initDaoSession();
    }

    /**
     * 初始化GreenDao数据库
     */
    private void initDaoSession() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "linkgame.db");
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }
}
