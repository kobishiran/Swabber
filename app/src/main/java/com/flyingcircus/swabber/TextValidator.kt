package com.flyingcircus.swabber

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast

abstract class TextValidator(inputEditText: EditText, private val context: Context): TextWatcher {
    companion object {

    }
    private val editText = inputEditText

    abstract fun validate(editText: EditText, text: String)

    override fun afterTextChanged(s: Editable?) {
        val text = editText.text.toString()
        validate(editText, text)
    }

    fun isInRange(min: Int, max: Int, value: Int) : Boolean {
        val inRange = value in min..max
        if (!inRange) {
            Toast.makeText(context, "Value must be between $min and $max!", Toast.LENGTH_SHORT).show()
        }
        return inRange
    }

    fun isInRange(min: Float, max: Float, value: Float) : Boolean {
        val inRange = value >= min && value <= max
        if (!inRange) {
            Toast.makeText(context, "Value must be between $min and $max!", Toast.LENGTH_SHORT).show()
        }
        return inRange
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}