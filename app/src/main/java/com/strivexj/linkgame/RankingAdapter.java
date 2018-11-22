package com.strivexj.linkgame;

import android.content.Context;

import com.strivexj.linkgame.base.BaseHolder;
import com.strivexj.linkgame.base.BaseRecyclerviewAdapter;

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
        holder.setText(R.id.id, object.getId() + "");
        holder.setText(R.id.userName, object.getUserName());
        holder.setText(R.id.record, object.getRecord() / 1000 + "s");
        holder.setText(R.id.date, object.getDate());
        String type = "easy";
        switch (object.getType()) {
            case LinkGameFragment.EASY:
                type = "easy";
                break;
            case LinkGameFragment.MEDIUM:
                type = "medium";
                break;
            case LinkGameFragment.DIFFICULTY:
                type = "difficuly";
                break;
        }
        holder.setText(R.id.type, type);
    }
}
