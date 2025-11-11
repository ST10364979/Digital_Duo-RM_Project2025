package com.example.rm_project

import android.os.Bundle
import android.view.Gravity
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

data class ServiceRequest(
    val RequestID: Int,
    val Username: String?,
    val ServiceType: String?,
    val Description: String?,
    val Status: String?,
    val AssignedTo: String?,
    val EstimatedCost: Double?
)

data class UpdateRequestBody(
    val RequestID: Int,
    val AssignedTo: String?,
    val Status: String?,
    val EstimatedCost: Double?
)

// ⚠️ Remove ApiResponse here if already defined in ApiService.kt
// data class ApiResponse(val message: String)

interface RequestsApi {
    @GET("requests/all")
    suspend fun getAllRequests(): List<ServiceRequest>

    @PUT("requests/update")
    suspend fun updateRequest(@Body update: UpdateRequestBody): ApiResponse
}

class ReviewRequestsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var api: RequestsApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 48, 24, 48)
        }
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            isIndeterminate = true
            val params = LinearLayout.LayoutParams(100, 100)
            params.gravity = Gravity.CENTER
            layoutParams = params
        }

        scroll.addView(container)
        setContentView(scroll)
        container.addView(progressBar)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // Replace with your IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RequestsApi::class.java)
        loadRequests()
    }

    private fun loadRequests() {
        progressBar.visibility = View.VISIBLE
        container.removeAllViews()
        container.addView(progressBar)

        lifecycleScope.launch {
            try {
                val requests = withContext(Dispatchers.IO) { api.getAllRequests() }
                progressBar.visibility = View.GONE

                if (requests.isEmpty()) {
                    container.addView(TextView(this@ReviewRequestsActivity).apply {
                        text = "No service requests found."
                        textSize = 18f
                    })
                } else {
                    for (req in requests) {
                        container.addView(createRequestCard(req))
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ReviewRequestsActivity, "Error loading requests: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createRequestCard(req: ServiceRequest): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 30)
            layoutParams = params
        }

        val title = TextView(this).apply {
            text = "📄 Request #${req.RequestID} — ${req.ServiceType ?: "Unknown"}"
            textSize = 18f
            setPadding(0, 0, 0, 6)
        }

        val details = TextView(this).apply {
            text = "Client: ${req.Username}\n" +
                    "Description: ${req.Description ?: "-"}\n" +
                    "Status: ${req.Status ?: "Pending"}\n" +
                    "Assigned To: ${req.AssignedTo ?: "None"}\n" +
                    "Estimated Cost: R${req.EstimatedCost ?: 0.0}"
            textSize = 14f
        }

        val etAssign = EditText(this).apply {
            hint = "Assign to employee"
            setText(req.AssignedTo ?: "")
        }

        val etCost = EditText(this).apply {
            hint = "Enter cost (R)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText((req.EstimatedCost ?: 0.0).toString())
        }

        val spStatus = Spinner(this)
        val statuses = arrayOf("Pending", "In Progress", "Completed")
        spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        spStatus.setSelection(statuses.indexOf(req.Status ?: "Pending"))

        val btnUpdate = Button(this).apply {
            text = "Update Request"
            setOnClickListener {
                val assigned = etAssign.text.toString()
                val newStatus = spStatus.selectedItem.toString()
                val cost = etCost.text.toString().toDoubleOrNull() ?: 0.0
                updateRequest(req.RequestID, assigned, newStatus, cost)
            }
        }

        card.addView(title)
        card.addView(details)
        card.addView(etAssign)
        card.addView(etCost)
        card.addView(spStatus)
        card.addView(btnUpdate)
        return card
    }

    private fun updateRequest(id: Int, assignedTo: String, status: String, cost: Double) {
        lifecycleScope.launch {
            try {
                val update = UpdateRequestBody(id, assignedTo, status, cost)
                val result = withContext(Dispatchers.IO) { api.updateRequest(update) }
                Toast.makeText(this@ReviewRequestsActivity, result.message, Toast.LENGTH_SHORT).show()
                loadRequests()
            } catch (e: Exception) {
                Toast.makeText(this@ReviewRequestsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
