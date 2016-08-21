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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Player service to play the random mp3 files.
 */
public class PlayerService extends Service implements OnCompletionListener, OnPreparedListener,
        OnErrorListener {
    public static final String ACTION_PLAY = "com.goforer.musicplayerwidget.action.PLAY";
    public static final String ACTION_PAUSE = "com.goforer.musicplayerwidget.action.PAUSE";
    public static final String ACTION_STOP = "com.goforer.musicplayerwidget.action.STOP";

    private static final  String TAG = "PlayerService";

    private final int NOTIFICATION_ID = 1;

    private enum State { Paused, Playing, Preparing, Stopped }

    private State mState = State.Preparing;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder = null;

    private MediaPlayer mPlayer = null;

    private File mFile;

    @Override
    public void onCreate() {
        /**
         * A foreground service must provide a notification for the status bar, which is placed
         * under the "Ongoing" heading, which means that the notification cannot be dismissed
         * unless the service is either stopped or removed from the foreground.
         */
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                try {
                    play();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_PAUSE:
                pause();
                break;
            case ACTION_STOP:
                stop();
                break;
            default:
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mState = State.Stopped;
        releaseMediaPlayer();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if (mState == State.Playing) {
            mState = State.Stopped;
            createPlayer();
        }

        mFile = getRandomMusicFile(Environment.getExternalStorageDirectory());
        if (mFile == null) {
            try {
                showMessage(getString(R.string.player_no_file));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        mState = State.Playing;
        if (!mPlayer.isPlaying()) {
            updatePlayerTitle(PlayerAppWidget.ACTION_TITLE_CHANGE, getSongFileName(mFile),
                    getSongAlbumTitle(mFile));
            mPlayer.start();
        }

        updateNotification(getSongFileName(mFile) + " : "
                + getResources().getString(R.string.button_playing));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        try {
            showMessage(getString(R.string.player_error_message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
        mState = State.Stopped;
        releaseMediaPlayer();

        return true;
    }

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get a randomly selected song file from list
     *
     * @param files the song list
     * @return A randomly selected song file
     */
    private File getRandomFile(@NonNull ArrayList<File> files) {
        Random mRandomGenerator = new Random();

        return files.get(mRandomGenerator.nextInt(files.size()));
    }

    /**
     * Get all mp3 files from specified directory.
     *
     * @param root the specified directory
     * @return  All mp3 files
     */
    private ArrayList<File> findMP3Files(@NonNull final File root) {
        ArrayList<File> fileList = new ArrayList<>();
        File[] files = root.listFiles();
        for(File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                fileList.addAll(findMP3Files(file));
            } else {
                if (file.getName().endsWith(".mp3")) {
                    fileList.add(file);
                }
            }
        }

        return  fileList;
    }

    /**
     * Get a song file from the specified directory
     *
     * @param root the specified directory
     *
     * @return A randomly selected song file
     */
    private File getRandomMusicFile(@NonNull final File root) {
        ArrayList<File> mFileList = findMP3Files(root);
        if (mFileList == null || mFileList.size() == 0) {
            return null;
        } else {
            return getRandomFile(mFileList);
        }
    }

    /**
     * Update the current MPlayer Widget state.
     *
     * @param action the action to update MPlayer Widget
     * @param state the current MPlayer state
     */
    private void updatePlayerState(String action, int state) {
        Intent intent = new Intent(action);
        intent.putExtra(PlayerAppWidget.EXTRA_PLAYER_STATE, state);
        sendBroadcast(intent);
    }

    /**
     *  Update the song file name and album title of MPlayer Widget whenever new song is played.
     *
     * @param action the action to update MPlayer Widget
     * @param fileName  the playing song's file name
     * @param albumTitle the playing song's album tile
     */
    private void updatePlayerTitle(String action, String fileName, String albumTitle) {
        Intent intent = new Intent(action);
        intent.putExtra(PlayerAppWidget.EXTRA_SONG_FILE_NAME, fileName);
        intent.putExtra(PlayerAppWidget.EXTRA_SONG_ALBUM_TITLE, albumTitle);
        sendBroadcast(intent);
    }

    /**
     * Post a notification to be shown in the status bar whenever the new song is played.
     *
     * @param fileName the song's file name to be show in the status bar
     */
    private void updateNotification(String fileName) {
        mNotificationBuilder.setContentText(fileName);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Make this service run in the foreground, supplying the ongoing notification to be shown
     * to the user while in this state.
     * By default services are background, meaning that if the system needs to kill them to reclaim
     * more memory (such as to display a large page in a web browser), they can be killed without
     * too much harm.
     * You can set this flag if killing your service would be disruptive to the user, such as if
     * your service is performing background music playback, so the user would notice if their music
     * stopped playing.
     * <p>
     * A foreground service must provide a notification for the status bar, which is placed
     * under the "Ongoing" heading, which means that the notification cannot be dismissed
     * unless the service is either stopped or removed from the foreground.
     * </p>
     *
     * @param fileName the file of current playing song
     */
    private void setForeground(String fileName) {
        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_player)
                .setTicker(fileName)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(fileName)
                .setOngoing(true);
        /**
         * Notice : When I carried out the instrumented unit test for Service,
         * I commented below code(the line) colling startForeground(true).
         * Please block comment below code(the line) whenever you run the instrumented unit test for Service.
         */
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void play() throws IOException {
        if (mState == State.Preparing || mState == State.Stopped) {
            mFile = getRandomMusicFile(Environment.getExternalStorageDirectory());
            if (mFile == null) {
                try {
                    showMessage(getString(R.string.player_no_file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            } else {
                try {
                    prepare(mFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (mState == State.Paused) {
            mState = State.Playing;
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
        }

        /**
         * A music player that plays music from a service should be set to run in the foreground,
         * because the user is explicitly aware of its operation.
         */
        setForeground(getSongFileName(mFile));

        updatePlayerState(PlayerAppWidget.ACTION_STATE_CHANGE, PlayerAppWidget.PLAYER_STATE_PLAY);
    }

    /**
     * Prepares the player for playback, asynchronously.
     *
     * <p>
     * Sets the audio stream type & data source
     * </p>
     *
     * @param path the song's file path
     * @throws IllegalStateException if it is called in an invalid state
     */
    private void prepare(String path) throws IOException, IllegalStateException {
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(path);
        /**
         * Notice : When I carried out the instrumented unit test for Service,
         * I commented below code(the line) colling stopForeground(true).
         * Please comment below code(the line( whenever you run the instrumented unit test for Service.
         */
        stopForeground(true);
        mPlayer.prepareAsync();
    }

    private void createPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setVolume(3.0f, 3.0f);
        }
        else {
            mPlayer.reset();
        }
    }

    private void pause() {
        if (mState == State.Playing) {
            mState = State.Paused;
            updatePlayerState(PlayerAppWidget.ACTION_STATE_CHANGE,
                    PlayerAppWidget.PLAYER_STATE_PAUSE);
            mPlayer.pause();
            /**
             * Notice : When I carried out the instrumented unit test for Service,
             * I commented below code(the line) colling stopForeground(true).
             * Please comment below code(the line( whenever you run the instrumented unit test for Service.
             */
            stopForeground(true);
        }
    }

    private void stop() {
        if (mState == State.Playing || mState == State.Paused) {
            mState = State.Stopped;
            updatePlayerState(PlayerAppWidget.ACTION_STATE_CHANGE,
                    PlayerAppWidget.PLAYER_STATE_STOP);
            mPlayer.stop();
        }

        releaseMediaPlayer();
        stopSelf();
    }

    /**
     * Release the using MediaPlayer.
     */
    private void releaseMediaPlayer() {
        /**
         * Notice : When I carried out the instrumented unit test for Service,
         * I commented below code(the line) colling stopForeground(true).
         * Please comment below code(the line( whenever you run the instrumented unit test for Service.
         */
        stopForeground(true);

        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * Get the file name from the specified file.
     *
     * @param file the specified file
     *
     * @return the file name
     */
    private String getSongFileName(@NonNull File file) {
        return file.getName().replace(".mp3", "");
    }

    /**
     * Get the album tile from the specified file.
     *
     * @param file the specified file
     *
     * @return the album title
     */
    private String getSongAlbumTitle(@NonNull File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getAbsolutePath());
        try {
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).trim();
            if (title.isEmpty() || title.equals("")) {
                title = file.getName().replace(".mp3", "");
            }

            return title;
        } catch (Exception e) {
            return getResources().getString(R.string.player_no_album_title);
        }
    }
}

