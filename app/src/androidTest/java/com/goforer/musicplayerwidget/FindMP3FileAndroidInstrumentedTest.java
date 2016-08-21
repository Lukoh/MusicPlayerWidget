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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.test.ActivityInstrumentationTestCase2;

import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class FindMP3FileAndroidInstrumentedTest
        extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public FindMP3FileAndroidInstrumentedTest(){
        super(MainActivity.class);
    }

    private static void allowStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED
                || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private ArrayList<File> findMP3Files(@NonNull File root) {
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

    private File getRandomFile(@NonNull ArrayList<File> files) {
        Random mRandomGenerator = new Random();

        return files.get(mRandomGenerator.nextInt(files.size()));
    }

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
            return "";
        }
    }

    @TestOnly
    public void testGetResponse() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);

        if (Build.VERSION.SDK_INT >= 23) {
            allowStoragePermissions(getActivity());
        }

        /* The testing thread will wait here until the UI thread releases it
	     * above with the countDown() or 30 seconds passes and it times out.
	     */
        signal.await(10, TimeUnit.SECONDS);

        ArrayList<File> mFileList = findMP3Files(Environment.getExternalStorageDirectory());

        if (mFileList != null && mFileList.size() >= 0) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }

        if (mFileList.size() > 0) {
            File file = getRandomFile(mFileList);

            assertNotNull(file);

            if (file != null) {
                String albumTitle = getSongAlbumTitle(file);

                if (albumTitle.equals("")) {
                    assertTrue(false);
                } else {
                    assertTrue(true);
                }
            }
        }
    }
}
