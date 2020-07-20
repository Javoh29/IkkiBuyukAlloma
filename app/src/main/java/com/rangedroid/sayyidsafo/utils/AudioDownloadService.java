package com.rangedroid.sayyidsafo.utils;

import android.app.Notification;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.rangedroid.sayyidsafo.R;


public class AudioDownloadService extends DownloadService {

    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";
    public static final int DOWNLOAD_NOTIFICATION_ID = 2;

    public AudioDownloadService() {
        super(
                DOWNLOAD_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_CHANNEL_ID,
                R.string.download_channel_name);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return DownloadUtil.getDownloadManager(this);
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @Override
    protected Notification getForegroundNotification(DownloadManager.TaskState[] taskStates) {
        return DownloadNotificationUtil.buildProgressNotification(
                this,
                R.drawable.splashlogo,
                DOWNLOAD_CHANNEL_ID,
                null,
                null,
                taskStates);
    }

}