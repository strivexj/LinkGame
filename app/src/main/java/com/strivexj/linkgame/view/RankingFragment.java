package com.strivexj.linkgame.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strivexj.linkgame.App;
import com.strivexj.linkgame.MyApi;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.adapter.RankingAdapter;
import com.strivexj.linkgame.bean.Ranking;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cwj on 11/22/18 19:20
 */
public class RankingFragment extends Fragment {
    private TextView myRecord;
    private TextView title;
    private TextView name;
    private MainActivity mainActivity;
//    private ImageView home;
    private RecyclerView recyclerView;
    private RankingAdapter rankingAdapter;
    private ProgressBar progressBar;
    private List<Ranking> rankingList;
    private boolean showLocalRecord = false;

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
        name = view.findViewById(R.id.userName);
        String username = getActivity().getSharedPreferences("userName", MODE_PRIVATE).getString("userName", null);
        if (TextUtils.isEmpty(username)) {
            inputUsername();
        } else {
            name.setText(username);
        }
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUsername();
            }
        });

       /* home = view.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showMainFragment();
            }
        });*/
        myRecord = view.findViewById(R.id.myRecord);
        title = view.findViewById(R.id.title);

        progressBar = view.findViewById(R.id.progress_circular);

        myRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocalRecord = !showLocalRecord;
                if (showLocalRecord) {
                    rankingList = App.getDaoSession().getRankingDao().loadAll();
                    Collections.sort(rankingList, new Comparator<Ranking>() {
                        @Override
                        public int compare(Ranking o1, Ranking o2) {
                            return (int) (o1.getRecord() - o2.getRecord());
                        }
                    });
                    rankingAdapter.setList(rankingList);
                    rankingAdapter.notifyDataSetChanged();

                  /*  boolean one = false, two = false, three = false;
                    for (int i = 0; i < rankingList.size(); i++) {
                        Ranking ranking = rankingList.get(i);
                        if (!one && ranking.getType() == EASY) {
                            one = true;
                        }
                        if (!two && ranking.getType() == MEDIUM) {
                            two = true;
                        }
                        if (!three && ranking.getType() == DIFFICULTY) {
                            two = true;
                        }
                        if (one && two && three) break;

                        uploadMyRecord(ranking.getUsername(), ranking.getType(), ranking.getRecord(), ranking.getDate());
                    }*/
                    title.setText("My Record");
                    myRecord.setText("Ranking");
                } else {
                    getRankingList();

                }
            }
        });


        recyclerView = view.findViewById(R.id.recyclerview);
        rankingAdapter = new RankingAdapter(getActivity(), new ArrayList<Ranking>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(rankingAdapter);

        getRankingList();
    }

    private void inputUsername() {
        new MaterialDialog.Builder(getActivity())
                .title("Please input your username~")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .cancelable(false)
                .input(0, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String userName = input.toString().trim();
                        if (TextUtils.isEmpty(userName)) {
                            Toast.makeText(mainActivity, "Username can no be empty!", Toast.LENGTH_SHORT).show();
                            inputUsername();
                        } else {
                            getActivity().getSharedPreferences("userName", MODE_PRIVATE).edit().putString("userName", userName).apply();
                            name.setText(userName);
                        }
                    }
                }).show();
    }

    private void getRankingList() {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyApi.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(MyApi.class).getRankingList().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Ranking>>() {
                    }.getType();
                    List<Ranking> rankingList = gson.fromJson(json, type);
                    rankingAdapter.setList(rankingList);
                    rankingAdapter.notifyDataSetChanged();
                    Log.d("getRankingList", json);

                    title.setText("Ranking");
                    myRecord.setText("My Record");
                    progressBar.setVisibility(View.GONE);
                } catch (IOException e) {
                    Log.d("getRankingList", "decode failed");
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(mainActivity, "Failed to load, please check your network connection~", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("getRankingList", "failed");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mainActivity, "Failed to load, please check your network connection~", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
