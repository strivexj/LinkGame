package com.strivexj.linkgame.view;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.strivexj.linkgame.R;
import com.strivexj.linkgame.SharedPerferencesUtil;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinkGameFragment linkGameFragment = null;
    private MainFragment mainFragment = null;
    private AboutFragment aboutFragment = null;
    private RankingFragment rankingFragment = null;
    private Fragment mContent = null;

    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        }

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("LinkGame");


        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_ranking:
                        showRankingFragment();
                        return true;
                    case R.id.action_main:
                        showMainFragment();
                        return true;
                    case R.id.action_about:
                        showAboutFragment();
                        return true;
                }
                return false;
            }
        });
        if (mContent == null) {
            setDefaultFragment();
            navigationView.setSelectedItemId(R.id.action_main);
        }
    }

    private void setDefaultFragment() {
        if (mainFragment == null)
            mainFragment = MainFragment.newInstance();
        FragmentTransaction mFragmentTrans = getSupportFragmentManager().beginTransaction();
        mFragmentTrans.add(R.id.container, mainFragment).commit();
        mContent = mainFragment;
    }

    public void showMainFragment() {
        if (mainFragment == null)
            mainFragment = MainFragment.newInstance();
        switchContent(mainFragment);
    }

    public void showLinkGameFragment() {
        if (linkGameFragment == null)
            linkGameFragment = LinkGameFragment.newInstance();
        switchContent(linkGameFragment);
    }

    public void showAboutFragment() {
        if (aboutFragment == null)
            aboutFragment = AboutFragment.newInstance();
        switchContent(aboutFragment);
    }

    public void showRankingFragment() {
        if (rankingFragment == null)
            rankingFragment = RankingFragment.newInstance();
        switchContent(rankingFragment);
    }

    private void switchContent(Fragment to) {
        if (mContent != to) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, to).setTransition(TRANSIT_FRAGMENT_FADE).commit();
            mContent = to;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shuffle:
                if (mContent == linkGameFragment)
                    linkGameFragment.shuffle();
                break;
            case R.id.easy:
                SharedPerferencesUtil.setRank(LinkGamePresenter.EASY);
                if (mContent == linkGameFragment)
                    linkGameFragment.startGame();
                break;
            case R.id.medium:
                SharedPerferencesUtil.setRank(LinkGamePresenter.MEDIUM);
                if (mContent == linkGameFragment)
                    linkGameFragment.startGame();
                break;
            case R.id.diffculty:
                SharedPerferencesUtil.setRank(LinkGamePresenter.DIFFICULTY);
                if (mContent == linkGameFragment)
                    linkGameFragment.startGame();
                break;
            case R.id.newGame:
                if (mContent == linkGameFragment)
                    linkGameFragment.startGame();
                break;
            case R.id.about:
//                showAboutFragment();
                navigationView.setSelectedItemId(R.id.action_about);
                break;
            case R.id.ranking:
//                showRankingFragment();
                navigationView.setSelectedItemId(R.id.action_ranking);
                break;
            case R.id.music:
                item.setChecked(!item.isChecked());
                SharedPerferencesUtil.setMusic(item.isChecked());
                if (!item.isChecked()) {
                    if (mContent == linkGameFragment)
                        linkGameFragment.stopBgMusic();
                    else if (mContent == mainFragment) {
                        mainFragment.stopBgMusic();
                    }
                } else {
                    if (mContent == linkGameFragment)
                        linkGameFragment.playBgMusic();
                    else if (mContent == mainFragment) {
                        mainFragment.playBgMusic();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (SharedPerferencesUtil.getRank()) {
            case LinkGamePresenter.EASY:
                menu.findItem(R.id.easy).setChecked(true);
                break;
            case LinkGamePresenter.MEDIUM:
                menu.findItem(R.id.medium).setChecked(true);
                break;
            case LinkGamePresenter.DIFFICULTY:
                menu.findItem(R.id.diffculty).setChecked(true);
                break;
        }
        if (SharedPerferencesUtil.isMusic())
            menu.findItem(R.id.music).setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }
}
