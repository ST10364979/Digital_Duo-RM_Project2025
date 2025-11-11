package com.example.rm_project
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
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
import java.util.*

data class Meeting(
    val MeetingID: Int? = null,
    val ClientName: String,
    val Date: String,
    val Time: String,
    val Topic: String,
    val Notes: String?
)

data class MeetingResponse(val message: String)

interface MeetingApi {
    @GET("meetings/all")
    suspend fun getMeetings(): List<Meeting>

    @POST("meetings/create")
    suspend fun createMeeting(@Body meeting: Meeting): MeetingResponse

    @PUT("meetings/update/{id}")
    suspend fun updateMeeting(@Path("id") id: Int, @Body meeting: Meeting): MeetingResponse

    @DELETE("meetings/delete/{id}")
    suspend fun deleteMeeting(@Path("id") id: Int): MeetingResponse
}

class ScheduleMeetingsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var api: MeetingApi
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Scrollable layout
        val scrollView = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }
        scrollView.addView(container)
        setContentView(scrollView)

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            val params = LinearLayout.LayoutParams(100, 100)
            params.gravity = Gravity.CENTER
            layoutParams = params
        }
        container.addView(progressBar)

        // Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:3000/api/") // Replace with your IPv4
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(MeetingApi::class.java)

        // Load existing meetings
        loadMeetings()

        // Add “New Meeting” button
        val btnNew = Button(this).apply {
            text = "➕ Schedule New Meeting"
            setOnClickListener { openNewMeetingDialog() }
        }
        container.addView(btnNew)
    }

    private fun loadMeetings() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val meetings = withContext(Dispatchers.IO) { api.getMeetings() }
                progressBar.visibility = View.GONE
                displayMeetings(meetings)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ScheduleMeetingsActivity, "Error loading meetings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayMeetings(meetings: List<Meeting>) {
        container.removeAllViews()
        for (m in meetings) {
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
                text = "📅 ${m.Topic}"
                textSize = 18f
            }

            val details = TextView(this).apply {
                text = "Client: ${m.ClientName}\nDate: ${m.Date}\nTime: ${m.Time}\nNotes: ${m.Notes ?: "-"}"
                textSize = 14f
            }

            val editBtn = Button(this).apply {
                text = "✏️ Edit"
                setOnClickListener { openEditMeetingDialog(m) }
            }

            val deleteBtn = Button(this).apply {
                text = "🗑️ Delete"
                setOnClickListener { deleteMeeting(m.MeetingID ?: return@setOnClickListener) }
            }

            card.addView(title)
            card.addView(details)
            card.addView(editBtn)
            card.addView(deleteBtn)
            container.addView(card)
        }
    }

    private fun openNewMeetingDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val etClient = EditText(this).apply { hint = "Client Name" }
        val etTopic = EditText(this).apply { hint = "Meeting Topic" }
        val etNotes = EditText(this).apply { hint = "Notes (optional)" }

        val etDate = EditText(this).apply {
            hint = "Select Date"
            inputType = InputType.TYPE_NULL
            setOnClickListener { showDatePicker(this) }
        }

        val etTime = EditText(this).apply {
            hint = "Select Time"
            inputType = InputType.TYPE_NULL
            setOnClickListener { showTimePicker(this) }
        }

        dialogLayout.addView(etClient)
        dialogLayout.addView(etTopic)
        dialogLayout.addView(etDate)
        dialogLayout.addView(etTime)
        dialogLayout.addView(etNotes)

        AlertDialog.Builder(this)
            .setTitle("Schedule New Meeting")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val meeting = Meeting(
                    ClientName = etClient.text.toString(),
                    Topic = etTopic.text.toString(),
                    Date = etDate.text.toString(),
                    Time = etTime.text.toString(),
                    Notes = etNotes.text.toString()
                )
                saveMeeting(meeting)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditMeetingDialog(meeting: Meeting) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val etClient = EditText(this).apply { setText(meeting.ClientName) }
        val etTopic = EditText(this).apply { setText(meeting.Topic) }
        val etDate = EditText(this).apply {
            setText(meeting.Date)
            inputType = InputType.TYPE_NULL
            setOnClickListener { showDatePicker(this) }
        }
        val etTime = EditText(this).apply {
            setText(meeting.Time)
            inputType = InputType.TYPE_NULL
            setOnClickListener { showTimePicker(this) }
        }
        val etNotes = EditText(this).apply { setText(meeting.Notes ?: "") }

        dialogLayout.addView(etClient)
        dialogLayout.addView(etTopic)
        dialogLayout.addView(etDate)
        dialogLayout.addView(etTime)
        dialogLayout.addView(etNotes)

        AlertDialog.Builder(this)
            .setTitle("Edit Meeting")
            .setView(dialogLayout)
            .setPositiveButton("Update") { _, _ ->
                val updatedMeeting = Meeting(
                    MeetingID = meeting.MeetingID,
                    ClientName = etClient.text.toString(),
                    Topic = etTopic.text.toString(),
                    Date = etDate.text.toString(),
                    Time = etTime.text.toString(),
                    Notes = etNotes.text.toString()
                )
                updateMeeting(updatedMeeting)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(et: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day -> et.setText("$year-${month + 1}-$day") },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(et: EditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute -> et.setText(String.format("%02d:%02d", hour, minute)) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveMeeting(meeting: Meeting) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { api.createMeeting(meeting) }
                Toast.makeText(this@ScheduleMeetingsActivity, response.message, Toast.LENGTH_SHORT).show()
                loadMeetings()
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleMeetingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateMeeting(meeting: Meeting) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { api.updateMeeting(meeting.MeetingID!!, meeting) }
                Toast.makeText(this@ScheduleMeetingsActivity, response.message, Toast.LENGTH_SHORT).show()
                loadMeetings()
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleMeetingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteMeeting(id: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { api.deleteMeeting(id) }
                Toast.makeText(this@ScheduleMeetingsActivity, response.message, Toast.LENGTH_SHORT).show()
                loadMeetings()
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleMeetingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
