package com.hardcoreamature.tradingpersonalityapp

import android.text.Editable
import android.text.TextWatcher

open class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Do nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Do nothing
    }

    override fun afterTextChanged(s: Editable?) {
        // Do nothing
    }
}
