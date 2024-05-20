package com.example.mytestapp;

import androidx.appcompat.app.AppCompatActivity;  // AppCompatActivity를 상속받아야 합니다.
import android.os.Bundle;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {  // AppCompatActivity를 상속받도록 수정합니다.

    // Firebase Firestore 인스턴스 가져오기
    private FirebaseFirestore db;
    private static List<SensorDataModel> sensorDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    // btnClick 메서드가 인스턴스 메서드로 되어야 합니다.
    public void btnClick(View view) {
        db = FirebaseFirestore.getInstance();
        CollectionReference parkCollection = db.collection("park");
        // 클릭 이벤트가 발생한 UI 요소의 id를 가져옵니다.
        int viewId = view.getId();
        // 클릭 이벤트가 발생한 UI 요소를 식별합니다.
        if (viewId == R.id.frame_1) {
            Task<Void> allSensorTask = getAllSensorData(parkCollection);
            Tasks.whenAllComplete(allSensorTask)
                    .addOnCompleteListener(allTasks -> {
                        if (allTasks.isSuccessful()) {
                            System.out.println("주차하기 레이어 누름");
                            Toast.makeText(this, "그대여 이제야 오는가?", Toast.LENGTH_SHORT).show();
                            // 새로운 액티비티를 시작 (ParkingGarageActivity)
                            Intent intent = new Intent(this, Parking_Red.class);
                            intent.putExtra("sensorDataList", convertListToJsonString(sensorDataList));
                            startActivity(intent);
                        } else{
                                // 실패 처리
                                Log.d("MainActivity", "센서 데이터 검색 실패");
                            }
                        });
        }
        if (viewId == R.id.frame_2) {
            System.out.println("차량번호입력 레이어 누름");
            Toast.makeText(this, "그대여 이제야 등록하는가?", Toast.LENGTH_SHORT).show();
            // 새로운 액티비티를 시작 (CarNumActivity)
            Intent intent = new Intent(this, CarNumActivity.class);
            startActivity(intent);
        }
    }

    public static String convertListToJsonString(List<SensorDataModel> sensorDataList) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (SensorDataModel data : sensorDataList) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("documentName", data.getDocumentName());
                jsonItem.put("isOccupied", data.getIsOccupied());
                jsonItem.put("formattedTimestamp", data.getFormattedTimestamp());
                jsonArray.put(jsonItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }
    static class SensorDataModel {
        private String documentName;
        private Boolean isOccupied;
        private String formattedTimestamp;

        public SensorDataModel(String documentName, Boolean isOccupied, String formattedTimestamp) {
            this.documentName = documentName;
            this.isOccupied = isOccupied;
            this.formattedTimestamp = formattedTimestamp;
        }
        public String getDocumentName() {
            return documentName;
        }
        public Boolean getIsOccupied() {
            return isOccupied;
        }
        public String getFormattedTimestamp() {
            return formattedTimestamp;
        }
    }

    public static Task<Void> getAllSensorData(CollectionReference parkCollection) {
        sensorDataList = new ArrayList<>();

        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        parkCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

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
                        taskCompletionSource.setResult(null); // 작업 성공
                    } else {
                        Log.d("FirestoreData", "데이터 가져오기 실패", task.getException());
                        taskCompletionSource.setException(task.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }
    static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분 ss초", Locale.KOREA);
        return sdf.format(date);
    }

    public static Task<List<SensorDataModel>> getSensorDataList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference parkCollection = db.collection("park");

        return getAllSensorData(parkCollection).continueWith(task -> {
            if (task.isSuccessful()) {
                return sensorDataList;
            } else {
                throw task.getException();
            }
        });
    }
}

