package com.hardcoreamature.budgetcalculatoreno

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BudgetSummaryActivity : AppCompatActivity() {

    private lateinit var salaryTextView: TextView
    private lateinit var payFrequencyTextView: TextView
    private lateinit var budgetPlanTextView: TextView
    private lateinit var budgetResultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_summary)

        salaryTextView = findViewById(R.id.salaryTextView)
        payFrequencyTextView = findViewById(R.id.payFrequencyTextView)
        budgetPlanTextView = findViewById(R.id.budgetPlanTextView)
        budgetResultTextView = findViewById(R.id.budgetResultTextView)

        val salary = intent.getDoubleExtra("salary", 0.0)
        val payFrequency = intent.getStringExtra("payFrequency")
        val budgetPlan = intent.getStringExtra("budgetPlan")
        val budgetResult = intent.getStringExtra("budgetResult")
        val expenses =
            intent.getParcelableArrayListExtra("expenses", Expense::class.java)

        salaryTextView.text = getString(R.string.salary, salary)
        payFrequencyTextView.text = getString(R.string.pay_frequency, payFrequency)
        budgetPlanTextView.text = getString(R.string.budget_plan, budgetPlan)
        budgetResultTextView.text = budgetResult

        // Optional: Handling expenses if needed, otherwise remove this part
        val expensesText = expenses?.joinToString("\n") { expense ->
            "${expense.name}: $${expense.amount} (${expense.category})"
        } ?: getString(R.string.no_expenses)

        // If you want to display the expenses in budgetResultTextView, append the expensesText to budgetResult
        budgetResultTextView.text = "$budgetResult\n\n$expensesText"
    }
}
