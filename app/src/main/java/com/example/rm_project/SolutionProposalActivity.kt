package com.example.rm_project


import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SolutionProposalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solution_proposal)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
