package com.example.esp32chat

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    // Biến để lưu trữ địa chỉ IP của ESP32
    private val esp32IpAddress: String = "http://<IP-ESP32>/post"

    // Khai báo các view
    private lateinit var buttonSend: Button
    private lateinit var editTextMessage: EditText
    private lateinit var textViewSent: TextView
    private lateinit var textViewReceived: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Thiết lập layout

        // Khởi tạo StrictMode để cho phép thực thi tác vụ mạng trên main thread
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Khai báo và gán các view bằng findViewById
        buttonSend = findViewById(R.id.buttonSend)
        editTextMessage = findViewById(R.id.editTextMessage)
        textViewSent = findViewById(R.id.textViewSent)
        textViewReceived = findViewById(R.id.textViewReceived)

        // Khai báo sự kiện khi nhấn nút Gửi
        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendPostRequest(message) // Gửi tin nhắn đến ESP32
            } else {
                textViewReceived.text = "Vui lòng nhập tin nhắn" // Thông báo nếu tin nhắn trống
            }
        }
    }

    private fun sendPostRequest(message: String) {
        try {
            // Hiển thị tin nhắn đã gửi lên TextView
            textViewSent.text = message

            // Thiết lập kết nối đến ESP32
            val url = URL(esp32IpAddress)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "text/plain")
            urlConnection.doOutput = true

            // Gửi tin nhắn đến ESP32
            val outputStream: OutputStream = urlConnection.outputStream
            outputStream.write(message.toByteArray())
            outputStream.flush()
            outputStream.close()

            // Nhận phản hồi từ ESP32
            val inputStream = BufferedReader(InputStreamReader(urlConnection.inputStream))
            val response = StringBuilder()
            var inputLine: String?
            while (inputStream.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            inputStream.close()

            // Cập nhật TextView để hiển thị phản hồi
            textViewReceived.text = response.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            textViewReceived.text = "Lỗi kết nối đến ESP32" // Thông báo lỗi kết nối
        }
    }
}