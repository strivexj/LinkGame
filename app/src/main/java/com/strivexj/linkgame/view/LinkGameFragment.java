package com.strivexj.linkgame.view;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.strivexj.linkgame.App;
import com.strivexj.linkgame.GameEngine;
import com.strivexj.linkgame.MyApi;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.SharedPerferencesUtil;
import com.strivexj.linkgame.adapter.LinkGameAdapter;
import com.strivexj.linkgame.base.BaseHolder;
import com.strivexj.linkgame.base.OnItemClickListener;
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
import tyrantgit.explosionfield.ExplosionField;


public class LinkGameFragment extends Fragment {
    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int DIFFICULTY = 3;
    public static final int ROW = 9;
    public static final int COLUMN = 8;
    private RecyclerView recyclerview;
    private LinkGameAdapter linkGameAdapter;
    private List<Item> itemList = new ArrayList<>();
    private MediaPlayer bgMusic;
    private MediaPlayer sound;
    private ImageView bomb;
    private ImageView home;
    private DrawView drawView;

    private int lastClick = -1;
    private long startTime = 0, endTime = 0;
    private boolean isBomb = false;
    private int leftBomb = 2;
    private int leftShuffle = 3;

    private GameEngine gameEngine;
    private MainActivity mainActivity;

    public LinkGameFragment() {
    }

