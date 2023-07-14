package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView sampleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sampleText = findViewById(R.id.sample_text);
        sampleText.setText("Hello world!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().getAssignmentManager().post(TaskThread4.class, () -> sampleText.setText("張三李四王五"));
    }
}