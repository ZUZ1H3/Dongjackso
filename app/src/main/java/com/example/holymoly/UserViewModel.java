package com.example.holymoly;

import android.app.Application;
import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> nicknameLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<String, String>> infoLiveData = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<String> getNameLiveData() { return nameLiveData; }
    public LiveData<String> getNicknameLiveData() {
        return nicknameLiveData;
    }
    public MutableLiveData<Pair<String, String>> getInfoLiveData() { return infoLiveData; }

    public void loadName(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(task -> {
                    String name = task.getString("name");
                    nameLiveData.setValue(name);
                });
    }

    public void loadNickname(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(task -> {
                    String nickname = task.getString("nickname");
                    nicknameLiveData.setValue(nickname);
                });
    }

    public void loadInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(task -> {
                    String name = task.getString("name");
                    String nickname = task.getString("nickname");

                    infoLiveData.setValue(new Pair<>(name, nickname));
                });
    }

}
