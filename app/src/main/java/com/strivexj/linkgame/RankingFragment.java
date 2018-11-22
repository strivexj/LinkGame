package com.strivexj.linkgame;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by cwj on 11/22/18 19:20
 */
public class RankingFragment extends Fragment {
    private TextView textView;
    private MainActivity mainActivity;
    private ImageView home;
    private RecyclerView recyclerView;
    private RankingAdapter rankingAdapter;
    private List<Ranking> rankingList;

    public RankingFragment() {
    }

    public static RankingFragment newInstance() {
        RankingFragment fragment = new RankingFragment();
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
        return inflater.inflate(R.layout.fragment_ranking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        home = view.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showMainFragment();
            }
        });


       /* for (int i = 0; i < 4; i++) {
            long record = System.currentTimeMillis();
            Ranking ranking = new Ranking("strivexj" + i, record, getTime());
            App.getDaoSession().getRankingDao().insertOrReplace(ranking);
        }*/

        recyclerView = view.findViewById(R.id.recyclerview);
        rankingList = App.getDaoSession().getRankingDao().loadAll();
        rankingAdapter = new RankingAdapter(getActivity(), rankingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(rankingAdapter);
    }

}
