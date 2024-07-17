package com.hardcoreamature.budgetcalculatoreno

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var apiSecretInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var showButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var isApiSecretVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        apiKeyInput = findViewById(R.id.apiKeyInput)
        apiSecretInput = findViewById(R.id.apiSecretInput)
        confirmButton = findViewById(R.id.confirmButton)
        showButton = findViewById(R.id.showButton)

        // Load saved API keys if they exist
        apiKeyInput.setText(sharedPreferences.getString("kraken_api_key", ""))
        apiSecretInput.setText(sharedPreferences.getString("kraken_api_secret", ""))

        confirmButton.setOnClickListener {
            val apiKey = apiKeyInput.text.toString()
            val apiSecret = apiSecretInput.text.toString()

            if (apiKey.isNotEmpty() && apiSecret.isNotEmpty()) {
                sharedPreferences.edit()
                    .putString("kraken_api_key", apiKey)
                    .putString("kraken_api_secret", apiSecret)
                    .apply()

                Toast.makeText(this, "API keys saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter both API key and secret", Toast.LENGTH_SHORT).show()
            }
        }

        showButton.setOnClickListener {
            isApiSecretVisible = !isApiSecretVisible
            if (isApiSecretVisible) {
                apiSecretInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                showButton.text = getString(R.string.hide)
            } else {
                apiSecretInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                showButton.text = getString(R.string.show)
            }
            apiSecretInput.setSelection(apiSecretInput.text.length)
        }
    }
}
