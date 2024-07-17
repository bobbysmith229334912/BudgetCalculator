package com.hardcoreamature.budgetcalculatoreno

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var salaryInput: EditText
    private lateinit var payFrequencySpinner: Spinner
    private lateinit var budgetPlanSpinner: Spinner
    private lateinit var stateTaxSpinner: Spinner
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var expenseNameInput: EditText
    private lateinit var expenseAmountInput: EditText
    private lateinit var expenseCategorySpinner: Spinner
    private lateinit var addExpenseButton: Button
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var summaryButton: Button
    private lateinit var explainButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        salaryInput = findViewById(R.id.salaryInput)
        payFrequencySpinner = findViewById(R.id.payFrequencySpinner)
        budgetPlanSpinner = findViewById(R.id.budgetPlanSpinner)
        stateTaxSpinner = findViewById(R.id.stateTaxSpinner)
        calculateButton = findViewById(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)
        expenseNameInput = findViewById(R.id.expenseNameInput)
        expenseAmountInput = findViewById(R.id.expenseAmountInput)
        expenseCategorySpinner = findViewById(R.id.expenseCategorySpinner)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        saveButton = findViewById(R.id.saveButton)
        loadButton = findViewById(R.id.loadButton)
        summaryButton = findViewById(R.id.summaryButton)
        explainButton = findViewById(R.id.explainButton)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)

        // Set up spinners with adapters
        setUpSpinners()

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView with adapter
        expensesAdapter = ExpensesAdapter(mutableListOf()) { expense ->
            deleteExpense(expense)
        }
        expensesRecyclerView.adapter = expensesAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up button click listeners
        setUpButtonListeners()

        // Show explanation dialog if first time
        showExplanationDialog()
    }

    private fun deleteExpense(expense: Expense) {
        expensesAdapter.removeExpense(expense)
    }

    private fun setUpSpinners() {
        val payFrequencyAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.pay_frequencies,
            android.R.layout.simple_spinner_item
        )
        payFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        payFrequencySpinner.adapter = payFrequencyAdapter

        val budgetPlanAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.budget_plans,
            android.R.layout.simple_spinner_item
        )
        budgetPlanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        budgetPlanSpinner.adapter = budgetPlanAdapter

        val stateTaxAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.state_taxes,
            android.R.layout.simple_spinner_item
        )
        stateTaxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stateTaxSpinner.adapter = stateTaxAdapter

        val expenseCategoryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        )
        expenseCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        expenseCategorySpinner.adapter = expenseCategoryAdapter
    }

    private fun setUpButtonListeners() {
        calculateButton.setOnClickListener {
            calculateBudget()
        }

        addExpenseButton.setOnClickListener {
            addExpense()
        }

        saveButton.setOnClickListener {
            saveData()
        }

        loadButton.setOnClickListener {
            loadData()
        }

        summaryButton.setOnClickListener {
            val intent = Intent(this, BudgetSummaryActivity::class.java).apply {
                putExtra("salary", salaryInput.text.toString().toDouble())
                putExtra("payFrequency", payFrequencySpinner.selectedItem.toString())
                putExtra("budgetPlan", budgetPlanSpinner.selectedItem.toString())
                putExtra("budgetResult", resultTextView.text.toString())
                putParcelableArrayListExtra("expenses", ArrayList(expensesAdapter.getExpenses()))
            }
            startActivity(intent)
        }

        explainButton.setOnClickListener {
            showExplanationDialog()
        }
    }

    @OptIn(UnstableApi::class)
    private fun calculateBudget() {
        val salaryStr = salaryInput.text.toString()
        if (salaryStr.isEmpty()) {
            Toast.makeText(this, R.string.enter_your_salary, Toast.LENGTH_SHORT).show()
            return
        }

        val salary = salaryStr.toDouble()
        val payFrequency = payFrequencySpinner.selectedItem.toString()
        val budgetPlan = budgetPlanSpinner.selectedItem.toString()
        val stateTaxStr = stateTaxSpinner.selectedItem.toString().split(" - ")[1].removeSuffix("%")
        val stateTax = stateTaxStr.toDouble() / 100

        val annualSalary = when (payFrequency) {
            "Monthly" -> salary * 12
            "Bi-Weekly" -> salary * 26
            "Weekly" -> salary * 52
            else -> 0.0
        }

        val monthlySalary = annualSalary / 12
        val salaryAfterTax = monthlySalary * (1 - stateTax)

        val (essentials, wants, savings) = when (budgetPlan) {
            "Savvy Saver (50/30/20)" -> Triple(50, 30, 20)
            "Big Spender (70/20/10)" -> Triple(70, 20, 10)
            "Balanced Baller (60/20/20)" -> Triple(60, 20, 20)
            "Frugal Fanatic (80/15/5)" -> Triple(80, 15, 5)
            "Cautious Calculator (70/10/20)" -> Triple(70, 10, 20)
            "Quirky Quota (33/33/34)" -> Triple(33, 33, 34)
            else -> Triple(0, 0, 0)
        }

        val essentialAmount = (essentials / 100.0) * salaryAfterTax
        val wantsAmount = (wants / 100.0) * salaryAfterTax
        val savingsAmount = (savings / 100.0) * salaryAfterTax

        // Retrieve expenses
        val expenses = expensesAdapter.getExpenses()
        Log.d("BudgetCalculator", "Expenses List: $expenses")

        // Calculate total expenses by category
        val totalEssentialExpenses = expenses.filter { it.category.equals("Essentials", ignoreCase = true) || it.category.equals("Essential", ignoreCase = true) }.sumOf { it.amount }
        val totalWantsExpenses = expenses.filter { it.category.equals("Wants", ignoreCase = true) || it.category.equals("Want", ignoreCase = true) }.sumOf { it.amount }
        val totalSavingsExpenses = expenses.filter { it.category.equals("Savings", ignoreCase = true) || it.category.equals("Saving", ignoreCase = true) }.sumOf { it.amount }

        Log.d("BudgetCalculator", "Total Essential Expenses: $totalEssentialExpenses")
        Log.d("BudgetCalculator", "Total Wants Expenses: $totalWantsExpenses")
        Log.d("BudgetCalculator", "Total Savings Expenses: $totalSavingsExpenses")

        // Calculate remaining amounts after expenses for each category
        val remainingEssential = essentialAmount - totalEssentialExpenses
        val remainingWants = wantsAmount - totalWantsExpenses
        val remainingSavings = savingsAmount - totalSavingsExpenses

        // Updated display text
        val summaryText = """
            📊 **Salary After Tax**: $${String.format("%.2f", salaryAfterTax)}
            
            🏠 **Budget Allocation**:
            • 🛒 **Essentials**: $${String.format("%.2f", essentialAmount)}
            • 🎉 **Wants**: $${String.format("%.2f", wantsAmount)}
            • 💰 **Savings**: $${String.format("%.2f", savingsAmount)}
        """.trimIndent()

        val remainingText = """
            📈 **Remaining after Expenses**:
            • 🛒 **Essentials**: $${String.format("%.2f", remainingEssential)} (Total Spent: $${String.format("%.2f", totalEssentialExpenses)})
            • 🎉 **Wants**: $${String.format("%.2f", remainingWants)} (Total Spent: $${String.format("%.2f", totalWantsExpenses)})
            • 💰 **Savings**: $${String.format("%.2f", remainingSavings)} (Total Spent: $${String.format("%.2f", totalSavingsExpenses)})
        """.trimIndent()

        resultTextView.text = "$summaryText\n\n$remainingText"
    }

    private fun addExpense() {
        val name = expenseNameInput.text.toString()
        val amountStr = expenseAmountInput.text.toString()
        val category = expenseCategorySpinner.selectedItem.toString()

        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter both name and amount of the expense", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        val date = Date()

        val expense = Expense(name, amount, category, date)
        expensesAdapter.addExpense(expense)

        // Clear input fields
        expenseNameInput.text.clear()
        expenseAmountInput.text.clear()
    }

    private fun saveData() {
        val salaryStr = salaryInput.text.toString()
        val payFrequency = payFrequencySpinner.selectedItem.toString()
        val budgetPlan = budgetPlanSpinner.selectedItem.toString()
        val budgetResult = resultTextView.text.toString()
        val expenses = expensesAdapter.getExpenses()

        if (salaryStr.isEmpty()) {
            Toast.makeText(this, "Please enter your salary before saving", Toast.LENGTH_SHORT).show()
            return
        }

        val salary = salaryStr.toDouble()

        val budget = Budget(salary, payFrequency, budgetPlan, budgetResult)
        val userExpenses = expenses.map { expense ->
            mapOf(
                "name" to expense.name,
                "amount" to expense.amount,
                "category" to expense.category,
                "date" to expense.date
            )
        }

        val userData = mapOf(
            "budget" to budget,
            "expenses" to userExpenses
        )

        db.collection("users")
            .document("user1")
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadData() {
        db.collection("users")
            .document("user1")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val budgetMap = document.get("budget") as? Map<*, *>
                    val salary = budgetMap?.get("salary") as? Double ?: 0.0
                    val payFrequency = budgetMap?.get("payFrequency") as? String ?: ""
                    val budgetPlan = budgetMap?.get("budgetPlan") as? String ?: ""
                    val budgetResult = budgetMap?.get("budgetResult") as? String ?: ""

                    salaryInput.setText(salary.toString())

                    val payFrequencyAdapter = payFrequencySpinner.adapter
                    if (payFrequencyAdapter is ArrayAdapter<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (payFrequencyAdapter as? ArrayAdapter<String>)?.let { adapter ->
                            val position = adapter.getPosition(payFrequency)
                            if (position >= 0) {
                                payFrequencySpinner.setSelection(position)
                            }
                        }
                    }

                    val budgetPlanAdapter = budgetPlanSpinner.adapter
                    if (budgetPlanAdapter is ArrayAdapter<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (budgetPlanAdapter as? ArrayAdapter<String>)?.let { adapter ->
                            val position = adapter.getPosition(budgetPlan)
                            if (position >= 0) {
                                budgetPlanSpinner.setSelection(position)
                            }
                        }
                    }

                    resultTextView.text = budgetResult

                    val expensesList = document.get("expenses")
                    if (expensesList is List<*>) {
                        val expenses = expensesList.mapNotNull { expenseMap ->
                            if (expenseMap is Map<*, *>) {
                                val name = expenseMap["name"] as? String ?: return@mapNotNull null
                                val amount = expenseMap["amount"] as? Double ?: return@mapNotNull null
                                val category = expenseMap["category"] as? String ?: return@mapNotNull null
                                val date = (expenseMap["date"] as? com.google.firebase.Timestamp)?.toDate() ?: return@mapNotNull null
                                Expense(name, amount, category, date)
                            } else {
                                null
                            }
                        }
                        expensesAdapter.setExpenses(expenses)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showExplanationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_explanation, null)

        val checkBox = dialogView.findViewById<CheckBox>(R.id.checkBox)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        confirmButton.setOnClickListener {
            if (checkBox.isChecked) {
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please confirm you have read and understood the instructions", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    data class Budget(
        val salary: Double,
        val payFrequency: String,
        val budgetPlan: String,
        val budgetResult: String
    )
}
