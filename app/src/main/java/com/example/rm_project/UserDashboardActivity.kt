package com.example.rm_project


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rm_project.adapters.ServiceAdapter
import com.example.rm_project.models.ServiceItem

class UserDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val rv = findViewById<RecyclerView>(R.id.rvServices)
        rv.layoutManager = GridLayoutManager(this, 2)

        // Build the services list (titles & blurbs match your mock)
        val items = listOf(
            ServiceItem(
                title = "Client Consultation",
                description = "Understand your business needs and challenges.",
            ) {
                startActivity(Intent(this, ClientConsultationActivity::class.java))
            },

            ServiceItem(
                title = "IT Assessment",
                description = "Identify technology gaps in your current infrastructure.",
            ) { startActivity(Intent(this, ITAssessmentActivity::class.java)) },

            ServiceItem(
                title = "Solution Proposal",
                description = "Recommend tailored IT solutions for your business.",
            ) { startActivity(Intent(this, SolutionProposalActivity::class.java)) },


            ServiceItem(
                title = "Implementation",
                description = "Professional setup and configuration of services.",
            ) { startActivity(Intent(this, ImplementationActivity::class.java)) },

            ServiceItem(
                title = "Ongoing Support",
                description = "Proactive monitoring and help when you need it.",
            ) { toast("Ongoing Support") },

            ServiceItem(
                title = "Request Service",
                description = "Form to get our services.",
            ) { startActivity(Intent(this, RequestServiceActivity::class.java)) },


            ServiceItem(
                title = "My Dashboard",
                description = "See your services.",
            ) { startActivity(Intent(this, MyDashboardActivity::class.java)) },
        )

        rv.adapter = ServiceAdapter(items)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
