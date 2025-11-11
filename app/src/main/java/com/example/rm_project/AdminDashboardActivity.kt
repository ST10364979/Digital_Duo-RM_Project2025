package com.example.rm_project



import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rm_project.adapters.ServiceAdapter
import com.example.rm_project.models.ServiceItem

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val rv = findViewById<RecyclerView>(R.id.rvAdmin)
        rv.layoutManager = GridLayoutManager(this, 2)

        val items = listOf(
            ServiceItem("Manage Users", "Add, remove, and assign roles.") {
                startActivity(Intent(this, ManageUsersActivity::class.java))
            },
            ServiceItem("Review Requests", "View and approve service requests.") {
                startActivity(Intent(this, ReviewRequestsActivity::class.java))
            },
            ServiceItem("Schedule Meetings", "Book or edit client meetings.") {
                startActivity(Intent(this, ScheduleMeetingsActivity::class.java))
            },
            ServiceItem("Documents", "Upload & share client documents.") {
                startActivity(Intent(this, DocumentsActivity::class.java))
            },
            ServiceItem("Reports", "View KPIs and export summaries.") {
                startActivity(Intent(this, ReportsActivity::class.java))
            },
            ServiceItem("Settings", "Branding, notifications, access.") {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        )

        rv.adapter = ServiceAdapter(items)
    }

    // optional helper
    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

