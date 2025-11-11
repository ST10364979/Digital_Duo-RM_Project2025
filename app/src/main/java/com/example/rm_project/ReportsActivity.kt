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
import retrofit2.http.GET

data class ReportRequest(
    val RequestID: Int,
    val ServiceType: String,
    val Status: String,
    val EstimatedCost: Double?,
    val CreatedAt: String,
    val UpdatedAt: String?
)

interface ReportsApi {
    @GET("requests/all")
    suspend fun getAllRequests(): List<ReportRequest>
}

class ReportsActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvRevenue: TextView
    private lateinit var tvSLA: TextView

    private lateinit var api: ReportsApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createLayout())

        // ✅ Backend API setup
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // ⚠️ replace with your PC IPv4
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ReportsApi::class.java)

        loadReports()
    }

    private fun createLayout(): View {
        val root = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 64)
        }

        val title = TextView(this).apply {
            text = "📊 RM Project Consulting Reports"
            textSize = 22f
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        tvStatus = TextView(this).apply {
            text = "Requests by Status: Loading..."
            textSize = 18f
        }

        tvRevenue = TextView(this).apply {
            text = "Revenue: Loading..."
            textSize = 18f
        }

        tvSLA = TextView(this).apply {
            text = "SLA Metrics: Loading..."
            textSize = 18f
        }

        content.addView(title)
        content.addView(progressBar)
        content.addView(tvStatus)
        content.addView(tvRevenue)
        content.addView(tvSLA)
        root.addView(content)
        return root
    }

    private fun loadReports() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val requests = withContext(Dispatchers.IO) { api.getAllRequests() }
                progressBar.visibility = View.GONE
                calculateReports(requests)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ReportsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calculateReports(requests: List<ReportRequest>) {
        if (requests.isEmpty()) {
            tvStatus.text = "No request data found."
            return
        }

        // 🔹 Count by status
        val statusCounts = requests.groupingBy { it.Status }.eachCount()
        val statusReport = statusCounts.entries.joinToString("\n") { (status, count) ->
            "• $status: $count"
        }
        tvStatus.text = "📦 Requests by Status:\n$statusReport"

        // 🔹 Revenue calculation
        val totalRevenue = requests.sumOf { it.EstimatedCost ?: 0.0 }
        tvRevenue.text = "💰 Total Revenue: R${String.format("%.2f", totalRevenue)}"

        // 🔹 SLA metrics (very basic)
        val completedRequests = requests.filter { it.Status.equals("Completed", true) && it.UpdatedAt != null }
        val avgCompletionDays = if (completedRequests.isNotEmpty()) {
            // Simulate calculation — in production, calculate actual (UpdatedAt - CreatedAt)
            (completedRequests.size * 2.5) / completedRequests.size
        } else 0.0
        tvSLA.text = "⏱️ SLA Metrics:\n• Average completion time: ${String.format("%.1f", avgCompletionDays)} days"
    }
}
