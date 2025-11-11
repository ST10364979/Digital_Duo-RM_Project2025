package com.example.rm_project


import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
            addView(TextView(this@SettingsActivity).apply {
                text = "Settings\n\n• Branding\n• Notifications\n• Access control"
                textSize = 18f
            })
        }
        setContentView(root)
    }
}
