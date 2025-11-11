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

data class Employee(
    val EmployeeID: Int,
    val Name: String,
    val Email: String,
    var Role: String
)

interface EmployeeApi {
    @GET("employees")
    suspend fun getEmployees(): List<Employee>

    @PUT("employees/promote")
    suspend fun updateRole(@Body request: Map<String, Any>): Map<String, String>
}

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var employeeListView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var api: EmployeeApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createLayout())

        // ✅ Initialize API connection
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // ⚠️ change IP to your PC’s IPv4
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(EmployeeApi::class.java)

        // Load employees from backend
        loadEmployees()
    }

    private fun createLayout(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Manage Employees"
            textSize = 22f
        }
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        employeeListView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(title)
        root.addView(progressBar)
        root.addView(employeeListView)
        return root
    }

    private fun loadEmployees() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val employees = withContext(Dispatchers.IO) { api.getEmployees() }
                progressBar.visibility = View.GONE
                showEmployees(employees)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ManageUsersActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEmployees(employees: List<Employee>) {
        employeeListView.removeAllViews()

        for (emp in employees) {
            val empLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
            }

            val nameText = TextView(this).apply {
                text = "${emp.Name} (${emp.Role})"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val promoteBtn = Button(this).apply {
                text = "Promote"
                setOnClickListener { updateEmployeeRole(emp.EmployeeID, "Manager") }
            }

            val demoteBtn = Button(this).apply {
                text = "Demote"
                setOnClickListener { updateEmployeeRole(emp.EmployeeID, "Staff") }
            }

            empLayout.addView(nameText)
            empLayout.addView(promoteBtn)
            empLayout.addView(demoteBtn)

            employeeListView.addView(empLayout)
        }
    }

    private fun updateEmployeeRole(employeeId: Int, newRole: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.updateRole(mapOf("id" to employeeId, "role" to newRole))
                }
                Toast.makeText(this@ManageUsersActivity, response["message"] ?: "Updated!", Toast.LENGTH_SHORT).show()
                loadEmployees() // Refresh list
            } catch (e: Exception) {
                Toast.makeText(this@ManageUsersActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
