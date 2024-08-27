package com.example.holymoly;

import android.widget.ImageView;
import android.widget.TextView;

// 사용자 정보 가져오는 interface
public interface UserInfoLoader {
    void loadUserInfo(ImageView profile, TextView name, TextView nickname);
}
