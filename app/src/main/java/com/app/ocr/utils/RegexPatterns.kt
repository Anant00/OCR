package com.app.ocr.utils

import java.util.regex.Pattern

object RegexPatterns {
    val phonePattern: Pattern by lazy {
        Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[- (]*(\\d{3})[- )]*(\\d{3})[- ]*(\\d{4})(?: *[x/#](\\d+))?\\s*\$")
    }
    val usPhonePattern: Pattern by lazy {
        Pattern.compile("^[+]*[(]?[0-9]{1,3}[)]?[-\\s./0-9]*\$")
    }
    val emailPattern: Pattern by lazy {
        Pattern.compile("[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
    }
}