package com.hardcoreamature.tradingpersonalityapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FinancialAssessmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_assessment)

        val emergencyFundInput: EditText = findViewById(R.id.edit_text_emergency_fund)
        val savingsInput: EditText = findViewById(R.id.edit_text_savings)
        val submitButton: Button = findViewById(R.id.button_submit)

        submitButton.setOnClickListener {
            val emergencyFund = emergencyFundInput.text.toString().toDoubleOrNull()
            val savings = savingsInput.text.toString().toDoubleOrNull()

            if (emergencyFund == null || savings == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalAmount = emergencyFund + savings

            if (totalAmount < 20000) {
                AlertDialog.Builder(this)
                    .setTitle("Financial Assessment")
                    .setMessage("Your combined savings and emergency fund are less than $20,000. It's important to build a solid financial foundation before trading.")
                    .setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(this, TradingScenariosActivity::class.java))
                    }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Financial Assessment")
                    .setMessage("You have a good financial foundation to start trading.")
                    .setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(this, TradingScenariosActivity::class.java))
                    }
                    .show()
            }
        }
    }
}