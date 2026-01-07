package com.example.booklibrary;

import android.content.Context;
import android.content.SharedPreferences;

public class GoalPrefs {
    private static final String PREFS_NAME = "reading_prefs";
    private static final String KEY_YEAR_GOAL = "year_goal";
    public static final int DEFAULT_GOAL = 50;

    public static int getYearGoal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_YEAR_GOAL, DEFAULT_GOAL);
    }

    public static void setYearGoal(Context context, int goal) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_YEAR_GOAL, goal).apply();
    }
}
