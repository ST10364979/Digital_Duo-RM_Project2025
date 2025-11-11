package com.example.rm_project

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spRole: Spinner
    private lateinit var etAdminCode: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ====================== INITIALIZE VIEWS ======================
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirm)
        spRole = findViewById(R.id.rgRole)
        etAdminCode = findViewById(R.id.etAdminCode)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLoginHere)

        // Populate Spinner for roles
        val roles = arrayOf("User", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spRole.adapter = adapter

        // Hide Admin Code by default
        etAdminCode.visibility = android.view.View.GONE

        spRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                etAdminCode.visibility = if (roles[position] == "Admin") android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ====================== NAVIGATION ======================
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ====================== BACKEND CONNECTION ======================
        val api = ApiClient.instance.create(ApiService::class.java)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val role = spRole.selectedItem.toString()
            val adminCode = etAdminCode.text.toString().trim()

            // Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role == "Admin" && adminCode != "RM-ADMIN-2025") {
                Toast.makeText(this, "Invalid admin code!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare API call
            val request = RegisterRequest(username, email, password, role, adminCode)

            api.register(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        Toast.makeText(this@RegisterActivity, res?.message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
