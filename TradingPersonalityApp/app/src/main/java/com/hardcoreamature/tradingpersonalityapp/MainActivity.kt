package com.hardcoreamature.tradingpersonalityapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Load AdView
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Check if user is signed in
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

        val profileButton: Button = findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val completeTaskButton: Button = findViewById(R.id.button_complete_task)
        completeTaskButton.setOnClickListener {
            completeTask(currentUser!!.uid)
        }

        val awardBadgeButton: Button = findViewById(R.id.button_award_badge)
        awardBadgeButton.setOnClickListener {
            awardBadge(currentUser!!.uid, getString(R.string.first_task_completed))
        }

        val awardAchievementButton: Button = findViewById(R.id.button_award_achievement)
        awardAchievementButton.setOnClickListener {
            awardAchievement(currentUser!!.uid, getString(R.string.first_achievement))
        }

        val dailyChallengeButton: Button = findViewById(R.id.button_daily_challenge)
        dailyChallengeButton.setOnClickListener {
            val intent = Intent(this, DailyChallengeActivity::class.java)
            startActivity(intent)
        }

        createUserDocumentIfNotExists(currentUser!!.uid)
    }

    private fun completeTask(uid: String) {
        val userRef = firestore.collection("users").document(uid)
        userRef.update("points", FieldValue.increment(10))
    }

    private fun awardBadge(uid: String, badge: String) {
        val userRef = firestore.collection("users").document(uid)
        userRef.update("badges", FieldValue.arrayUnion(badge))
    }

    private fun awardAchievement(uid: String, achievement: String) {
        val userRef = firestore.collection("users").document(uid)
        userRef.update("achievements", FieldValue.arrayUnion(achievement))
    }

    private fun createUserDocumentIfNotExists(uid: String) {
        val userRef = firestore.collection("users").document(uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                createUserDocument(uid)
            }
        }
    }

    private fun createUserDocument(uid: String) {
        val userRef = firestore.collection("users").document(uid)
        val userData = hashMapOf(
            "points" to 0,
            "badges" to listOf<String>(),
            "achievements" to listOf<String>()
        )
        userRef.set(userData)
    }
}
