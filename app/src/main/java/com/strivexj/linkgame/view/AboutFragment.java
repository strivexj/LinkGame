package com.strivexj.linkgame.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.strivexj.linkgame.R;

/**
 * Created by cwj on 11/22/18 19:20
 */
public class AboutFragment extends Fragment {
    private TextView textView;
    private MainActivity mainActivity;
    private ImageView home;

    public AboutFragment() {
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
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
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView = view.findViewById(R.id.about_content);
       /* home = view.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showMainFragment();
            }
        });*/
        StringBuilder sb = new StringBuilder();
        sb.append("连连看\n");
        sb.append("《连连看》只要将相同的两张牌用三根以内的直线连在一起就可以消除，规则简单容易上手。\n\n");
        sb.append("版本：V1.0.0\n");
        sb.append("作者：成文杰\n");
        sb.append("学号：201613161124\n");
        sb.append("班级：计科三班\n");
        textView.setText(sb.toString());
    }
}
