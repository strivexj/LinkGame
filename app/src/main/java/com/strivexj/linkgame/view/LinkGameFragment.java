package com.strivexj.linkgame.view;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.strivexj.linkgame.App;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.SharedPerferencesUtil;
import com.strivexj.linkgame.adapter.LinkGameAdapter;
import com.strivexj.linkgame.base.BaseHolder;
import com.strivexj.linkgame.base.OnItemClickListener;
import com.strivexj.linkgame.bean.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.explosionfield.ExplosionField;


public class LinkGameFragment extends Fragment implements LinkGameContract.View {
    private RecyclerView recyclerview;
    private LinkGameAdapter linkGameAdapter;
    private List<Item> itemList = new ArrayList<>();
    private int row = 9, column = 8;
    private MediaPlayer bgMusic;
    private MediaPlayer sound;
    private ImageView bomb;
    //    private ImageView home;
    private DrawView drawView;
    private MainActivity mainActivity;
    private boolean isBomb = false;
    private LinkGameContract.Presenter presenter;
    private int lastClick = -1;
    private int leftShuffle = 3;
    private int leftBomb = 2;

    private TextView leftTime;
    private int left = 150;
    private Timer timer;

    public LinkGameFragment() {
    }

    public static LinkGameFragment newInstance() {
        LinkGameFragment fragment = new LinkGameFragment();
        return fragment;
    }

    @Override
    public MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    public void startGame() {
        lastClick = -1;
        leftShuffle = 3;
        leftBomb = 2;
        left = 150;
        presenter.startGame();
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leftTime.setText(left / 60 + "分" + left % 60 + "秒");
                            if (left <= 0) {
                                timer.cancel();
                                new MaterialDialog.Builder(getActivity())
                                        .title("时间到了")
                                        .content("你没有通过本次游戏哦")
                                        .cancelable(false)
                                        .positiveText("再来一盘")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                startGame();
                                            }
                                        }).show();
                            }
                            left--;
                        }
                    });
                } catch (NullPointerException e) {
                    timer.cancel();
                    e.printStackTrace();
                }

            }
        }, 0, 1000);
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
        startGame();
    }

    @Override
    public void init(@NonNull View view) {
        recyclerview = view.findViewById(R.id.recyclerview);
        bomb = view.findViewById(R.id.bomb);
//        home = view.findViewById(R.id.home);
        drawView = view.findViewById(R.id.line);
//        home.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mainActivity.showMainFragment();
//            }
//        });
        leftTime = view.findViewById(R.id.left_time);
        bomb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBomb = true;
            }
        });

        linkGameAdapter = new LinkGameAdapter(getActivity(), itemList, ExplosionField.attach2Window(getActivity()));
        recyclerview.setLayoutManager(new GridLayoutManager(getContext(), column));
        recyclerview.setAdapter(linkGameAdapter);
        linkGameAdapter.setmOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                LinkGameFragment.this.onItemClick(position);
            }
        });
        presenter = new LinkGamePresenter(row, column, this, linkGameAdapter);
    }

    private void onItemClick(int position) {
        Item item = itemList.get(position);
        if (item.isEliminated()) return;

        item.setSelect(!item.isSelect());
        linkGameAdapter.setShuffle(false);
        linkGameAdapter.notifyItemChanged(position);

        if (isBomb) {
            bomb.setClickable(false);
            isBomb = false;
            if (leftBomb <= 0) {
                Toast.makeText(App.getContext(), "一局只能使用两次炸弹哦～！", Toast.LENGTH_LONG).show();
            } else {
                leftBomb--;
                presenter.bomb(itemList.get(position).getId());
            }
            bomb.setClickable(true);
            lastClick = -1;
            return;
        }

        if (lastClick != -1) {
            presenter.eliminable(lastClick, position);
            lastClick = -1;
        } else {
            lastClick = position;
        }


        //隐藏底部和顶部
        mainActivity.showBottomAndToolbar(false);
    }

    @Override
    public void drawLine(List<Point> turnPoints) {
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
            if (x >= row) {
                x = row - 1;
                point.y += 70;
            }
            if (y >= column) {
                y = column - 1;
                point.x += 70;
            }

            int position = x * column + y;
            Log.d("point", "x:" + x + " y:" + y + " p:" + position);

            if (linkGameAdapter.getItemCount() > 0) {
                RecyclerView.ViewHolder holder = recyclerview.findViewHolderForAdapterPosition(position);
                if (holder != null && holder instanceof BaseHolder) {
                    BaseHolder viewHolder = (BaseHolder) holder;
                    int[] location = new int[2];
                    viewHolder.itemView.getLocationOnScreen(location);
                    point.x += location[0] + 50;
                    point.y += location[1] + 50;
                    printPoints.add(point);
                    Log.d("point", "第" + i + "个  x坐标：" + location[0] + " y坐标：" + location[1]);
                }
            }
            drawView.drawLine(printPoints);
        }
    }

    @Override
    public void updateView(List<Item> itemList) {
        this.itemList = itemList;
        linkGameAdapter.setList(itemList);
        linkGameAdapter.notifyDataSetChanged();
    }

    @Override
    public void select(int position) {
        itemList.get(position).setSelect(!itemList.get(position).isSelect());
        linkGameAdapter.setShuffle(false);
        linkGameAdapter.notifyItemChanged(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        playBgMusic();
    }

    @Override
    public void playBgMusic() {
        if (!SharedPerferencesUtil.isMusic()) return;
        bgMusic = MediaPlayer.create(getActivity(), R.raw.background);
        float volume = (float) 0.3;
        bgMusic.setVolume(volume, volume);
        bgMusic.start();
        bgMusic.setLooping(true);
    }

    @Override
    public void playSound(int bomb) {
        if (!SharedPerferencesUtil.isMusic()) return;
        sound = MediaPlayer.create(getActivity(), bomb);
        sound.start();
    }

    @Override
    public void stopBgMusic() {
        try {
            bgMusic.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopBgMusic();
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
        presenter.resetLinkGameMatrix();
        if (linkGameAdapter != null) {
            linkGameAdapter.setShuffle(true);
            linkGameAdapter.notifyDataSetChanged();
        }
        leftShuffle--;
    }

}
