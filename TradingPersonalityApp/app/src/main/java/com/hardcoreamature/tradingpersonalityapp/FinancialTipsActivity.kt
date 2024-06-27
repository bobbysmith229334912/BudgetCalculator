package com.hardcoreamature.tradingpersonalityapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FinancialTipsActivity : AppCompatActivity() {

    private lateinit var incomeEditText: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var perfectLifeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_tips)

        incomeEditText = findViewById(R.id.edit_text_income)
        calculateButton = findViewById(R.id.button_calculate)
        resultTextView = findViewById(R.id.text_view_result)
        perfectLifeButton = findViewById(R.id.button_perfect_life)

        calculateButton.isEnabled = false

        incomeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateButton.isEnabled = s?.isNotEmpty() == true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        calculateButton.setOnClickListener {
            val income = incomeEditText.text.toString().toFloatOrNull()
            if (income != null) {
                showBudgetOptions(income)
            } else {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.invalid_income_input))
                    .setMessage(getString(R.string.please_enter_valid_income))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

        perfectLifeButton.setOnClickListener {
            val intent = Intent(this, PerfectLifeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showBudgetOptions(income: Float) {
        val options = arrayOf(getString(R.string.dave_ramsey_system), getString(R.string.percentage_system))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_budgeting_system))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDaveRamseySystem(income)
                    1 -> showPercentageSystem(income)
                }
            }
            .show()
    }

    private fun showDaveRamseySystem(income: Float) {
        val needs = income * 0.50
        val wants = income * 0.30
        val savings = income * 0.20
        val result = getString(R.string.dave_ramsey_result_format, needs, wants, savings)
        resultTextView.text = result
        showAlert(getString(R.string.budget_calculated))
        perfectLifeButton.visibility = Button.VISIBLE
    }

    private fun showPercentageSystem(income: Float) {
        val savings = income * 0.33
        val livingExpenses = income * 0.33
        val entertainment = income * 0.34
        val result = getString(R.string.percentage_result_format, savings, livingExpenses, entertainment)
        resultTextView.text = result
        showAlert(getString(R.string.budget_calculated))
        perfectLifeButton.visibility = Button.VISIBLE
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.result))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}


