package com.goforer.musicplayerwidget;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class PlayerServiceTest extends ServiceTestCase<PlayerService> {

    public PlayerServiceTest()
    {
        super(PlayerService.class);
    }

    @SmallTest
    public void testPlay() {
        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_PLAY);

        startService(intent);
    }

    @SmallTest
    public void testPause() {
        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_PAUSE);

        startService(intent);
    }

    @SmallTest
    public void testStop() {
        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_STOP);

        startService(intent);
    }
}
