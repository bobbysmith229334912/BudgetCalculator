package com.hardcoreamature.budgetcalculatoreno

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpensesAdapter(
    private val expenses: MutableList<Expense>,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName: TextView = itemView.findViewById(R.id.expenseName)
        val expenseAmount: TextView = itemView.findViewById(R.id.expenseAmount)
        val expenseCategory: TextView = itemView.findViewById(R.id.expenseCategory)
        val expenseDate: TextView = itemView.findViewById(R.id.expenseDate)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentExpense = expenses[position]
        holder.expenseName.text = currentExpense.name
        holder.expenseAmount.text = String.format("%.2f", currentExpense.amount)
        holder.expenseCategory.text = currentExpense.category
        holder.expenseDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentExpense.date)
        holder.deleteButton.setOnClickListener {
            onDeleteClick(currentExpense)
        }
    }

    override fun getItemCount() = expenses.size

    fun addExpense(expense: Expense) {
        expenses.add(expense)
        notifyItemInserted(expenses.size - 1)
    }

    fun removeExpense(expense: Expense) {
        val position = expenses.indexOf(expense)
        if (position >= 0) {
            expenses.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setExpenses(expensesList: List<Expense>) {
        expenses.clear()
        expenses.addAll(expensesList)
        notifyDataSetChanged()
    }

    fun getExpenses(): List<Expense> {
        return expenses
    }
}
