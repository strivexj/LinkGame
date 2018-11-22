package com.strivexj.linkgame;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinkGameFragment linkGameFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("LinkGame");
        linkGameFragment = LinkGameFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, linkGameFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (linkGameFragment == null || !linkGameFragment.isPlaying())
            return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.shuffle:
                linkGameFragment.shuffle();
                break;
            case R.id.easy:
                linkGameFragment.loadData(LinkGameFragment.EASY);
                //记录用户选择难度
                getSharedPreferences("linkgame", MODE_PRIVATE).edit().putInt("rank", LinkGameFragment.EASY).apply();
                break;
            case R.id.medium:
                linkGameFragment.loadData(LinkGameFragment.MEDIUM);
                getSharedPreferences("linkgame", MODE_PRIVATE).edit().putInt("rank", LinkGameFragment.MEDIUM).apply();
                break;
            case R.id.diffculty:
                linkGameFragment.loadData(LinkGameFragment.DIFFICULTY);
                getSharedPreferences("linkgame", MODE_PRIVATE).edit().putInt("rank", LinkGameFragment.DIFFICULTY).apply();
                break;
            case R.id.newGame:
                linkGameFragment.loadData(getSharedPreferences("linkgame", Context.MODE_PRIVATE).getInt("rank", 1));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int rank = getSharedPreferences("linkgame", Context.MODE_PRIVATE).getInt("rank", 1);
        switch (rank) {
            case LinkGameFragment.EASY:
                menu.findItem(R.id.easy).setChecked(true);
                break;
            case LinkGameFragment.MEDIUM:
                menu.findItem(R.id.medium).setChecked(true);
                break;
            case LinkGameFragment.DIFFICULTY:
                menu.findItem(R.id.diffculty).setChecked(true);
                break;

        }

        return super.onPrepareOptionsMenu(menu);
    }
}
