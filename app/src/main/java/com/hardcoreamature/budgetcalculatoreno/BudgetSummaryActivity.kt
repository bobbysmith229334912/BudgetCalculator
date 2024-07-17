package com.hardcoreamature.budgetcalculatoreno

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BudgetSummaryActivity : AppCompatActivity() {
    private lateinit var salaryTextView: TextView
    private lateinit var payFrequencyTextView: TextView
    private lateinit var budgetPlanTextView: TextView
    private lateinit var budgetResultTextView: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_summary)

        // Initialize views
        salaryTextView = findViewById(R.id.salaryTextView)
        payFrequencyTextView = findViewById(R.id.payFrequencyTextView)
        budgetPlanTextView = findViewById(R.id.budgetPlanTextView)
        budgetResultTextView = findViewById(R.id.budgetResultTextView)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)

        // Get data from Intent
        val salary = intent.getDoubleExtra("salary", 0.0)
        val payFrequency = intent.getStringExtra("payFrequency") ?: ""
        val budgetPlan = intent.getStringExtra("budgetPlan") ?: ""
        val budgetResult = intent.getStringExtra("budgetResult") ?: ""
        val expenses: ArrayList<Expense> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("expenses", Expense::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("expenses") ?: arrayListOf()
        }

        // Set data to views
        salaryTextView.text = getString(R.string.salary, salary)
        payFrequencyTextView.text = getString(R.string.pay_frequency, payFrequency)
        budgetPlanTextView.text = getString(R.string.budget_plan, budgetPlan)
        budgetResultTextView.text = budgetResult

        // Set up RecyclerView
        expensesAdapter = ExpensesAdapter(expenses.toMutableList()) { /* No deletion in summary */ }
        expensesRecyclerView.adapter = expensesAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
