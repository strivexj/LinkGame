package com.strivexj.linkgame.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.base.BaseHolder;
import com.strivexj.linkgame.base.BaseRecyclerviewAdapter;
import com.strivexj.linkgame.bean.Item;

import java.io.IOException;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

/**
 * Created by cwj on 11/20/18 19:02
 */
public class LinkGameAdapter extends BaseRecyclerviewAdapter<Item> {
    private ExplosionField explosionField;
    private boolean shuffle = false;

    public LinkGameAdapter(Context context, List<Item> list, ExplosionField explosionField) {
        super(context, list);
        this.explosionField = explosionField;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public int getContentView(int viewType) {
        return R.layout.item_link_game;
    }

    @Override
    public void onInitView(final BaseHolder holder, Item object, int position) {
        Log.d("item info", "select:" + object.isSelect() + " eliminate:" + object.isEliminated());
        if (object.isSelect()) {
            holder.getView(R.id.select).setVisibility(View.VISIBLE);
        } else {
            holder.getView(R.id.select).setVisibility(View.INVISIBLE);
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets()
                    .open("image/" + mList.get(position).getId() + ".png"));

            ImageView imageView = ((ImageView) holder.getView(R.id.image));
//                imageView.setImageBitmap(bitmap);
            Glide.with(context).load(bitmap).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (object.isEliminated()) {
            final ImageView imageView = ((ImageView) holder.getView(R.id.image));
            holder.getView(R.id.select).setVisibility(View.INVISIBLE);
            if (!shuffle) {
                Log.d("item info", "explode");
                imageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        explosionField.explode(imageView);
                        imageView.setVisibility(View.INVISIBLE);
                    }
                }, 15);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        } /*else {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open("image/" + mList.get(position).getId() + ".png"));
                ImageView imageView = ((ImageView) holder.getView(R.id.image));
//                imageView.setImageBitmap(bitmap);
                Glide.with(context).load(bitmap).into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}
