package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.assignment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'assignment' library on application startup.
    static {
        System.loadLibrary("assignment");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().getAssignmentManager().post(TaskThread4.class, () -> binding.sampleText.setText("張三李四王五"));
    }

    /**
     * A native method that is implemented by the 'assignment' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}