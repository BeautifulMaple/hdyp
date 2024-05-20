package com.example.mytestapp;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;

public class CustomAlertDialog {
    private final AlertDialog alertDialog;
    private boolean isPopupShown = false;

    public CustomAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            // "OK" 버튼을 클릭했을 때 수행할 동작을 여기에 작성하세요.
            isPopupShown = false;
        });
        alertDialog = builder.create();
    }

    public void show() {
        if (!isPopupShown) {
            alertDialog.show();
            isPopupShown = true;
        }
    }
}
