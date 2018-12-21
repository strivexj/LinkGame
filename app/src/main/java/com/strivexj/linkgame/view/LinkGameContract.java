package com.strivexj.linkgame.view;

import android.graphics.Point;
import android.support.annotation.NonNull;

import com.strivexj.linkgame.bean.Item;

import java.util.List;

/**
 * Created by cwj on 12/21/18 13:17
 */
public interface LinkGameContract {
    interface View {
        void init(@NonNull android.view.View view);

        void playBgMusic();

        void playSound(int bomb);

        void stopBgMusic();

        void updateView(List<Item> itemList);

        void select(int position);

        void drawLine(List<Point> printPoints);

        MainActivity getMainActivity();

        void startGame();

    }

    interface Presenter {
        void startGame();

        void resetLinkGameMatrix();

        void bomb(int id);

        void eliminable(int first, int second);

        void judgeGameOver();

        /**
         * 上传游戏记录
         */
        void uploadMyRecord(String username, int type, int record, String date);

        List<Item> getData(int rank);

        String getCurrentTime();
    }
}