    public static LinkGameFragment newInstance() {
        LinkGameFragment fragment = new LinkGameFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_link_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(@NonNull View view) {
        recyclerview = view.findViewById(R.id.recyclerview);
        bomb = view.findViewById(R.id.bomb);
        home = view.findViewById(R.id.home);
        drawView = view.findViewById(R.id.line);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showMainFragment();
            }
        });

        bomb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBomb = true;
            }
        });

        gameEngine = new GameEngine(ROW, COLUMN);
        loadData(SharedPerferencesUtil.getRank());
        linkGameAdapter = new LinkGameAdapter(getActivity(), itemList, ExplosionField.attach2Window(getActivity()));
        recyclerview.setLayoutManager(new GridLayoutManager(getContext(), COLUMN));
        recyclerview.setAdapter(linkGameAdapter);
        linkGameAdapter.setmOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Item item = itemList.get(position);
                if (item.isEliminated()) return;

                item.setSelect(!item.isSelect());
                linkGameAdapter.setShuffle(false);
                linkGameAdapter.notifyItemChanged(position);

                if (isBomb) {
                    bomb.setClickable(false);
                    isBomb = false;
                    if (leftBomb <= 0) {
                        Toast.makeText(mainActivity, "一局只能使用两次炸弹哦～！", Toast.LENGTH_LONG).show();
                        item.setSelect(!item.isSelect());
                        linkGameAdapter.setShuffle(false);
                        linkGameAdapter.notifyItemChanged(position);
                        bomb.setClickable(true);
                        return;
                    } else {
                        bomb(itemList.get(position).getId());
                        Log.d("onclick", "return");
                        leftBomb--;
                        bomb.setClickable(true);
                        return;
                    }
                }
                if (lastClick != -1) {
                    eliminable(lastClick, position);
                } else {
                    lastClick = position;
                }
            }
        });
    }

    private void bomb(int id) {
        playSound(R.raw.bomb);
        linkGameAdapter.setShuffle(false);

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getId() == id) {
                itemList.get(i).setEliminated(true);
                int rowOne = i / COLUMN;
                int columnOne = i - rowOne * COLUMN;
                gameEngine.eliminate(rowOne, columnOne);
                linkGameAdapter.notifyItemChanged(i);
            }
        }
        judgeGameOver();
    }


    @Override
    public void onResume() {
        super.onResume();
        playBgMusic();
    }

    public void playBgMusic() {
        if (!SharedPerferencesUtil.isMusic()) return;
        bgMusic = MediaPlayer.create(getActivity(), R.raw.background);
        float volume = (float) 0.3;
        bgMusic.setVolume(volume, volume);
        bgMusic.start();
        bgMusic.setLooping(true);
    }

    public void playSound(int bomb) {
        if (!SharedPerferencesUtil.isMusic()) return;
        sound = MediaPlayer.create(getActivity(), bomb);
        sound.start();
    }
    public void stopBgMusic() {
        try {
            bgMusic.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        stopBgMusic();
    }

    private void eliminable(int first, int second) {
        lastClick = -1;

        //计算点击位置所在 行和列
        int rowOne = first / COLUMN;
        int columnOne = first - rowOne * COLUMN;

        int rowTwo = second / COLUMN;
        int columnTwo = second - rowTwo * COLUMN;


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
            gameEngine.printMap();

            List<Point> printPoints = new ArrayList<>();
            for (int i = 0; i < turnPoints.size(); i++) {
                int x = turnPoints.get(i).x, y = turnPoints.get(i).y;
                Point point = new Point(0, 0);

                //判断是否有点在数组之外
                if (x < 0) {
                    x = 0;
                    point.y -= 70;
                }
                if (y < 0) {
                    y = 0;
                    point.x -= 70;
                }
                if (x >= ROW) {
                    x = ROW - 1;
                    point.y += 70;
                }
                if (y >= COLUMN) {
                    y = COLUMN - 1;
                    point.x += 70;
                }

                int position = x * COLUMN + y;
                Log.d("point", "x:" + x + " y:" + y + " p:" + position);

                if (linkGameAdapter.getItemCount() > 0) {
                    RecyclerView.ViewHolder holder = recyclerview.findViewHolderForAdapterPosition(position);
                    if (holder != null && holder instanceof BaseHolder) {
                        BaseHolder viewHolder = (BaseHolder) holder;
                        int[] location = new int[2];
                        viewHolder.itemView.getLocationOnScreen(location);
                        point.x += location[0] + 40;
                        point.y += location[1] - 140;
                        printPoints.add(point);
                        Log.d("point", "第" + i + "个  x坐标：" + location[0] + " y坐标：" + location[1]);
                    }
                }
                drawView.drawLine(printPoints);
            }
            playSound(R.raw.eliminate);
            judgeGameOver();
        } else {
            itemList.get(first).setSelect(false);
            itemList.get(second).setSelect(false);
            playSound(R.raw.again);
        }
        linkGameAdapter.setShuffle(false);
        linkGameAdapter.notifyItemChanged(first);
        linkGameAdapter.notifyItemChanged(second);
    }

    /**
     * 判断游戏是否结束
     */
    private void judgeGameOver() {
        if (gameEngine.isGameOver()) {
            endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;
            stopBgMusic();
            final String username = SharedPerferencesUtil.getUsername();
            if (!TextUtils.isEmpty(username)) {
                Ranking ranking = new Ranking(username, (int) (duration / 1000), getTime(), SharedPerferencesUtil.getRank());
                App.getDaoSession().getRankingDao().insertOrReplace(ranking);
                uploadMyRecord(ranking.getUsername(), ranking.getType(), ranking.getRecord(), ranking.getDate());
                mainActivity.showRankingFragment();
            } else {
                new MaterialDialog.Builder(getActivity())
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

                                SharedPerferencesUtil.setUsername(username);
                                Ranking ranking = new Ranking(userName, (int) (duration / 1000), getTime(), SharedPerferencesUtil.getRank());
                                App.getDaoSession().getRankingDao().insertOrReplace(ranking);
                                uploadMyRecord(ranking.getUsername(), ranking.getType(), ranking.getRecord(), ranking.getDate());
                                mainActivity.showRankingFragment();
                            }
                        }).show();
            }
        }
    }



    /**
     * 上传游戏记录
     *
     * @param username
     * @param type
     * @param record
     * @param date
     */
    private void uploadMyRecord(String username, int type, int record, String date) {
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

    private String getTime() {
        String pattern = "yyyy-MM-dd HH:mm";
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * 生成数据
     *
     * @param rank
     */
    public void loadData(int rank) {
        itemList.clear();
        lastClick = -1;
        startTime = System.currentTimeMillis();
        isBomb = false;
        leftBomb = 2;
        leftShuffle = 3;

        int totalAnimal = 10;
        if (rank == EASY) {
            totalAnimal = 10;
        } else if (rank == MEDIUM) {
            totalAnimal = 15;
        } else if (rank == DIFFICULTY) {
            totalAnimal = 25;
        }
        for (int i = 0; i < (COLUMN * ROW / 2); i++) {
            //每次加两个，保证是偶数
            Item item1 = new Item(i % totalAnimal + 1, false, false);
            Item item2 = new Item(i % totalAnimal + 1, false, false);
            itemList.add(item1);
            itemList.add(item2);
        }
        shuffle();
    }

    /**
     * 重置游戏引擎
     */
    private void resetLinkGameMatrix() {
        gameEngine.init();
        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            int row = i / COLUMN;
            int column = i - row * COLUMN;
            if (itemList.get(i).isEliminated()) {
                gameEngine.eliminate(row, column);
            } else {
                gameEngine.set(row, column);
            }
        }
    }


    /**
     * 重排位置
     */
    public void shuffle() {
        if (leftShuffle <= 0) {
            Toast.makeText(mainActivity, "一局只能使用两次重排哦～！", Toast.LENGTH_LONG).show();
            return;
        }

        if (lastClick != -1) {
            itemList.get(lastClick).setSelect(false);
            lastClick = -1;
        }

        Collections.shuffle(itemList);
        resetLinkGameMatrix();
        if (linkGameAdapter != null) {
            linkGameAdapter.setShuffle(true);
            linkGameAdapter.notifyDataSetChanged();
        }
        leftShuffle--;
    }


}
