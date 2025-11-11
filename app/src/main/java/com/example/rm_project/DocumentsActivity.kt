package com.example.rm_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentsActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etDocName: EditText
    private lateinit var tvFilePath: TextView
    private lateinit var btnChooseFile: Button
    private lateinit var btnUpload: Button
    private lateinit var docsContainer: LinearLayout
    private var selectedFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        etUsername = findViewById(R.id.etUsername)
        etDocName = findViewById(R.id.etDocName)
        tvFilePath = findViewById(R.id.tvFilePath)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnUpload = findViewById(R.id.btnUpload)
        docsContainer = findViewById(R.id.docsContainer)

        val api = ApiClient.instance.create(ApiService::class.java)

        // 📁 Choose File
        btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, 101)
        }

        // 📤 Upload File
        btnUpload.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val docName = etDocName.text.toString().trim()

            if (username.isEmpty() || docName.isEmpty() || selectedFilePath == null) {
                Toast.makeText(this, "Please fill all fields and select a file.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = DocumentUploadRequest(username, docName, selectedFilePath!!)
            api.uploadDocument(request).enqueue(object : Callback<DocumentResponse> {
                override fun onResponse(call: Call<DocumentResponse>, response: Response<DocumentResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DocumentsActivity, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                        fetchDocuments(username)
                    } else {
                        Toast.makeText(this@DocumentsActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                    Toast.makeText(this@DocumentsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Handle file picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            selectedFilePath = uri?.path ?: "Unknown"
            tvFilePath.text = "Selected: $selectedFilePath"
        }
    }

    private fun fetchDocuments(username: String) {
        val api = ApiClient.instance.create(ApiService::class.java)
        api.getDocuments(username).enqueue(object : Callback<List<DocumentItem>> {
            override fun onResponse(call: Call<List<DocumentItem>>, response: Response<List<DocumentItem>>) {
                if (response.isSuccessful) {
                    val docs = response.body() ?: emptyList()
                    docsContainer.removeAllViews()
                    for (doc in docs) {
                        val tv = TextView(this@DocumentsActivity).apply {
                            text = "• ${doc.DocName}"
                            textSize = 16f
                            setPadding(8, 8, 8, 8)
                            setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.FilePath))
                                startActivity(intent)
                            }
                        }
                        docsContainer.addView(tv)
                    }
                }
            }

            override fun onFailure(call: Call<List<DocumentItem>>, t: Throwable) {
                Toast.makeText(this@DocumentsActivity, "Error loading documents", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
