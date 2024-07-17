package com.hardcoreamature.budgetcalculatoreno

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Expense(
    val name: String,
    val amount: Double,
    val category: String,
    val date: Date
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(amount)
        parcel.writeString(category)
        parcel.writeLong(date.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}
