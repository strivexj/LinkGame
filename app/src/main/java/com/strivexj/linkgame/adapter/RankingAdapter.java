package com.strivexj.linkgame.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.strivexj.linkgame.LinkGameFragment;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.base.BaseHolder;
import com.strivexj.linkgame.base.BaseRecyclerviewAdapter;
import com.strivexj.linkgame.bean.Ranking;

import java.util.List;

/**
 * Created by cwj on 11/22/18 20:41
 */
public class RankingAdapter extends BaseRecyclerviewAdapter<Ranking> {
    public RankingAdapter(Context context, List<Ranking> list) {
        super(context, list);
    }

    @Override
    public int getContentView(int viewType) {
        return R.layout.item_ranking;
    }

    @Override
    public void onInitView(BaseHolder holder, Ranking object, int position) {
        holder.setText(R.id.id, position + 1 + "");

        if (position == 0) {
            holder.getView(R.id.image).setVisibility(View.VISIBLE);
            holder.getView(R.id.id).setVisibility(View.GONE);
            ((ImageView)holder.getView(R.id.image)).setImageResource(R.drawable.first);
        }else {
            holder.getView(R.id.image).setVisibility(View.GONE);
            holder.getView(R.id.id).setVisibility(View.VISIBLE);
        }
        holder.setText(R.id.userName, object.getUsername());
        holder.setText(R.id.record, object.getRecord() + "s");
        holder.setText(R.id.date, object.getDate());
        String type = "Easy";
        switch (object.getType()) {
            case LinkGameFragment.EASY:
                type = "Easy";
                break;
            case LinkGameFragment.MEDIUM:
                type = "Medium";
                break;
            case LinkGameFragment.DIFFICULTY:
                type = "Difficulty";
                break;
        }
        holder.setText(R.id.type, type);
    }
}
