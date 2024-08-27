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
        // 음악 파일 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm_sea);
        mediaPlayer.setLooping(true); // 반복 재생
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 음악 재생 시작
        mediaPlayer.start();

        // 서비스가 중단되었을 때 재시작하지 않도록 설정
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 MediaPlayer 자원을 해제
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // 서비스가 더 이상 필요하지 않으면 종료
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}