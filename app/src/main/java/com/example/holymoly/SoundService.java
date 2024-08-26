package com.example.holymoly;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class SoundService extends Service {
    private MediaPlayer mediaPlayer;
    public SoundService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        // 음악 파일 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 효과음 재생
        mediaPlayer.start();

        // 서비스가 중단되었을 때 재시작하지 않도록 설정
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 중지될 때 MediaPlayer 자원을 해제
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}