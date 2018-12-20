package com.strivexj.linkgame;

import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cwj on 12/20/18 20:18
 */
public class SharedPerferencesUtil {
    private static SharedPreferences linkGame_pref = App.getContext().getSharedPreferences("linkGame", MODE_PRIVATE);
    private static SharedPreferences.Editor linkGame_editor = App.getContext().getSharedPreferences("linkGame", MODE_PRIVATE).edit();

    public static int getRank() {
        return linkGame_pref.getInt("rank", 1);
    }

    public static void setRank(int rank) {
        linkGame_editor.putInt("rank", rank);
        linkGame_editor.apply();
    }

    public static boolean isMusic() {
        return linkGame_pref.getBoolean("music", true);
    }

    public static void setMusic(boolean music) {
        linkGame_editor.putBoolean("music", music);
        linkGame_editor.apply();
    }

    public  static String getUsername() {
        return linkGame_pref.getString("username", null);
    }

    public static void setUsername(String username) {
        linkGame_editor.putString("username", username);
        linkGame_editor.apply();
    }
}
