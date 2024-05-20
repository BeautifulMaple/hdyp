package com.example.mytestapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.json.JSONArray;

public class CarNumActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private EditText carNumberEditText;
    private TextView displayedCarNumberTextView;
    private Button sendButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_num);

        requestQueue = Volley.newRequestQueue(this);
        carNumberEditText = findViewById(R.id.edit_message);
        displayedCarNumberTextView = findViewById(R.id.carnum);
        sendButton = findViewById(R.id.send_button);

        // SharedPreferences 초기화
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // 저장된 차량 번호 가져오기
        String savedCarNumber = sharedPreferences.getString("carnum_park", "");

        // 가져온 차량 번호를 사용하여 필요한 작업 수행
        if (!savedCarNumber.isEmpty()) {
            // 저장된 차량 번호가 비어있지 않은 경우
            // 예시: 가져온 차량 번호를 텍스트뷰에 설정
            displayedCarNumberTextView.setText("ID: " + savedCarNumber);
        }

        carNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String carNumber = carNumberEditText.getText().toString();
                displayedCarNumberTextView.setText("ID: " + carNumber);

                // 입력한 차량 번호를 SharedPreferences에 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("carNumber", carNumber);
                editor.apply();

                return true;
            }
            return false;
        });

        sendButton.setOnClickListener(v -> {
            String carNumber = carNumberEditText.getText().toString();
            displayedCarNumberTextView.setText("ID: " + carNumber);

            // 입력한 차량 번호를 SharedPreferences에 저장
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("carNumber", carNumber);
            editor.apply();
        });
    }
}
