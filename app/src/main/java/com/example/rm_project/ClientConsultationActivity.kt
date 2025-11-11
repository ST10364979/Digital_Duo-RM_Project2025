package com.example.rm_project


import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ClientConsultationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_consultation)

        // Back arrow
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
