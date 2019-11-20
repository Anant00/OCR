package com.app.ocr.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Any.showLog(msg: String?) {
    Log.d(this::class.java.simpleName, msg.let { msg!! })
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
