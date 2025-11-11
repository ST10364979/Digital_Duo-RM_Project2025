package com.example.rm_project


import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ITAssessmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_it_assessment)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
