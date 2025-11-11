package com.example.rm_project

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvForgot: TextView
    private lateinit var tvRegister: TextView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        cbRemember = findViewById(R.id.cbRemember)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgot = findViewById(R.id.tvForgot)
        tvRegister = findViewById(R.id.tvRegister)

        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)

        // Restore "remember me"
        val savedUser = prefs.getString("username", null)
        val savedPass = prefs.getString("password", null)
        val remember = prefs.getBoolean("remember", false)
        if (remember && !savedUser.isNullOrBlank() && !savedPass.isNullOrBlank()) {
            etUsername.setText(savedUser)
            etPassword.setText(savedPass)
            cbRemember.isChecked = true
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Password reset coming soon.", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val api = ApiClient.instance.create(ApiService::class.java)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(username, password)
            api.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        val role = res?.role ?: "User"

                        Toast.makeText(this@LoginActivity, res?.message ?: "Login successful", Toast.LENGTH_SHORT).show()

                        // Save Remember Me
                        if (cbRemember.isChecked) {
                            prefs.edit().apply {
                                putString("username", username)
                                putString("password", password)
                                putBoolean("remember", true)
                                apply()
                            }
                        } else {
                            prefs.edit().clear().apply()
                        }

                        // Navigate based on role
                        if (role.equals("Admin", true)) {
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this@LoginActivity, UserDashboardActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
