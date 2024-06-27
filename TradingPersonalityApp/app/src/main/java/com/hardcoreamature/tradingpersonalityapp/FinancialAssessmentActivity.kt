package com.hardcoreamature.tradingpersonalityapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FinancialAssessmentActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var assessmentButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_assessment)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        assessmentButton = findViewById(R.id.button_complete_assessment)
        resultTextView = findViewById(R.id.text_view_assessment_result)

        assessmentButton.setOnClickListener {
            completeFinancialAssessment()
        }
    }

    private fun completeFinancialAssessment() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Perform the financial assessment logic here
            val assessmentResult = "Your financial assessment is complete!"

            // Display the assessment result
            resultTextView.text = assessmentResult

            // Check if the user already has the "First Task Completed" achievement
            db.collection("achievements")
                .whereEqualTo("userId", userId)
                .whereEqualTo("achievementId", "first_task_completed")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Award the "First Task Completed" achievement
                        val achievement = hashMapOf(
                            "userId" to userId,
                            "achievementId" to "first_task_completed",
                            "title" to getString(R.string.first_task_completed),
                            "dateEarned" to System.currentTimeMillis()
                        )
                        db.collection("achievements")
                            .add(achievement)
                            .addOnSuccessListener {
                                // Achievement awarded successfully
                                resultTextView.text = "$assessmentResult\n${getString(R.string.first_task_completed_awarded)}"
                            }
                            .addOnFailureListener {
                                // Failed to award achievement
                                resultTextView.text = "$assessmentResult\n${getString(R.string.failed_to_award_achievement)}"
                            }
                    } else {
                        // Achievement already awarded
                        resultTextView.text = "$assessmentResult\n${getString(R.string.first_task_completed_already_awarded)}"
                    }
                }
        }
    }
}
