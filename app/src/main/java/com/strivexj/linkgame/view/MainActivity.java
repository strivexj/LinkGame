package com.strivexj.linkgame.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.strivexj.linkgame.App;
import com.strivexj.linkgame.R;
import com.strivexj.linkgame.SharedPerferencesUtil;

import java.io.File;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final int CHOOSEICON = 1;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private LinkGameFragment linkGameFragment = null;
    private MainFragment mainFragment = null;
    private AboutFragment aboutFragment = null;
    private RankingFragment rankingFragment = null;
    private Fragment mContent = null;
    private BottomNavigationView navigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private boolean showBottomAndToolbar = true;
    private RelativeLayout nav_header;
    private TextView userName;
    private CircleImageView userIcon;

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
        actionBar = getSupportActionBar();
        toolbar.setTitle("LinkGame");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }

        drawerLayout = findViewById(R.id.drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

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

        View headerView = navView.getHeaderView(0);
        nav_header = headerView.findViewById(R.id.nav_header);
        userName = nav_header.findViewById(R.id.userName);
        userIcon = nav_header.findViewById(R.id.user_icon);
        if (!TextUtils.isEmpty(SharedPerferencesUtil.getUsername())) {
            userName.setText(SharedPerferencesUtil.getUsername());
        }
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUsername();
            }
        });
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPictureFromAblum(CHOOSEICON);
            }
        });
        File file = new File(App.getContext().getFilesDir().getAbsolutePath() + File.separator + "icon.jpg");
        if (file.exists()) {
            Glide.with(this).load(file).into(userIcon);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // 通过返回码判断是哪个应用返回的数据
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSEICON:
                    File file = new File(App.getContext().getFilesDir().getAbsolutePath() + File.separator + "icon.jpg");
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        Glide.with(this).load(bitmap).into(userIcon);
                        final FileOutputStream out1 = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out1);
                        out1.flush();
                        out1.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void pickPictureFromAblum(int code) {
        /**
         * 从相册选取
         */
        Intent choiceFromAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // 设置数据类型为图片类型
        choiceFromAlbumIntent.setType("image/*");
        startActivityForResult(choiceFromAlbumIntent, code);
    }

    private void inputUsername() {
        new MaterialDialog.Builder(this)
                .title("Please input your username~")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .cancelable(false)
                .input(0, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String username = input.toString().trim();
                        if (TextUtils.isEmpty(username)) {
                            Toast.makeText(MainActivity.this, "Username can no be empty!", Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPerferencesUtil.setUsername(username);
                            userName.setText(username);
                        }
                    }
                }).show();
    }

    public void showBottomAndToolbar(boolean show) {
        showBottomAndToolbar = show;
        if (show) {
            toolbar.setVisibility(View.VISIBLE);
            navigationView.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
            navigationView.setVisibility(View.GONE);
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
        if (mContent == linkGameFragment) {
            showBottomAndToolbar(false);
        } else {
            showBottomAndToolbar(true);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_ranking:
                navigationView.setSelectedItemId(R.id.action_ranking);
                break;
            case R.id.action_main:
                navigationView.setSelectedItemId(R.id.action_main);
                break;
            case R.id.action_about:
                navigationView.setSelectedItemId(R.id.action_about);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!showBottomAndToolbar) {
            showBottomAndToolbar(true);
        } else {
            super.onBackPressed();
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
