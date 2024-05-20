package com.example.mytestapp;

import static com.example.mytestapp.MainActivity.formatDate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mytestapp.MainActivity.SensorDataModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class Parking_Red extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView parkingImageView1, parkingImageView2;
    private boolean isGreen1 = true;
    private boolean isGreen2 = true; // 초기 색상은 초록색
    private boolean isPopupShown1 = false;
    private boolean isPopupShown2 = false; // 팝업 표시 여부를 나타내는 변수

    private static final String PREF_NAME = "ParkingPrefs";
    private static final String PARKING_TEXT_KEY = "ParkingText";

    List<SensorDataModel> sensorDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking_garage);

        // 이전에 저장된 parkingText 값을 불러옴
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedParkingText = prefs.getString(PARKING_TEXT_KEY, "No."); // 기본값은 "No."

        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("sensorDataList");

         // 새로운 리스트 생성

        if (jsonString != null) {
            // JSON 문자열을 List로 변환
            sensorDataList = convertJsonStringToList(jsonString);
        } else {
            Log.d("Parking_Red", "No data received from MainActivity");
        }

        RelativeLayout homeLayout = findViewById(R.id.home_id);// 홈화면 레이어 id 변수
        parkingImageView1 = findViewById(R.id.parking_green1); // 주차 자리 P1번 레이어 id 변수
        parkingImageView2 = findViewById(R.id.parking_green2);
        parkingImageView1.setTag("parking_green1");
        parkingImageView2.setTag("parking_green2");
        TextView parkingText = findViewById(R.id.ParkingNum);
        TextView retTag = new TextView(this);
        parkingText.setText(savedParkingText);

        // fetchParkingStatus 메서드 호출 시 sensorDataList 전달
        fetchParkingStatus(sensorDataList);

        // 클릭 리스너를 통합하여 코드 중복 최소화
        View.OnClickListener parkingClickListener = view -> {

            boolean isGreen;
            boolean isPopupShown;

            if (view == parkingImageView1) {
                isGreen = isGreen1;
                isPopupShown = isPopupShown1;
            } else if (view == parkingImageView2) {
                isGreen = isGreen2;
                isPopupShown = isPopupShown2;
            } else {
                return; // 다른 뷰에서 클릭되었을 경우 처리하지 않음
            }
            // 클릭 처리
            handleParkingClick((ImageView) view, parkingText, retTag, isGreen, isPopupShown);
        };

        // 클릭 리스너 할당
        parkingImageView1.setOnClickListener(parkingClickListener);
        parkingImageView2.setOnClickListener(parkingClickListener);

        homeLayout.setOnClickListener(v -> {
            System.out.println("메인 홈 버튼");
            finish(); // 현재 액티비티를 종료합니다.
        });

        ImageView refreshLayout = findViewById(R.id.refresh_button);
        refreshLayout.setOnClickListener(v -> {
            System.out.println("새로고침");
            handleRefreshButton();
        });

        TextView returnCarText = findViewById(R.id.return_car);
        returnCarText.setOnClickListener(v -> {
            // "반납하기" 텍스트를 클릭했을 때의 동작
            System.out.println("반납");
            handleReturnCarClick(parkingText, retTag);
        });
    }

    private void handleParkingClick(ImageView parkingImageView, TextView parkingText, TextView retTag, boolean isGreen, boolean isPopupShown) {
        if (isGreen) {
            // 초록색인 경우
            if (!isPopupShown) {
                // 팝업 창 표시
                showAlertDialog();
                // No. 텍스트를 변경
                parkingText.setText("No.");

                // 차량 번호 텍스트를 숨김
                hideCarNumText();
            }
        } else {
            // 빨간색인 경우
            // 주차 번호 텍스트를 변경
            String parkingNumber = parkingImageView.getTag() == "parking_red1" ? "P01" : "P02";
            parkingText.setText(parkingNumber);
            // 차량 번호 텍스트를 보임
            showCarNumText();
        }
        retTag.setTag(parkingImageView.getTag());

        // 주차가 빨간색일 때만 SharedPreferences 업데이트
        if (!isGreen) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PARKING_TEXT_KEY, parkingText.getText().toString());
            editor.apply();
        }
    }
    private void handleRefreshButton() {
        db = FirebaseFirestore.getInstance();
        CollectionReference parkCollection = db.collection("park");

        parkCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        sensorDataList.clear(); // 갱신하기 전에 기존 센서 데이터 리스트를 비웁니다.

                        for (DocumentSnapshot document : documents) {
                            Boolean isOccupied = document.getBoolean("is_occupied");
                            Timestamp timestamp = document.getTimestamp("timestamp");

                            if (isOccupied != null && timestamp != null) {
                                String formattedTimestamp = formatDate(timestamp.toDate());
                                SensorDataModel sensorData = new SensorDataModel(document.getId(), isOccupied, formattedTimestamp);
                                sensorDataList.add(sensorData);
                            } else {
                                Log.d("FirestoreData", "잘못된 데이터 형식");
                            }
                        }

                        // 새로고침된 데이터를 기반으로 주차 상태 UI를 업데이트합니다.
                        fetchParkingStatus(sensorDataList);
                        Toast.makeText(this, "데이터가 성공적으로 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Firestore에서 데이터를 가져오는 데 실패한 경우 처리
                        Log.d("Parking_Red", "데이터 갱신 실패", task.getException());
                        Toast.makeText(this, "데이터 갱신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void handleReturnCarClick(TextView parkingText, TextView retTag) {
        // "반납하기" 텍스트를 클릭했을 때의 동작
        hideCarNumText();
        //parkingText.setText("No.");

        // 이전에 저장된 parkingText 값을 불러옴
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedParkingText = prefs.getString(PARKING_TEXT_KEY, "No."); // 기본값은 "No."

        parkingText.setText(savedParkingText);

        // 저장된 값이 "No."이면 아무 동작을 하지 않고, 그렇지 않으면 차량 번호를 표시하도록 처리
        if (!"No.".equals(savedParkingText)) {
            parkingText.setText("No.");
        }

        // 변경된 parkingText 값을 SharedPreferences에 저장
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PARKING_TEXT_KEY, parkingText.getText().toString());
        editor.apply();
//
//        // 반납하기 눌렀을 때 빨간색이던 주차 자리를 초록색으로 변경
//        if(retTag.getTag() == "parking_green1" || retTag.getTag() == "parking_green2") {
//            //팝업 등장! 반납할 수 없는 자리 입니다.
//        } else {
//            if(retTag.getTag() == parkingImageView1.getTag()) {
//                parkingImageView1.setBackgroundColor(Color.GREEN);
//                parkingImageView1.setTag("parking_green1");
//                isGreen1 = true; isPopupShown1 = false;
//            } else {
//                parkingImageView2.setBackgroundColor(Color.GREEN);
//                parkingImageView2.setTag("parking_green2");
//                isGreen2 =true; isPopupShown2 = false;
//            }
//        }
        // currentImageResource = R.drawable.parking_green;
        //parkingImageView1.setImageResource(currentImageResource);

//        // 팝업 표시 여부를 true로 변경
//        isPopupShown2 = true;
    }

    private void showAlertDialog() {
        System.out.println("AlerDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("빈 자리");
        builder.setMessage("주차된 차량이 없습니다. 다시 선택해 주세요.");

        builder.setPositiveButton("확인", (dialog, which) -> {
            // 확인 버튼을 눌렀을 때의 동작
            dialog.dismiss(); // 다이얼로그 닫기
        });

        // 다이얼로그 보이기
        builder.create().show();
    }

    // 이 메서드는 "차량 번호" TextView를 숨기는 데 사용됩니다.
    // 매개변수를 사용하지 않고 직접 ID로 TextView를 찾습니다.
    private void hideCarNumText() {
        // 현재 레이아웃에서 ID가 "carnum"인 TextView를 찾습니다.
        TextView carNumText = findViewById(R.id.carnum);

        // TextView의 가시성을 GONE으로 설정하여 보이지 않게 하고 공간을 차지하지 않도록 합니다.
        carNumText.setVisibility(View.GONE);
    }

    // 이 메서드는 "차량 번호" TextView를 표시하는 데 사용됩니다.
    // 매개변수를 사용하지 않고 직접 ID로 TextView를 찾습니다.
    private void showCarNumText() {
        // 현재 레이아웃에서 ID가 "carnum"인 TextView를 찾습니다.
        TextView carNumText = findViewById(R.id.carnum);

        // TextView의 가시성을 VISIBLE로 설정하여 보이게 합니다.
        carNumText.setVisibility(View.VISIBLE);
    }
    private void fetchParkingStatus(List<SensorDataModel> sensorDataList) {
        // 실제로 사용할 때는 각 센서 데이터를 통해 UI를 업데이트하는 로직을 추가해야 합니다.
        // 여기서는 예시로 주차 자리 P1, P2를 빨간색 또는 초록색으로 변경하는 로직만 추가하였습니다.
        for (SensorDataModel sensorData : sensorDataList) {
            String sensorId = sensorData.getDocumentName();
            boolean isOccupied = sensorData.getIsOccupied();

            if ("sensor1".equals(sensorId)) {
                int color = isOccupied ? Color.RED : Color.GREEN;
                parkingImageView1.setBackgroundColor(color);
                if (isOccupied) {
                    parkingImageView1.setTag("parking_red1");
                    isGreen1 = !isOccupied; isPopupShown1 = true;
                } else {
                    parkingImageView1.setTag("parking_green1");
                }
            } else if ("sensor2".equals(sensorId)) {
                int color = isOccupied ? Color.RED : Color.GREEN;
                parkingImageView2.setBackgroundColor(color);
                if (isOccupied) {
                    parkingImageView2.setTag("parking_red2");
                    isGreen2 = !isOccupied; isPopupShown2 = true;
                } else {
                    parkingImageView2.setTag("parking_green2");
                }
            }
        }
    }

    private List<SensorDataModel> convertJsonStringToList(String jsonString) {
        List<SensorDataModel> sensorDataList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonItem = jsonArray.getJSONObject(i);
                String documentName = jsonItem.getString("documentName");
                boolean isOccupied = jsonItem.getBoolean("isOccupied");
                String formattedTimestamp = jsonItem.getString("formattedTimestamp");
                sensorDataList.add(new SensorDataModel(documentName, isOccupied, formattedTimestamp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sensorDataList;
    }
}
