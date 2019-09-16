package com.example.voicesimpletodo

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


fun View.hideSoftKeyBoard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
fun View.showSoftKeyBoard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    imm.hideSoftInputFromWindow(windowToken, InputMethodManager.SHOW_IMPLICIT)
}