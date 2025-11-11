package com.example.rm_project

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Request(
    val RequestID: Int,
    val ServiceType: String,
    val Status: String,
    val EstimatedCost: Double?,
    val CreatedAt: String
)

data class Meeting(
    val MeetingID: Int,
    val DateTime: String,
    val Agenda: String,
    val Admin: String
)

data class Document(
    val DocID: Int,
    val DocName: String,
    val FilePath: String
)

interface DashboardApi {
    @GET("requests/user/{email}")
    suspend fun getRequests(@Path("email") email: String): List<Request>

    @GET("meetings/{username}")
    suspend fun getMeetings(@Path("username") username: String): List<Meeting>

    @GET("documents/{username}")
    suspend fun getDocuments(@Path("username") username: String): List<Document>
}

class MyDashboardActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvSummary: TextView
    private lateinit var tvMeetings: TextView
    private lateinit var tvDocuments: TextView
    private lateinit var tvTimeline: TextView

    private lateinit var api: DashboardApi
    private val userEmail = "mokgadi@example.com" // ⚠️ Replace with actual logged-in user's email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createLayout())

        // Back button (if you add one later)
        // findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // ✅ Initialize Retrofit API
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // ⚠️ Replace with your PC IPv4
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(DashboardApi::class.java)

        // Load dashboard data
        loadDashboardData()
    }

    private fun createLayout(): View {
        val root = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 64)
        }

        tvSummary = TextView(this).apply {
            text = "Service Summary: Loading..."
            textSize = 18f
        }
        tvMeetings = TextView(this).apply {
            text = "Scheduled Meetings: Loading..."
            textSize = 18f
        }
        tvDocuments = TextView(this).apply {
            text = "Service Documents: Loading..."
            textSize = 18f
        }
        tvTimeline = TextView(this).apply {
            text = "Service Timeline: Loading..."
            textSize = 18f
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        content.addView(progressBar)
        content.addView(tvSummary)
        content.addView(tvMeetings)
        content.addView(tvDocuments)
        content.addView(tvTimeline)
        root.addView(content)
        return root
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val requests = withContext(Dispatchers.IO) { api.getRequests(userEmail) }
                val meetings = withContext(Dispatchers.IO) { api.getMeetings(userEmail) }
                val documents = withContext(Dispatchers.IO) { api.getDocuments(userEmail) }

                progressBar.visibility = View.GONE
                updateUI(requests, meetings, documents)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MyDashboardActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(requests: List<Request>, meetings: List<Meeting>, documents: List<Document>) {
        // 🟢 Service Summary
        val summaryText = if (requests.isNotEmpty()) {
            requests.joinToString("\n") { req ->
                "• ${req.ServiceType} — ${req.Status} (${req.EstimatedCost ?: 0.0})"
            }
        } else "No active service requests."
        tvSummary.text = "🧾 Service Summary:\n$summaryText"

        // 🟢 Meetings
        val meetingsText = if (meetings.isNotEmpty()) {
            meetings.joinToString("\n") { m ->
                "• ${m.DateTime} — ${m.Agenda} (by ${m.Admin})"
            }
        } else "No meetings scheduled."
        tvMeetings.text = "📅 Scheduled Meetings:\n$meetingsText"

        // 🟢 Documents
        val docText = if (documents.isNotEmpty()) {
            documents.joinToString("\n") { d ->
                "• ${d.DocName} — ${d.FilePath}"
            }
        } else "No documents uploaded."
        tvDocuments.text = "📂 Service Documents:\n$docText"

        // 🟢 Timeline
        val timelineText = if (requests.isNotEmpty()) {
            requests.joinToString("\n") { r ->
                "• ${r.ServiceType}: ${r.Status} → Updated on ${r.CreatedAt}"
            }
        } else "No service timeline data."
        tvTimeline.text = "⏳ Service Timeline:\n$timelineText"
    }
}
