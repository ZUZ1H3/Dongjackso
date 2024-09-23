package com.example.holymoly;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrationActivity extends AppCompatActivity {
    //추가
    private static final String PREFS_NAME = "CharacterPrefs";
    private static final String KEY_HAIR = "selectedHair";
    private static final String KEY_HAIR1 = "selectedHair1";
    private static final String KEY_HAIR2 = "selectedHair2";

    private static final String KEY_CLOTHES = "selectedClothes";
    private static final String KEY_CLOTHES1 = "selectedClothes1";
    private static final String KEY_CLOTHES2 = "selectedClothes2";

    private static final String KEY_ACCESSORY_HEAD = "selectedAccessoryHead";
    private static final String KEY_ACCESSORY_BACK = "selectedAccessoryBack";

    private static final String KEY_EYES = "selectedEyes";
    private static final String KEY_HAIR_COLOR = "hairColor";
    private static final String KEY_HAIR_COLOR2 = "hairColor2";

    //라디오두줄
    public final MutableLiveData<Integer> radioChecked = new MutableLiveData<>();

    private ImageButton ibNext;
    private RadioGroup rgCategory, rgHair, rgHair2, rgClothes, rgClothes2, rgHairColor, rgEyesColor, rgAccessoryHead, rgAccessoryBack;
    private ImageView ivHair, ivEyesColor, ivClothes, ivFace, ivAccessoryHead, ivAccessoryBack;
    private boolean isOriginalColor = true;

    /* DB */
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private SharedPreferences sharedPreferences;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //저장 sharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //카테고리 라디오 그룹
        rgCategory = findViewById(R.id.rg_category);

        //뒤로가기 버튼 - 누르면 홈엑티비티로 넘어감
        ibNext = findViewById(R.id.ib_next);

        //머리/옷/염색 종류 라디오그룹 - 선택된 것을 캐릭터에 입히고 색깔도 변하게함 / 중복선택x
        rgHair = (RadioGroup) findViewById(R.id.rg_hair);
        rgHair.clearCheck();
        rgHair.setOnCheckedChangeListener(hairListener1);
        rgHair2 = (RadioGroup) findViewById(R.id.rg_hair2);
        rgHair2.clearCheck();
        rgHair2.setOnCheckedChangeListener(hairListener2);

        rgClothes = findViewById(R.id.rg_clothes);
        rgClothes.clearCheck();
        rgClothes.setOnCheckedChangeListener(clothesListener1);
        rgClothes2 = findViewById(R.id.rg_clothes2);
        rgClothes2.clearCheck();
        rgClothes2.setOnCheckedChangeListener(clothesListener2);

        rgAccessoryHead = findViewById(R.id.rg_accessory_head);
        rgAccessoryBack = findViewById(R.id.rg_accessory_back);

        rgHairColor = findViewById(R.id.rg_hairColor);
        rgEyesColor = findViewById(R.id.rg_eyesColor);

        //디폴트 상태 - 처음에 설정을 키면 머리가 선택된채로 나오게함
        findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.clothLayout).setVisibility(View.GONE);
        findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
        findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
        findViewById(R.id.accessoryLayout).setVisibility(View.GONE);

        //착용된 헤어, 눈동자 선언
        ivHair = findViewById(R.id.iv_hair);
        ivEyesColor = findViewById(R.id.iv_character_eyes);
        ivClothes = findViewById(R.id.iv_character_clothes);
        ivFace = findViewById(R.id.iv_character_face);
        ivAccessoryHead = findViewById(R.id.iv_accessory_head);
        ivAccessoryBack = findViewById(R.id.iv_accessory_back);

        rgCategory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sound();
                if (checkedId == R.id.rb_hairCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.accessoryLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_clothesCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.accessoryLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_accessoryCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.accessoryLayout).setVisibility(View.VISIBLE);
                }
                else if (checkedId == R.id.rb_colorCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.accessoryLayout).setVisibility(View.GONE);
                }
            }
        });

        //icNext를 누르면 HomeActivity로 넘어가도록 함(뒤로가기 버튼)
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                saveCharacterImage();
            }
        });

        // 머리 악세사리 리스너
        rgAccessoryHead.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sound();
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_accessory_head_bee) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_bee);
                } else if (checkedId == R.id.rb_accessory_head_angel) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_haed_angel);
                } else if (checkedId == R.id.rb_accessory_head_puppy) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_puppy);
                } else if (checkedId == R.id.rb_accessory_head_cat) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_cat);
                } else if (checkedId == R.id.rb_accessory_head_ribbon) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_ribbon);
                } else if (checkedId == R.id.rb_accessory_head_rabbit) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_rabbit);
                } else if (checkedId == R.id.rb_accessory_head_hat) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_hat);
                } else if (checkedId == R.id.rb_accessory_head_marin) {
                    ivAccessoryHead.setImageResource(R.drawable.iv_accessory_head_marin);
                } else if (checkedId == R.id.rb_accessory_head_null) {
                    ivAccessoryHead.setImageResource(0);
                }

                saveSelection(KEY_ACCESSORY_HEAD, checkedId);
            }
        });

        // 등 악세사리 리스너
        rgAccessoryBack.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sound();
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_accessory_back_bee) {
                    ivAccessoryBack.setImageResource(R.drawable.iv_accessory_back_bee);
                } else if (checkedId == R.id.rb_accessory_back_angel) {
                    ivAccessoryBack.setImageResource(R.drawable.iv_accessory_back_angel);
                } else if (checkedId == R.id.rb_accessory_back_puppy) {
                    ivAccessoryBack.setImageResource(R.drawable.iv_accessory_back_puppy);
                } else if (checkedId == R.id.rb_accessory_back_cat) {
                    ivAccessoryBack.setImageResource(R.drawable.iv_accessory_back_cat);
                } else if (checkedId == R.id.rb_accessory_back_wing) {
                    ivAccessoryBack.setImageResource(R.drawable.iv_accessory_back_wing);
                } else if (checkedId == R.id.rb_accessory_back_null) {
                    ivAccessoryBack.setImageResource(0);
                }

                saveSelection(KEY_ACCESSORY_BACK, checkedId);
            }
        });

        //눈동자 색깔 바꿔주는 체크체인지리스너
        rgEyesColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sound();
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
                saveSelection(KEY_EYES, checkedId);
            }
        });

        // 머리 색깔 바꿔주는 체크체인지리스너
        rgHairColor.setOnCheckedChangeListener((group, checkedId) -> {
            sound();
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
            saveColorFilter(colorFilter);
            saveSelection(KEY_HAIR_COLOR2, checkedId);
        });

        restoreSelection();
    }

    //rgHair 리스너
    private RadioGroup.OnCheckedChangeListener hairListener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId != -1) {
                rgHair2.setOnCheckedChangeListener(null);
                rgHair2.clearCheck();
                rgHair2.setOnCheckedChangeListener(hairListener2);
            }

            applyHairStyle(checkedId);
            saveSelection(KEY_HAIR1, checkedId);
            saveSelection(KEY_HAIR, checkedId); // 마지막으로 선택된 머리 스타일 저장
        }
    };

    //rgHair2 리스너
    private RadioGroup.OnCheckedChangeListener hairListener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId != -1) {
                rgHair.setOnCheckedChangeListener(null);
                rgHair.clearCheck();
                rgHair.setOnCheckedChangeListener(hairListener1);
            }

            applyHairStyle(checkedId);
            saveSelection(KEY_HAIR2, checkedId);
            saveSelection(KEY_HAIR, checkedId); // 마지막으로 선택된 머리 스타일 저장
        }
    };

    //ivHair를 바꿔주는 함수
    private void applyHairStyle(int checkedId) {
        // 선택된 머리 스타일에 따라 ivHair의 이미지를 설정
        if (checkedId == R.id.rb_g_long) {
            ivHair.setImageResource(R.drawable.iv_hair_long);
        } else if (checkedId == R.id.rb_g_pigtails) {
            ivHair.setImageResource(R.drawable.iv_hair_pigtails);
        } else if (checkedId == R.id.rb_g_twinbuns) {
            ivHair.setImageResource(R.drawable.iv_hair_twinbuns);
        } else if (checkedId == R.id.rb_g_short) {
            ivHair.setImageResource(R.drawable.iv_hair_short);
        } else if (checkedId == R.id.rb_g_twintail) {
            ivHair.setImageResource(R.drawable.iv_hair_twintail);
        } else if (checkedId == R.id.rb_hair_g_ponytail) {
            ivHair.setImageResource(R.drawable.iv_hair_ponytail);
        } else if (checkedId == R.id.rb_b_hook) {
            ivHair.setImageResource(R.drawable.iv_hair_hook);
        } else if (checkedId == R.id.rb_b_hedgehog) {
            ivHair.setImageResource(R.drawable.iv_hair_hedgehog);
        } else if (checkedId == R.id.rb_b_broccoli) {
            ivHair.setImageResource(R.drawable.iv_hair_broccoli);
        } else if (checkedId == R.id.rb_b_gourd) {
            ivHair.setImageResource(R.drawable.iv_hair_gourd);
        } else if (checkedId == R.id.rb_b_chestnut) {
            ivHair.setImageResource(R.drawable.iv_hair_chestnut);
        } else if (checkedId == R.id.rb_hair_b_twohooks) {
            ivHair.setImageResource(R.drawable.iv_hair_twohooks);
        }
    }

    //rgClothes 리스너
    private RadioGroup.OnCheckedChangeListener clothesListener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId != -1) {
                rgClothes2.setOnCheckedChangeListener(null);
                rgClothes2.clearCheck();
                rgClothes2.setOnCheckedChangeListener(clothesListener2);
            }

            applyClothesStyle(checkedId);
            saveSelection(KEY_CLOTHES1, checkedId);
            saveSelection(KEY_CLOTHES, checkedId);
        }
    };

    //rgClothes2 리스너
    private RadioGroup.OnCheckedChangeListener clothesListener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId != -1) {
                rgClothes.setOnCheckedChangeListener(null);
                rgClothes.clearCheck();
                rgClothes.setOnCheckedChangeListener(clothesListener1);
            }

            applyClothesStyle(checkedId);
            saveSelection(KEY_CLOTHES2, checkedId);
            saveSelection(KEY_CLOTHES, checkedId);
        }
    };

    //ivClothes를 바꿔주는 함수
    private void applyClothesStyle(int checkedId) {
        // 선택된 머리 스타일에 따라 ivHair의 이미지를 설정
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
        } else if (checkedId == R.id.rb_clothes_bee) {
            ivClothes.setImageResource(R.drawable.iv_clothes_bee);
        }
    }

    //추가
    private void saveSelection(String key, int checkedId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, checkedId);
        editor.apply();
    }

    private void saveColorFilter(int colorFilter) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_HAIR_COLOR, colorFilter);
        editor.apply();
    }

    private void restoreSelection() {
        // rg_hair 복원
        int savedHairId1 = sharedPreferences.getInt(KEY_HAIR1, -1);
        int savedHairId2 = sharedPreferences.getInt(KEY_HAIR2, -1);
        int finalHairId = sharedPreferences.getInt(KEY_HAIR, -1); // 마지막으로 선택된 머리 스타일을 ivHair에 적용

        if (finalHairId != -1) {
            if (savedHairId1 == finalHairId) {
                rgHair.check(finalHairId);
            } else if (savedHairId2 == finalHairId) {
                rgHair2.check(finalHairId);
            }
            applyHairStyle(finalHairId);
        }

        int savedClothesId1 = sharedPreferences.getInt(KEY_CLOTHES1, -1);
        int savedClothesId2 = sharedPreferences.getInt(KEY_CLOTHES2, -1);
        int finalClothesId = sharedPreferences.getInt(KEY_CLOTHES, -1);

        if (finalClothesId != -1) {
            if (savedClothesId1 == finalClothesId) {
                rgClothes.check(finalClothesId);
            } else if (savedClothesId2 == finalClothesId) {
                rgClothes2.check(finalClothesId);
            }
            applyClothesStyle(finalClothesId);
        }

        int savedAccessoryHead = sharedPreferences.getInt(KEY_ACCESSORY_HEAD, -1);
        if (savedAccessoryHead != -1) {
            rgAccessoryHead.check(savedAccessoryHead);
        }

        int savedAccessoryBack = sharedPreferences.getInt(KEY_ACCESSORY_BACK, -1);
        if (savedAccessoryBack != -1) {
            rgAccessoryBack.check(savedAccessoryBack);
        }

        int savedEyesId = sharedPreferences.getInt(KEY_EYES, -1);
        if (savedEyesId != -1) {
            rgEyesColor.check(savedEyesId);
        }

        int savedHairColor = sharedPreferences.getInt(KEY_HAIR_COLOR, Color.TRANSPARENT);
        if (savedHairColor != Color.TRANSPARENT) {
            ivHair.setColorFilter(savedHairColor, PorterDuff.Mode.SRC_ATOP);
        }

        int savedHairColor2 = sharedPreferences.getInt(KEY_HAIR_COLOR2, -1);
        if(savedHairColor2 != -1) {
            rgHairColor.check(savedHairColor2);
        }
    }

    // 캐릭터 생성 및 Storage에 업로드
    private void saveCharacterImage() {
        // 캐릭터 이미지 생성
        Bitmap finalBitmap = createBitmap();
        // 캐릭터 테두리 생성
        Bitmap finalBitmapWithStroke = createBitmapWithStroke(finalBitmap);
        StorageReference imageRef = storageRef.child("characters/" + user.getUid() + "_1.png");

        // 캐릭터 테두리를 png로 압축 및 저장
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        finalBitmapWithStroke.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // 테두리 업로드 시작
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> { });

        // 기존 이미지 삭제 후 새 파일 업로드
        //deleteImageAndUpload(finalBitmap);
        uploadImage(finalBitmap);
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

        faceBitmap.recycle();
        eyesBitmap.recycle();
        clothesBitmap.recycle();
        hairBitmap.recycle();

        // 최종 Bitmap 이미지 생성할 크기 설정 - 너비, 높이, 배경 투명
        Bitmap finalBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(cFaceBitmap, 136, 124, null);
        canvas.drawBitmap(cHairBitmap, 50, -7, null);
        canvas.drawBitmap(cClothesBitmap, 162, 292, null);
        canvas.drawBitmap(cEyesBitmap, 238, 265, null);

        return finalBitmap;
    }

    // 테두리가 있는 Bitmap
    private Bitmap createBitmapWithStroke(Bitmap bitmap) {
        Bitmap bitmapWithStroke = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWithStroke);

        // 테두리(스트로크)를 위한 Paint 설정
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.WHITE); // 테두리 색상
        strokePaint.setStyle(Paint.Style.STROKE); // 테두리만 그리기
        //strokePaint.setStrokeJoin(Paint.Join.ROUND); // 경로 연결 부분을 둥글게 설정
        strokePaint.setStrokeWidth(10); // 테두리 두께 설정
        //strokePaint.setAntiAlias(true); // 높은 해상도 설정

        // 이미지 및 테두리 그리기
        Path strokePath = createStroke(bitmap);
        canvas.drawPath(strokePath, strokePaint);
        canvas.drawBitmap(bitmap, 0, 0, null);

        return bitmapWithStroke;
    }

    // 테두리 생성
    private Path createStroke(Bitmap bitmap) {
        Path path = new Path();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel != Color.TRANSPARENT) {
                    // 투명하지 않은 픽셀에 대해 경로 추가
                    path.addCircle(x , y, 5, Path.Direction.CW);
                }
            }
        }
        return path;
    }
    // Storage에 이미지 파일 업로드
    private void uploadImage(Bitmap bitmap) {
        StorageReference imageRef = storageRef.child("characters/" + user.getUid() + ".png");

        // bitmap을 png로 압축 및 저장
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // 업로드 시작
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "이미지 업데이트 성공", Toast.LENGTH_SHORT).show();
            isRegistration(); // 이미지 업로드 완료되면 이동
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
                                finish();
                            }
                            else { // 없으면 신규 사용자 -> InfoActivity로 정보 등록
                                Intent intent = new Intent(this, InfoActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
