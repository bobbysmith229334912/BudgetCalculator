package com.hardcoreamature.budgetcalculatoreno

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val signUpButton: Button = findViewById(R.id.button_sign_up)
        val emailEditText: EditText = findViewById(R.id.edit_text_email)
        val passwordEditText: EditText = findViewById(R.id.edit_text_password)
        val confirmPasswordEditText: EditText = findViewById(R.id.edit_text_confirm_password)
        val signInTextView: TextView = findViewById(R.id.text_view_sign_in)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    signUp(email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        signInTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Registration successful, save user progress and navigate to main activity
                saveUserProgressAndNavigate()
            } else {
                val exception = task.exception
                if (exception is FirebaseAuthUserCollisionException) {
                    // Email already in use, redirect to login screen with a message
                    Toast.makeText(this, "Email already registered. Please log in.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed for another reason
                    Toast.makeText(this, "Sign up failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserProgressAndNavigate() {
        val user = auth.currentUser
        val userProgress = UserProgress(userId = user!!.uid)
        firestore.collection("users").document(user.uid).set(userProgress)
            .addOnSuccessListener {
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                // Handle the failure case
                e.printStackTrace()
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    data class UserProgress(
        val userId: String
    )
}
