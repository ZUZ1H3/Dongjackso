package com.example.holymoly;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrationActivity extends AppCompatActivity {

    private ImageButton ibNext;
    private RadioGroup rgCategory, rgHair, rgClothes, rgHairColor, rgEyesColor;
    private ImageView ivHair, ivEyesColor, ivClothes, ivFace;
    private boolean isOriginalColor = true;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //카테고리 라디오 그룹
        rgCategory = findViewById(R.id.rg_category);

        //뒤로가기 버튼 - 누르면 홈엑티비티로 넘어감
        ibNext = findViewById(R.id.ib_next);

        //머리/옷/염색 종류 라디오그룹 - 선택된 것을 캐릭터에 입히고 색깔도 변하게함 / 중복선택x
        rgHair = findViewById(R.id.rg_hair);
        rgClothes = findViewById(R.id.rg_clothes);
        rgHairColor = findViewById(R.id.rg_hairColor);
        rgEyesColor = findViewById(R.id.rg_eyesColor);

        //디폴트 상태 - 처음에 설정을 키면 머리가 선택된채로 나오게함
        findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.clothLayout).setVisibility(View.GONE);
        findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
        findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);

        //착용된 헤어, 눈동자 선언
        ivHair = findViewById(R.id.iv_hair);
        ivEyesColor = findViewById(R.id.iv_character_eyes);
        ivClothes = findViewById(R.id.iv_character_clothes);

        ivFace = findViewById(R.id.iv_character_face);

        rgCategory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_hairCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_clothesCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_colorCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.VISIBLE);
                }
            }
        });

        //icNext를 누르면 HomeActivity로 넘어가도록 함(뒤로가기 버튼)
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCharacterImage();
            }
        });

        //헤어 버튼 누를때 맞는 헤어를 입혀주는 체크체인지리스너
        rgHair.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_g_long) {
                    ivHair.setImageResource(R.drawable.iv_hair_long);
                } else if (checkedId == R.id.rb_b_hook) {
                    ivHair.setImageResource(R.drawable.iv_hair_hook);
                } else if (checkedId == R.id.rb_g_pigtails) {
                    ivHair.setImageResource(R.drawable.iv_hair_pigtails);
                } else if (checkedId == R.id.rb_b_hedgehog) {
                    ivHair.setImageResource(R.drawable.iv_hair_hedgehog);
                } else if (checkedId == R.id.rb_g_twinbuns) {
                    ivHair.setImageResource(R.drawable.iv_hair_twinbuns);
                } else if (checkedId == R.id.rb_b_broccoli) {
                    ivHair.setImageResource(R.drawable.iv_hair_broccoli);
                } else if (checkedId == R.id.rb_g_short) {
                    ivHair.setImageResource(R.drawable.iv_hair_short);
                } else if (checkedId == R.id.rb_b_gourd) {
                    ivHair.setImageResource(R.drawable.iv_hair_gourd);
                } else if (checkedId == R.id.rb_g_twintail) {
                    ivHair.setImageResource(R.drawable.iv_hair_twintail);
                } else if (checkedId == R.id.rb_b_chestnut) {
                    ivHair.setImageResource(R.drawable.iv_hair_chestnut);
                } else if (checkedId == R.id.rb_hair_g_ponytail) {
                    ivHair.setImageResource(R.drawable.iv_hair_ponytail);
                } else if (checkedId == R.id.rb_hair_b_twohooks) {
                    ivHair.setImageResource(R.drawable.iv_hair_twohooks);
                }
            }
        });

        //눈동자 색깔 바꿔주는 체크체인지리스너
        rgEyesColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_ec_purple) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_purple);
                } else if (checkedId == R.id.rb_ec_green) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_green);
                } else if (checkedId == R.id.rb_ec_yellow) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_yellow);
                } else if (checkedId == R.id.rb_ec_pink) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_pink);
                } else if (checkedId == R.id.rb_ec_brown) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_brown);
                } else if (checkedId == R.id.rb_ec_blue) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_blue);
                }
            }
        });

        // 머리 색깔 바꿔주는 체크체인지리스너
        rgHairColor.setOnCheckedChangeListener((group, checkedId) -> {
            int colorFilter = Color.TRANSPARENT;

            if (checkedId == R.id.rb_hc_purple) {
                colorFilter = Color.parseColor("#AA93BC"); // 보라색
            } else if (checkedId == R.id.rb_hc_green) {
                colorFilter = Color.parseColor("#91B684"); // 녹색
            } else if (checkedId == R.id.rb_hc_yellow) {
                colorFilter = Color.parseColor("#FBC17E"); // 노란색
            } else if (checkedId == R.id.rb_hc_pink) {
                colorFilter = Color.parseColor("#EC96B0"); // 분홍색
            } else if (checkedId == R.id.rb_hc_brown) {
                colorFilter = Color.parseColor("#A86D60"); // 갈색
            } else if (checkedId == R.id.rb_hc_blue) {
                colorFilter = Color.parseColor("#7E8DB1"); // 파란색
            }

            ivHair.setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP);
        });

        //옷 버튼을 누르면 옷을 입혀주는 체크체인지리스너
        rgClothes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_clothes_princessdress) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_princessdress);
                } else if (checkedId == R.id.rb_clothes_princedress) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_princedress);
                } else if (checkedId == R.id.rb_clothes_denimskirts) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_denimskirt);
                } else if (checkedId == R.id.rb_clothes_denimjeans) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_denimjeans);
                } else if (checkedId == R.id.rb_clothes_rabbit) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_rabbit);
                } else if (checkedId == R.id.rb_clothes_frog) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_frog);
                } else if (checkedId == R.id.rb_clothes_flower) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_flower);
                } else if (checkedId == R.id.rb_clothes_marin) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_marin);
                } else if (checkedId == R.id.rb_clothes_stripe) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_stripe);
                } else if (checkedId == R.id.rb_clothes_cherry) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_cherry);
                }
            }
        });
    }

    // 캐릭터 생성 및 Storage에 업로드
    private void saveCharacterImage() {
        // 캐릭터 이미지 생성
        Bitmap finalBitmap = createBitmap();
        // 기존 이미지 삭제 후 새 파일 업로드
        deleteImageAndUpload(finalBitmap);
    }
    // 캐릭터 Bitmap 이미지 제작
    private Bitmap createBitmap() {
        // 캐릭터 구성 요소마다 하나의 Bitmap 이미지로 구분
        ivFace.setDrawingCacheEnabled(true);
        Bitmap faceBitmap = Bitmap.createBitmap(ivFace.getDrawingCache());
        ivFace.setDrawingCacheEnabled(false);

        ivEyesColor.setDrawingCacheEnabled(true);
        Bitmap eyesBitmap = Bitmap.createBitmap(ivEyesColor.getDrawingCache());
        ivEyesColor.setDrawingCacheEnabled(false);

        ivClothes.setDrawingCacheEnabled(true);
        Bitmap clothesBitmap = Bitmap.createBitmap(ivClothes.getDrawingCache());
        ivClothes.setDrawingCacheEnabled(false);

        ivHair.setDrawingCacheEnabled(true);
        Bitmap hairBitmap = Bitmap.createBitmap(ivHair.getDrawingCache());
        ivHair.setDrawingCacheEnabled(false);

        // 구성 요소들 사이즈 맞추기
        Bitmap cFaceBitmap = Bitmap.createScaledBitmap(faceBitmap, 328, 239, true);
        Bitmap cHairBitmap = Bitmap.createScaledBitmap(hairBitmap, 500, 500, true);
        Bitmap cClothesBitmap = Bitmap.createScaledBitmap(clothesBitmap, 276, 308, true);
        Bitmap cEyesBitmap = Bitmap.createScaledBitmap(eyesBitmap, 126, 40, true);

        // 최종 Bitmap 이미지 생성할 크기 설정 - 너비, 높이, 배경 투명
        Bitmap finalBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(cFaceBitmap, 86, 124, null);
        canvas.drawBitmap(cHairBitmap, 0, -7, null);
        canvas.drawBitmap(cClothesBitmap, 112, 292, null);
        canvas.drawBitmap(cEyesBitmap, 188, 265, null);

        return finalBitmap;
    }

    // 이미지 삭제 및 업로드
    private void deleteImageAndUpload(Bitmap newBitmap) {
        // 'characters' 경로의 모든 파일을 나열
        StorageReference imageRef = storageRef.child("characters/");
        imageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            boolean fileExists = false; // 파일 존재 확인 변수

            for (StorageReference item : items) {
                String name = item.getName();
                // 파일 이름이 현재 사용자의 ID로 시작하는 경우 삭제
                if (name.startsWith(user.getUid())) {
                    fileExists = true;
                    item.delete().addOnSuccessListener(aVoid -> {
                        // 삭제 성공 시 새 이미지 업로드
                        uploadImage(newBitmap);
                    });
                    break; // 찾으면 멈춤
                }
            }

            // 현재 사용자의 ID로 시작하는 파일이 없을 경우
            if (!fileExists) uploadImage(newBitmap);
        }).addOnFailureListener(exception -> {
            // 파일 나열 실패 시
            Toast.makeText(this, "파일 나열 실패: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    // Storage에 이미지 파일 업로드
    private void uploadImage(Bitmap bitmap) {
        // Storage에 올라갈 파일 참조명 - 타임스탬프 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String fileName = user.getUid() + "_" + timestamp + ".png";

        StorageReference imageRef = storageRef.child("characters/" + fileName);

        // bitmap을 png로 압축 및 저장
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // 업로드 시작
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "이미지 업데이트 성공", Toast.LENGTH_SHORT).show();
            isRegistration(); // 이미지 업로드 완료되면 이동
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    // 가입 유무에 따라 activity 변환
    private void isRegistration() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 이름으로 회원가입 유무 확인
                            String name = document.getString("name");
                            if (name != null) { // 존재하면 등록된 사용자 -> HomeActivity
                                Intent intent = new Intent(this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else { // 없으면 신규 사용자 -> InfoActivity로 정보 등록
                                Intent intent = new Intent(this, InfoActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                });
    }
}
