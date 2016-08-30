/*
 * Copyright (C) 2016 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.musicplayerwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * MPlayer App Widget to play the random mp3 files.
 */
public class PlayerAppWidget extends AppWidgetProvider {
    public static final int PLAYER_STATE_PLAY = 10000;
    public static final int PLAYER_STATE_PAUSE = 10001;
    public static final int PLAYER_STATE_STOP = 10002;

    public static final String ACTION_STATE_CHANGE
            = "com.goforer.musicplayerwidget.action.STATE_CHANGE";
    public static final String ACTION_TITLE_CHANGE
            = "com.goforer.musicplayerwidget.action.TITLE_CHANGE";

    public static final String EXTRA_PLAYER_STATE
            = "com.goforer.musicplayerwidget.extra:control_state";
    public static final String EXTRA_SONG_FILE_NAME
            = "com.goforer.musicplayerwidget.extra:file_name";
    public static final String EXTRA_SONG_ALBUM_TITLE
            = "com.goforer.musicplayerwidget.extra:album_title";

    private static final int INTENT_FLAGS = 0;
    private static final int REQUEST_CODE = 0;

    private static int[] mAppWidgetIds;
    private static RemoteViews mViews;
    private static AppWidgetManager mAppWidgetManager;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        mViews = new RemoteViews(context.getPackageName(), R.layout.player_app_widget);

        mAppWidgetManager = appWidgetManager;

        Intent playIntent = new Intent(PlayerService.ACTION_PLAY);
        Intent pauseIntent = new Intent(PlayerService.ACTION_PAUSE);
        Intent stopIntent = new Intent(PlayerService.ACTION_STOP);

        PendingIntent playPendingIntent = PendingIntent.getService(
                context, REQUEST_CODE, playIntent, INTENT_FLAGS);
        PendingIntent pausePendingIntent = PendingIntent.getService(
                context, REQUEST_CODE, pauseIntent, INTENT_FLAGS);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                context, REQUEST_CODE, stopIntent, INTENT_FLAGS);

        mViews.setOnClickPendingIntent(
                R.id.btn_play, playPendingIntent);
        mViews.setOnClickPendingIntent(
                R.id.btn_pause, pausePendingIntent);
        mViews.setOnClickPendingIntent(
                R.id.btn_stop, stopPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, mViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mAppWidgetIds = appWidgetIds;
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (mViews == null) {
            mViews = new RemoteViews(context.getPackageName(), R.layout.player_app_widget);
        }

        if (mAppWidgetManager == null) {
            mAppWidgetManager = AppWidgetManager.getInstance(context);
            final ComponentName componentName = new ComponentName(context,
                    PlayerAppWidget.class);
            mAppWidgetIds = mAppWidgetManager.getAppWidgetIds(componentName);
        }

        if (ACTION_STATE_CHANGE.equals(action)) {
            updatePlayerState(intent.getIntExtra(EXTRA_PLAYER_STATE, 0));
        } else if (ACTION_TITLE_CHANGE.equals(action)) {
            updateTitle(intent.getStringExtra(EXTRA_SONG_FILE_NAME),
                    intent.getStringExtra(EXTRA_SONG_ALBUM_TITLE));
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    /**
     * Update the song file name and album title of MPlayer Widget whenever new song is played.
     *
     * @param fileName the playing song's file name
     * @param albumTitle the playing song's album tile
     */
    private void updateTitle(String fileName, String albumTitle) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : mAppWidgetIds) {
            mViews.setTextViewText(R.id.tv_file_name, fileName);
            mViews.setTextViewText(R.id.tv_album_name, albumTitle);
            mAppWidgetManager.updateAppWidget(appWidgetId, mViews);
        }
    }

    /**
     * Update the current MPlayer Widget state.
     *
     * @param state the current MPlayer state
     *               : State -  {PLAYER_STATE_PLAY, PLAYER_STATE_PAUSE, PLAYER_STATE_STOP}
     */
    private void updatePlayerState(int state) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : mAppWidgetIds) {
            switch(state) {
                case PLAYER_STATE_PLAY:
                    mViews.setInt(R.id.btn_play, "setBackgroundResource", R.color.colorSelectedButton);
                    mViews.setInt(R.id.btn_pause, "setBackgroundResource", R.color.colorButton);
                    mViews.setInt(R.id.btn_stop, "setBackgroundResource", R.color.colorButton);
                    break;
                case PLAYER_STATE_PAUSE:
                    mViews.setInt(R.id.btn_play, "setBackgroundResource", R.color.colorButton);
                    mViews.setInt(R.id.btn_pause, "setBackgroundResource", R.color.colorSelectedButton);
                    mViews.setInt(R.id.btn_stop, "setBackgroundResource", R.color.colorButton);
                    break;
                case PLAYER_STATE_STOP:
                    mViews.setInt(R.id.btn_play, "setBackgroundResource", R.color.colorButton);
                    mViews.setInt(R.id.btn_pause, "setBackgroundResource", R.color.colorButton);
                    mViews.setInt(R.id.btn_stop, "setBackgroundResource", R.color.colorSelectedButton);
                    break;
                default:
                    break;
            }

            mAppWidgetManager.updateAppWidget(appWidgetId, mViews);
        }
    }
}

