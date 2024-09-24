package com.example.holymoly;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;

    public MusicService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        // 초기 배경음악 설정 (필요에 따라 수정 가능)
        mediaPlayer = MediaPlayer.create(this, R.raw.ocean_theme_music);
        mediaPlayer.setLooping(true); // 반복 재생
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("CHANGE_MUSIC".equals(action)) {
            // 인텐트에서 전달된 리소스 ID로 음악을 변경
            int musicResId = intent.getIntExtra("MUSIC_RES_ID", R.raw.ocean_theme_music);
            changeMusic(musicResId);
        } else {
            mediaPlayer.start(); // 음악 재생
        }

        return START_NOT_STICKY;
    }

    // 새로운 배경음악으로 변경하는 메서드
    private void changeMusic(int musicResId) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, musicResId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}