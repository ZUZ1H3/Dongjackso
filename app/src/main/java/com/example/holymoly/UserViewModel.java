package com.example.holymoly;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final MutableLiveData<String> nicknameLiveData = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<String> getNicknameLiveData() {
        return nicknameLiveData;
    }

    public void loadNickname(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String nickname = task.getResult().getString("nickname");
                        nicknameLiveData.setValue(nickname);
                    }
                });
    }
}
