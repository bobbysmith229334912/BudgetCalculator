package com.hardcoreamature.tradingpersonalityapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user is signed in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        val financialAssessmentButton: Button = findViewById(R.id.button_financial_assessment)
        financialAssessmentButton.setOnClickListener {
            val intent = Intent(this, FinancialAssessmentActivity::class.java)
            startActivity(intent)
        }
    }
}
