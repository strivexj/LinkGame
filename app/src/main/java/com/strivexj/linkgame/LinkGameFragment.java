package com.strivexj.linkgame;

import android.content.Context;
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
import com.strivexj.linkgame.base.OnItemClickListener;
import com.strivexj.linkgame.bean.Item;
import com.strivexj.linkgame.bean.Ranking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
        loadData(getActivity().getSharedPreferences("linkgame", Context.MODE_PRIVATE).getInt("rank", 1));


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
                    isBomb = false;
                    if (leftBomb <= 0) {
                        Toast.makeText(mainActivity, "一局只能使用两次炸弹哦～！", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        bomb(itemList.get(position).getId());
                        Log.d("onclick", "return");
                        leftBomb--;
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
        sound = MediaPlayer.create(getActivity(), R.raw.bomb);
        sound.start();
        linkGameAdapter.setShuffle(false);

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getId() == id) {
                itemList.get(i).setEliminated(true);
                int rowOne = i / COLUMN;
                int columnOne = i - rowOne * COLUMN;
//                map[rowOne][columnOne] = 0;
                gameEngine.eliminate(rowOne, columnOne);
                linkGameAdapter.notifyItemChanged(i);
            }
        }
        judgeGameOver();
    }

    @Override
    public void onResume() {
        super.onResume();
        bgMusic = MediaPlayer.create(getActivity(), R.raw.background);
        float volume = (float) 0.3;
        bgMusic.setVolume(volume, volume);
        bgMusic.start();
        bgMusic.setLooping(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            bgMusic.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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

        if (itemList.get(first).getId() == itemList.get(second).getId() && gameEngine.linkAble(new Point(rowOne, columnOne), new Point(rowTwo, columnTwo))) {
            itemList.get(first).setEliminated(true);
            itemList.get(second).setEliminated(true);

           /* map[rowOne][columnOne] = 0;
            map[rowTwo][columnTwo] = 0;*/
            gameEngine.eliminate(rowOne, columnOne);
            gameEngine.eliminate(rowTwo, columnTwo);

            gameEngine.printMap();

            sound = MediaPlayer.create(getActivity(), R.raw.eliminate);
            sound.start();
            judgeGameOver();
        } else {
            itemList.get(first).setSelect(false);
            itemList.get(second).setSelect(false);
            sound = MediaPlayer.create(getActivity(), R.raw.again);
            sound.start();
        }
        linkGameAdapter.setShuffle(false);
        linkGameAdapter.notifyItemChanged(first);
        linkGameAdapter.notifyItemChanged(second);
    }

    private void judgeGameOver() {
        if (gameEngine.isGameOver()) {
            endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;

//            Toast.makeText(getActivity(), "一局连连看结束～！ 用时：" + (duration / 1000 + "秒"), Toast.LENGTH_SHORT).show();
            bgMusic.pause();

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
                            Ranking ranking = new Ranking(userName, duration, getActivity().getSharedPreferences("linkgame", Context.MODE_PRIVATE).getInt("rank", 1), getTime());
                            App.getDaoSession().getRankingDao().insertOrReplace(ranking);
                            mainActivity.showRankingFragment();
                        }
                    }).show();

        }
    }

    private String getTime() {
        String pattern = "yyyy-MM-dd HH:mm";
        return new SimpleDateFormat(pattern).format(new Date());
    }

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

    private void resetLinkGameMatrix() {
        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            int row = i / COLUMN;
            int column = i - row * COLUMN;
            if (itemList.get(i).isEliminated()) {
                gameEngine.eliminate(row, column);
//                map[row][column] = 0;
            } else {
                gameEngine.set(row, column);
//                map[row][column] = 1;
            }
        }
    }


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
