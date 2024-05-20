package com.example.mytestapp;// YourActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_screenActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 일정 시간이 지난 후에 메인 액티비티로 이동
        new Handler().postDelayed(this::run, 3000); // 여기서는 3초(3000 밀리초)로 설정했습니다. 필요에 따라 변경하세요.
    }

    private void run() {
        Intent intent = new Intent(Splash_screenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
