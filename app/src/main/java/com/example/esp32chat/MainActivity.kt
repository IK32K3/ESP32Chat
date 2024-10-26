package com.example.esp32chat

import android.os.Bundle
import android.os.StrictMode
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    // Địa chỉ IP của ESP32 khi nó hoạt động ở chế độ AP
    private val esp32IpAddress: String = "http://192.168.4.1"

    // Khai báo các view
    private lateinit var buttonSend: Button
    private lateinit var editTextMessage: EditText
    private lateinit var textViewReceived: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Thiết lập StrictMode để cho phép hoạt động mạng
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Gán các view chính
        buttonSend = findViewById(R.id.buttonSend)
        editTextMessage = findViewById(R.id.editTextMessage)
        textViewReceived = findViewById(R.id.textViewReceived)

        // Xử lý sự kiện gửi tin nhắn
        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendPostRequest(message)

                // Ẩn bàn phím sau khi gửi
                hideKeyboard()
            } else {
                textViewReceived.text = "Vui lòng nhập tin nhắn"
            }
        }
    }

    private fun sendPostRequest(message: String) {
        try {
            // Thiết lập kết nối đến ESP32
            val url = URL("$esp32IpAddress/")
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

            // Hiển thị phản hồi
            textViewReceived.text = response.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            textViewReceived.text = "Lỗi kết nối đến ESP32"
        }
    }

    private fun hideKeyboard() {
        // Ẩn bàn phím sau khi nhấn nút gửi
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextMessage.windowToken, 0)
    }
}
