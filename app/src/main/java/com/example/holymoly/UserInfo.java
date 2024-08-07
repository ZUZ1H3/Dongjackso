package com.example.holymoly;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

// firebase DB에 등록된 User 정보
public class UserInfo implements UserInfoLoader {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    StorageReference characterRef = storageRef.child("characters/");

    // Firestore에서 사용자 정보 가져오기
    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        // 이미지 가져오기
        characterRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            for (StorageReference item : items) {
                String img = item.getName();
                // 파일 이름이 현재 사용자의 ID로 시작하는 경우
                if (img.startsWith(user.getUid())) {
                    final long MEGABYTE = 1024 * 1024; // 1MB
                    item.getBytes(MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap cBitmap = cropImage(bitmap);
                            profile.setImageBitmap(cBitmap);
                        }
                    });
                }
            }
        });
        // 이름 가져오기
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String userName = document.getString("name");
                        name.setText(userName);
                    }
                });
    }
    // 이미지 확대
    private Bitmap cropImage(Bitmap bm) {
        int cropW = 25;
        int cropH = 5;
        int newWidth = 555;
        int newHeight = 440;

        return Bitmap.createBitmap(bm, cropW, cropH, newWidth, newHeight);
    }
}
