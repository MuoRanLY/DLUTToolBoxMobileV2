package com.Shirai_Kuroko.DLUTMobile.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Shirai_Kuroko.DLUTMobile.R;

public class OpenVirtualCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_virtual_card);
        Toast.makeText(this, "抱歉，此功能暂未实现", Toast.LENGTH_SHORT).show();
    }
}