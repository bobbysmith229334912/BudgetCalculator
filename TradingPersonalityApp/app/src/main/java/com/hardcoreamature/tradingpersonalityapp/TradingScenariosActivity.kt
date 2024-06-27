package com.hardcoreamature.tradingpersonalityapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class TradingScenariosActivity : AppCompatActivity() {

    private var step = 0
    private val answers = mutableListOf<String>()

    private val scenarios = listOf(
        "You buy \$1000 worth of stock. The price drops to \$900. What do you do?",
        "The price just went to \$700. What do you do?",
        "The price just went to \$500. What do you do?",
        "The price just went to \$300. What do you do?",
        "The price just went to \$100. What do you do?",
        "You just lost 90% of your money. What do you do?"
    )

    private lateinit var scenarioTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var buyButton: Button
    private lateinit var sellButton: Button
    private lateinit var holdButton: Button
    private lateinit var animationView: LottieAnimationView

    private var countDownTimer: CountDownTimer? = null
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trading_scenarios)

        scenarioTextView = findViewById(R.id.text_view_scenario)
        timerTextView = findViewById(R.id.text_view_timer)
        buyButton = findViewById(R.id.button_buy)
        sellButton = findViewById(R.id.button_sell)
        holdButton = findViewById(R.id.button_hold)
        animationView = findViewById(R.id.animation_view)

        sharedPreferences = getSharedPreferences("TradingApp", Context.MODE_PRIVATE)
        mediaPlayer = MediaPlayer.create(this, R.raw.correct_sound)

        startScenario()
    }

    private fun startScenario() {
        if (step >= scenarios.size) {
            evaluateResponses()
            return
        }

        scenarioTextView.text = scenarios[step]
        startTimer()

        buyButton.setOnClickListener {
            answers.add("Buy")
            provideFeedback()
            nextScenario()
        }

        sellButton.setOnClickListener {
            answers.add("Sell")
            provideFeedback()
            nextScenario()
        }

        holdButton.setOnClickListener {
            answers.add("Hold")
            provideFeedback()
            nextScenario()
        }
    }

    private fun nextScenario() {
        step++
        countDownTimer?.cancel()
        startScenario()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                Toast.makeText(this@TradingScenariosActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                answers.add("No Answer")
                nextScenario()
            }
        }.start()
    }

    private fun provideFeedback() {
        mediaPlayer.start()
        animationView.setAnimation("correct.json") // Use the same animation for all answers
        animationView.playAnimation()
    }

    private fun evaluateResponses() {
        val shortTermResponses = answers.count { it == "Sell" }
        val longTermResponses = answers.count { it == "Buy" || it == "Hold" }

        val result = when {
            shortTermResponses > longTermResponses -> "You are a short-term trader."
            longTermResponses > shortTermResponses -> "You are a long-term trader."
            else -> "You have a balanced approach to trading."
        }

        saveResult(result)

        AlertDialog.Builder(this)
            .setTitle("Trading Personality Result")
            .setMessage(result)
            .setPositiveButton("OK") { _, _ ->
                checkSavingsAndPromptForTips(result)
            }
            .show()
    }

    private fun saveResult(result: String) {
        val editor = sharedPreferences.edit()
        editor.putString("LastResult", result)
        editor.apply()
    }

    private fun checkSavingsAndPromptForTips(result: String) {
        val currentSavings = sharedPreferences.getFloat("CurrentSavings", 0f)
        if (currentSavings < 20000) {
            AlertDialog.Builder(this)
                .setTitle("Financial Tips")
                .setMessage("Your current savings are below \$20,000. Would you like to receive tips on how to save more?")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, FinancialTipsActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("No") { _, _ ->
                    showTips(result)
                }
                .show()
        } else {
            showTips(result)
        }
    }

    private fun showTips(result: String) {
        val tips = when (result) {
            "You are a short-term trader." -> "As a short-term trader, consider using stop-loss orders to protect your investments. Stay updated with market news and be prepared for quick decisions."
            "You are a long-term trader." -> "As a long-term trader, focus on the fundamentals of the companies you invest in. Diversify your portfolio and be patient with market fluctuations."
            "You have a balanced approach to trading." -> "With a balanced approach, you benefit from both worlds. Continue to diversify your investments and be mindful of both short-term and long-term strategies."
            else -> "Keep learning and improving your trading skills. Consider reading books on trading strategies and practicing with demo accounts."
        }

        AlertDialog.Builder(this)
            .setTitle("Tips for Your Trading Style")
            .setMessage(tips)
            .setPositiveButton("Got it") { _, _ -> finish() }
            .show()
    }
}
