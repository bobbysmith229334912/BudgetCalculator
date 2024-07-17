package com.hardcoreamature.budgetcalculatoreno

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.plaid.link.Plaid
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkResultHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PREF_FIRST_LAUNCH = "first_launch"
        private const val LINK_REQUEST_CODE = 100
    }

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
    private lateinit var settingsButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var roundUpCheckBox: CheckBox
    private lateinit var cryptoSpinner: Spinner
    private lateinit var currentAccumulationTextView: TextView
    private lateinit var minimumOrderSizeTextView: TextView
    private lateinit var krakenApiHelper: KrakenApiHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingFrame: FrameLayout
    private lateinit var startPlaidLinkButton: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Check if it's the first launch
        val isFirstLaunch = sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            showExplanationDialog()
            sharedPreferences.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply()
        }

        // Initialize views
        initializeViews()

        // Check if API keys are stored
        val apiKey = sharedPreferences.getString("kraken_api_key", null)
        val apiSecret = sharedPreferences.getString("kraken_api_secret", null)
        val selectedCrypto = sharedPreferences.getString("selected_crypto", "BTC")

        if (apiKey == null || apiSecret == null) {
            // Prompt user for API keys
            showKrakenSignupDialog()
        } else {
            // Initialize KrakenApiHelper with stored API keys
            krakenApiHelper = KrakenApiHelper(this, apiKey, apiSecret)
            updateCryptoInfo()
        }

        val viewBalanceButton: Button = findViewById(R.id.viewBalanceButton)
        viewBalanceButton.setOnClickListener {
            showBalanceDialog()
        }

        // Set up spinners with adapters
        setUpSpinners()

        // Set saved crypto pair
        setSavedCryptoPair(selectedCrypto)

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

        // Initialize Retrofit and MinimumAmountUpdater
        initializeRetrofit()
    }

    private fun showBalanceDialog() {
        if (!::krakenApiHelper.isInitialized) {
            Toast.makeText(this, "Kraken API is not set up", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        krakenApiHelper.getBalances { balances, error ->
            showLoading(false)
            if (error != null) {
                Toast.makeText(this, "Failed to fetch balance: $error", Toast.LENGTH_SHORT).show()
            } else if (balances != null) {
                val balanceText = balances.entries.joinToString(separator = "\n") { "${it.key}: ${it.value}" }
                showDialog("Your Balances", balanceText)
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_balance, null)
        val balanceTextView = dialogView.findViewById<TextView>(R.id.balanceTextView)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        balanceTextView.text = message

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateCryptoInfo() {
        if (!::krakenApiHelper.isInitialized) {
            Toast.makeText(this, "Kraken API is not set up", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure the selected item is not null
        val selectedCrypto = cryptoSpinner.selectedItem?.toString()
        if (selectedCrypto == null) {
            Toast.makeText(this, "No cryptocurrency selected", Toast.LENGTH_SHORT).show()
            return
        }

        val cryptoPair = when (selectedCrypto) {
            "BTC" -> "XXBTZUSD"
            "ETH" -> "XETHZUSD"
            "LTC" -> "XLTCZUSD"
            else -> {
                Toast.makeText(this, "Unsupported cryptocurrency selected", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Fetch the minimum order size from Firestore
        val minOrderSizeRetriever = MinimumOrderSizeRetriever(db)
        minOrderSizeRetriever.getMinimumOrderSize(cryptoPair) { minimumAmount ->
            if (minimumAmount != null) {
                krakenApiHelper.getCurrentPrice(cryptoPair) { price ->
                    val approxCost = price?.times(minimumAmount) ?: 0.0
                    minimumOrderSizeTextView.text = getString(R.string.minimum_order_size_and_cost, minimumAmount, approxCost)
                }
            } else {
                minimumOrderSizeTextView.text = getString(R.string.minimum_order_size_na)
            }
        }

        // Fetch the current accumulation from Firestore
        val docRef = db.collection("cryptoAccumulations").document(cryptoPair)
        docRef.get().addOnSuccessListener { document ->
            val currentAmount = document.getDouble("amount") ?: 0.0
            currentAccumulationTextView.text = getString(R.string.current_accumulation_text, currentAmount)
        }.addOnFailureListener {
            currentAccumulationTextView.text = getString(R.string.current_accumulation_na)
        }
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

        val cryptoAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.cryptocurrencies,
            android.R.layout.simple_spinner_item
        )
        cryptoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cryptoSpinner.adapter = cryptoAdapter

        roundUpCheckBox.setOnCheckedChangeListener { _, isChecked ->
            cryptoSpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setSavedCryptoPair(selectedCrypto: String?) {
        val cryptoAdapter = cryptoSpinner.adapter as ArrayAdapter<String>
        val position = cryptoAdapter.getPosition(selectedCrypto)
        if (position >= 0) {
            cryptoSpinner.setSelection(position)
        }
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

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        startPlaidLinkButton.setOnClickListener {
            Log.d("MainActivity", "Link Bank Account button clicked")
            fetchLinkToken()
        }
    }

    private fun fetchLinkToken() {
        val userId = "your_dynamic_user_id" // Replace this with the actual user ID

        val jsonBody = JSONObject().apply {
            put("user_id", userId)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api-vmzc33chzq-uc.a.run.app/create_link_token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error fetching link token", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Error fetching link token", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("MainActivity", "Response body: $responseBody")

                if (responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val linkToken = jsonResponse.getString("link_token")
                        runOnUiThread {
                            openPlaidLink(linkToken)
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Invalid response format", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "Invalid response format", e)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                        Log.e("MainActivity", "Empty response body")
                    }
                }
            }
        })
    }



    private fun openPlaidLink(linkToken: String) {
        val configuration = LinkTokenConfiguration.Builder()
            .token(linkToken)
            .build()

        // Correct use of the application context
        Plaid.create(application, configuration).open(this)
    }

    private val linkResultHandler = LinkResultHandler(
        onSuccess = { linkSuccess ->
            // Use the public token to exchange for an access token
            exchangePublicToken(linkSuccess.publicToken)
        },
        onExit = { linkExit ->
            // Handle the error, user exit, or institution not linked
            Toast.makeText(this, "Plaid link exited", Toast.LENGTH_SHORT).show()
        }
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LINK_REQUEST_CODE) {
            linkResultHandler.onActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun exchangePublicToken(publicToken: String) {
        val requestBody = FormBody.Builder()
            .add("public_token", publicToken)
            .build()

        val request = Request.Builder()
            .url("https://api-vmzc33chzq-uc.a.run.app/exchange_public_token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error exchanging public token", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val accessToken = jsonResponse.getString("access_token")
                    val itemId = jsonResponse.getString("item_id")

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Access token received", Toast.LENGTH_SHORT).show()
                    }

                    // Store accessToken and itemId securely for future use
                    storeAccessTokenAndItemId(accessToken, itemId)
                }
            }
        })
    }

    private fun storeAccessTokenAndItemId(accessToken: String, itemId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("plaid_access_token", accessToken)
        editor.putString("plaid_item_id", itemId)
        editor.apply()
    }

    private fun calculateBudget() {
        val salaryStr = salaryInput.text.toString()
        if (salaryStr.isEmpty()) {
            Toast.makeText(this, "Please enter your salary", Toast.LENGTH_SHORT).show()
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

        // Calculate total expenses by category
        val totalEssentialExpenses = expenses.filter { it.category.equals("Essentials", ignoreCase = true) || it.category.equals("Essential", ignoreCase = true) }.sumOf { it.amount }
        val totalWantsExpenses = expenses.filter { it.category.equals("Wants", ignoreCase = true) || it.category.equals("Want", ignoreCase = true) }.sumOf { it.amount }
        val totalSavingsExpenses = expenses.filter { it.category.equals("Savings", ignoreCase = true) || it.category.equals("Saving", ignoreCase = true) }.sumOf { it.amount }

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

        resultTextView.text = String.format("%s\n\n%s", summaryText, remainingText)
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

        // Handle round-up and purchase crypto if enabled
        if (roundUpCheckBox.isChecked) {
            val roundUpAmount = calculateRoundUp(amount)

            // Convert to Kraken's trading pair format if needed
            val cryptoPair = when (cryptoSpinner.selectedItem.toString()) {
                "BTC" -> "XXBTZUSD"
                "ETH" -> "XETHZUSD"
                "LTC" -> "XLTCZUSD"
                else -> throw IllegalArgumentException("Unsupported cryptocurrency")
            }

            // Save selected cryptocurrency pair
            sharedPreferences.edit()
                .putString("selected_crypto", cryptoSpinner.selectedItem.toString())
                .apply()

            // Fetch the minimum order size from Firestore
            val minOrderSizeRetriever = MinimumOrderSizeRetriever(db)
            minOrderSizeRetriever.getMinimumOrderSize(cryptoPair) { minimumAmount ->
                if (minimumAmount == null) {
                    Toast.makeText(this, "Failed to fetch minimum order size", Toast.LENGTH_SHORT).show()
                    return@getMinimumOrderSize
                }

                val docRef = db.collection("cryptoAccumulations").document(cryptoPair)
                docRef.get().addOnSuccessListener { document ->
                    val currentAmount = document.getDouble("amount") ?: 0.0
                    val newAmount = currentAmount + roundUpAmount

                    if (newAmount >= minimumAmount) {
                        // Prepare transaction with confirmation dialog
                        prepareTransaction(cryptoPair, newAmount)
                    } else {
                        updateCryptoAccumulation(cryptoPair, roundUpAmount)
                        Toast.makeText(
                            this,
                            "Insufficient funds for crypto purchase. Minimum required: $minimumAmount. Current accumulation: $newAmount. The purchase will be automatically done once the minimum amount is reached.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun prepareTransaction(cryptoPair: String, amount: Double) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_confirmation, null)
        val transactionDetailsTextView = dialogView.findViewById<TextView>(R.id.transactionDetailsTextView)
        val confirmTransactionButton = dialogView.findViewById<Button>(R.id.confirmTransactionButton)
        val cancelTransactionButton = dialogView.findViewById<Button>(R.id.cancelTransactionButton)

        val approxCost = amount * (sharedPreferences.getFloat("current_price_$cryptoPair", 0.0f).toDouble())
        transactionDetailsTextView.text = getString(R.string.transaction_details, amount, cryptoPair, approxCost)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        confirmTransactionButton.setOnClickListener {
            performTransaction(cryptoPair, amount)
            dialog.dismiss()
        }

        cancelTransactionButton.setOnClickListener {
            Toast.makeText(this, "Transaction canceled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performTransaction(cryptoPair: String, amount: Double) {
        krakenApiHelper.buyCrypto(cryptoPair, amount.toString()) { success, message ->
            if (success) {
                Log.d("MainActivity", "Crypto purchase successful: $message")
                updateCryptoAccumulation(cryptoPair, -amount)  // Reset the accumulation
            } else {
                Log.e("MainActivity", "Crypto purchase failed: $message")
                updateCryptoAccumulation(cryptoPair, amount)
            }
        }
    }

    private fun calculateRoundUp(amount: Double): Double {
        return ceil(amount) - amount
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
        showLoading(true)
        db.collection("users")
            .document("user1")
            .get()
            .addOnSuccessListener { document ->
                hideLoading()
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
                hideLoading()
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCryptoAccumulation(pair: String, amount: Double) {
        val docRef = db.collection("cryptoAccumulations").document(pair)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentAmount = snapshot.getDouble("amount") ?: 0.0
            val newAmount = currentAmount + amount
            transaction.set(docRef, mapOf("amount" to newAmount))
        }.addOnSuccessListener {
            Log.d("Firestore", "Category amount updated successfully")
            updateCryptoInfo() // Update the current accumulation after updating the crypto accumulation
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Category amount update failed", e)
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

    private fun showApiKeyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_api_key, null)

        val apiKeyInput = dialogView.findViewById<EditText>(R.id.apiKeyInput)
        val apiSecretInput = dialogView.findViewById<EditText>(R.id.apiSecretInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        confirmButton.setOnClickListener {
            val apiKey = apiKeyInput.text.toString()
            val apiSecret = apiSecretInput.text.toString()

            if (apiKey.isNotEmpty() && apiSecret.isNotEmpty()) {
                sharedPreferences.edit()
                    .putString("kraken_api_key", apiKey)
                    .putString("kraken_api_secret", apiSecret)
                    .apply()

                krakenApiHelper = KrakenApiHelper(this, apiKey, apiSecret)
                updateCryptoInfo()

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter both API key and secret", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showKrakenSignupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_kraken_signup, null)

        val buttonYes = dialogView.findViewById<Button>(R.id.button_yes)
        val buttonNo = dialogView.findViewById<Button>(R.id.button_no)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonYes.setOnClickListener {
            showApiKeyDialog()
            dialog.dismiss()
        }

        buttonNo.setOnClickListener {
            // Grey out the Kraken-related features
            roundUpCheckBox.isEnabled = false
            cryptoSpinner.isEnabled = false
            currentAccumulationTextView.setTextColor(getColor(R.color.greyed_out))
            minimumOrderSizeTextView.setTextColor(getColor(R.color.greyed_out))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        loadingFrame.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.content).visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun hideLoading() {
        loadingFrame.visibility = View.GONE
        findViewById<View>(R.id.content).visibility = View.VISIBLE
    }

    private fun initializeViews() {
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
        settingsButton = findViewById(R.id.settingsButton)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        roundUpCheckBox = findViewById(R.id.roundUpCheckBox)
        cryptoSpinner = findViewById(R.id.cryptoSpinner)
        currentAccumulationTextView = findViewById(R.id.currentAccumulationTextView)
        minimumOrderSizeTextView = findViewById(R.id.minimumOrderSizeTextView)
        loadingFrame = findViewById(R.id.loadingFrame)
        startPlaidLinkButton = findViewById(R.id.startPlaidLinkButton)
    }

    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kraken.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val krakenApiService = retrofit.create(KrakenApiService::class.java)
        val minimumAmountUpdater = MinimumAmountUpdater(krakenApiService, db)

        // Update all minimum order sizes
        minimumAmountUpdater.updateAllMinimumOrderSizes()

        // Schedule periodic updates for minimum order sizes
        val workRequest = PeriodicWorkRequestBuilder<MinimumOrderSizeUpdateWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    data class Budget(
        val salary: Double,
        val payFrequency: String,
        val budgetPlan: String,
        val budgetResult: String
    )
}
