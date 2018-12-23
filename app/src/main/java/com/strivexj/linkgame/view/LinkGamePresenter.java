package com.strivexj.linkgame.view;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.strivexj.linkgame.App;
import com.strivexj.linkgame.GameEngine;
import com.strivexj.linkgame.MyApi;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.SharedPerferencesUtil;
import com.strivexj.linkgame.adapter.LinkGameAdapter;
import com.strivexj.linkgame.bean.Item;
import com.strivexj.linkgame.bean.Ranking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cwj on 12/21/18 13:19
 */
public class LinkGamePresenter implements LinkGameContract.Presenter {
    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int DIFFICULTY = 3;
    LinkGameContract.View mView;

    private int row, column;
    private GameEngine gameEngine;
    private long startTime = 0, endTime = 0;
    private LinkGameAdapter linkGameAdapter;

    private List<Item> itemList;

    public LinkGamePresenter(int row, int column, LinkGameContract.View view, LinkGameAdapter linkGameAdapter) {
        this.row = row;
        this.column = column;
        this.mView = view;
        this.linkGameAdapter = linkGameAdapter;
        gameEngine = new GameEngine(row, column);
    }

    @Override
    public void startGame() {
        startTime = System.currentTimeMillis();
        itemList = getData(SharedPerferencesUtil.getRank());
        resetLinkGameMatrix();
        mView.updateView(itemList);
    }

    @Override
    public void resetLinkGameMatrix() {
        gameEngine.init();
        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            int r = i / column;
            int c = i % column;
            if (itemList.get(i).isEliminated()) {
                gameEngine.eliminate(r, c);
            } else {
                gameEngine.set(r, c);
            }
        }
    }


    @Override
    public void bomb(int id) {
        mView.playSound(R.raw.bomb);
        linkGameAdapter.setShuffle(false);
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getId() == id) {
                itemList.get(i).setEliminated(true);
                int rowOne = i / column;
                int columnOne = i % column;
                gameEngine.eliminate(rowOne, columnOne);
                linkGameAdapter.notifyItemChanged(i);
            }
        }
        judgeGameOver();
    }

    @Override
    public void eliminable(int first, int second) {
        //计算点击位置所在 行和列
        int rowOne = first / column;
        int columnOne = first % column;

        int rowTwo = second / column;
        int columnTwo = second % column;

        int id1 = itemList.get(first).getId(), id2 = itemList.get(second).getId();

        Log.d("onclick", "id1:" + id1 + " id2:" + id2 + " rowOne:" + rowOne + " columnOne:" + columnOne
                + " rowTwo:" + rowTwo + " columnTwo:" + columnTwo);

        List<Point> turnPoints = gameEngine.getLinkPoints(
                new Point(rowOne, columnOne), new Point(rowTwo, columnTwo), 2);
        if (itemList.get(first).getId() == itemList.get(second).getId() && turnPoints != null) {
            itemList.get(first).setEliminated(true);
            itemList.get(second).setEliminated(true);
            gameEngine.eliminate(rowOne, columnOne);
            gameEngine.eliminate(rowTwo, columnTwo);

            mView.drawLine(turnPoints);
            mView.playSound(R.raw.eliminate);
        } else {
            itemList.get(first).setSelect(false);
            itemList.get(second).setSelect(false);
            itemList.get(first).setEliminated(false);
            itemList.get(second).setEliminated(false);
            mView.playSound(R.raw.again);
        }

        gameEngine.printMap();

        linkGameAdapter.setShuffle(false);
        linkGameAdapter.notifyItemChanged(first);
        linkGameAdapter.notifyItemChanged(second);

        judgeGameOver();
    }

    @Override
    public void judgeGameOver() {
        if (gameEngine.isGameOver()) {
            endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;
            mView.stopBgMusic();
            final String username = SharedPerferencesUtil.getUsername();
            Log.d("username", username + " asda");
            if (!TextUtils.isEmpty(username)) {
                Ranking ranking = new Ranking(username, (int) (duration / 1000), getCurrentTime(), SharedPerferencesUtil.getRank());
                App.getDaoSession().getRankingDao().insertOrReplace(ranking);
                uploadMyRecord(ranking.getUsername(), ranking.getType(), ranking.getRecord(), ranking.getDate());
                mView.getMainActivity().showRankingFragment();
            } else {
                new MaterialDialog.Builder(mView.getMainActivity())
                        .title("Add Score (" + duration / 1000 + "s)")
                        .content("Please input your username~!")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .cancelable(false)
                        .input(0, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String userName = input.toString().trim();
                                if (TextUtils.isEmpty(userName))
                                    userName = "Anonymous";

                                SharedPerferencesUtil.setUsername(userName);
                                Log.d("username", userName + " save");
                                Ranking ranking = new Ranking(userName, (int) (duration / 1000), getCurrentTime(), SharedPerferencesUtil.getRank());
                                App.getDaoSession().getRankingDao().insertOrReplace(ranking);
                                uploadMyRecord(ranking.getUsername(), ranking.getType(), ranking.getRecord(), ranking.getDate());
                                mView.getMainActivity().showRankingFragment();
                            }
                        }).show();
            }
        }
    }

    @Override
    public void uploadMyRecord(String username, int type, int record, String date) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyApi.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(MyApi.class).uploadRecord(username, type, record, date).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String re = response.toString();
                Log.d("uploadMyRecord", re);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("uploadMyRecord", "failed");
            }
        });
    }

    @Override
    public List<Item> getData(int rank) {
        List<Item> itemList = new ArrayList<>();
        int totalAnimal = 10;
        if (rank == EASY) {
            totalAnimal = 10;
        } else if (rank == MEDIUM) {
            totalAnimal = 15;
        } else if (rank == DIFFICULTY) {
            totalAnimal = 25;
        }
        for (int i = 0; i < (column * row / 2); i++) {
            //每次加两个，保证是偶数
            Item item1 = new Item(i % totalAnimal + 1, false, false);
            Item item2 = new Item(i % totalAnimal + 1, false, false);
            itemList.add(item1);
            itemList.add(item2);
        }

        Collections.shuffle(itemList);
        return itemList;
    }

    @Override
    public String getCurrentTime() {
        String pattern = "yyyy-MM-dd HH:mm";
        return new SimpleDateFormat(pattern).format(new Date());
    }
}
