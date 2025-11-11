package com.example.rm_project

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// ======================== Data Model ========================
data class RequestForm(
    val name: String,
    val company: String?,
    val email: String,
    val phone: String?,
    val services: String,
    val description: String?,
    val urgency: String
)

data class ApiResponse(val message: String)

// ======================== Retrofit API ========================
interface RequestApi {
    @POST("requests/new")
    suspend fun submitRequest(@Body request: RequestForm): ApiResponse
}

// ======================== Activity ========================
class RequestServiceActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etCompany: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var cbConsult: CheckBox
    private lateinit var cbAssessment: CheckBox
    private lateinit var cbProposal: CheckBox
    private lateinit var cbImplementation: CheckBox
    private lateinit var cbSupport: CheckBox
    private lateinit var etAdditional: EditText
    private lateinit var spUrgency: Spinner
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var api: RequestApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_service)

        // ======================== Initialize UI ========================
        etFullName = findViewById(R.id.etFullName)
        etCompany  = findViewById(R.id.etCompany)
        etEmail    = findViewById(R.id.etEmail)
        etPhone    = findViewById(R.id.etPhone)
        cbConsult  = findViewById(R.id.cbConsult)
        cbAssessment = findViewById(R.id.cbAssessment)
        cbProposal   = findViewById(R.id.cbProposal)
        cbImplementation = findViewById(R.id.cbImplementation)
        cbSupport    = findViewById(R.id.cbSupport)
        etAdditional = findViewById(R.id.etAdditional)
        spUrgency    = findViewById(R.id.spUrgency)
        btnSubmit    = findViewById(R.id.btnSubmit)
        progressBar  = findViewById(R.id.progressBarRequest)

        ArrayAdapter.createFromResource(
            this, R.array.urgency_options, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spUrgency.adapter = adapter
        }

        // ✅ Initialize API client (use your machine IP for emulator)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // ⚠️ Replace with your IPv4 or localhost for testing
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RequestApi::class.java)

        btnSubmit.setOnClickListener { submitForm() }
    }

    private fun submitForm() {
        val fullName = etFullName.text.toString().trim()
        val company  = etCompany.text.toString().trim()
        val email    = etEmail.text.toString().trim()
        val phone    = etPhone.text.toString().trim()
        val services = selectedServices()
        val urgency  = spUrgency.selectedItem?.toString() ?: ""
        val notes    = etAdditional.text.toString().trim()

        if (fullName.isEmpty()) { etFullName.error = "Required"; return }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Valid email required"; return
        }
        if (services.isEmpty()) {
            Toast.makeText(this, "Please select at least one service.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RequestForm(fullName, company, email, phone, services, notes, urgency)
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        // ======================== Submit to backend ========================
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { api.submitRequest(request) }
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true

                AlertDialog.Builder(this@RequestServiceActivity)
                    .setTitle("✅ Request Submitted")
                    .setMessage(response.message)
                    .setPositiveButton("OK") { d, _ -> d.dismiss(); clearForm() }
                    .show()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(
                    this@RequestServiceActivity,
                    "Error submitting request: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun selectedServices(): String {
        val list = mutableListOf<String>()
        if (cbConsult.isChecked) list += "Client Consultation"
        if (cbAssessment.isChecked) list += "IT Assessment"
        if (cbProposal.isChecked) list += "Solution Proposal"
        if (cbImplementation.isChecked) list += "Implementation"
        if (cbSupport.isChecked) list += "Ongoing Support"
        return list.joinToString(", ")
    }

    private fun clearForm() {
        etFullName.text.clear()
        etCompany.text.clear()
        etEmail.text.clear()
        etPhone.text.clear()
        cbConsult.isChecked = false
        cbAssessment.isChecked = false
        cbProposal.isChecked = false
        cbImplementation.isChecked = false
        cbSupport.isChecked = false
        etAdditional.text.clear()
        spUrgency.setSelection(0)
    }
}
