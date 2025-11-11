package com.example.rm_project.models

data class ServiceItem(
    val title: String,
    val description: String,
    val action: () -> Unit = {}
)