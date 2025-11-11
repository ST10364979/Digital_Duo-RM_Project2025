package com.example.rm_project

// 🟩 Used for registering a new user
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val adminCode: String?
)

// 🟩 Generic message response from server (used for register, update, etc.)
data class GenericResponse(
    val message: String
)

// 🟩 Used for login requests
data class LoginRequest(
    val username: String,
    val password: String
)

// 🟩 Used for login responses
data class LoginResponse(
    val message: String,
    val role: String?
)
data class DocumentUploadRequest(
    val Username: String,
    val DocName: String,
    val FilePath: String
)

data class DocumentResponse(
    val message: String
)

data class DocumentItem(
    val DocID: Int,
    val Username: String,
    val DocName: String,
    val FilePath: String,
    val UploadedAt: String
)
