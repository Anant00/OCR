package com.app.ocr.repository

import android.app.Activity
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import com.app.ocr.utils.Details
import com.app.ocr.utils.RegexPatterns
import com.app.ocr.utils.showLog
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

object OCR {
    private var phoneNumber: String? = null
    private var email: String? = null
    private var blockText: String? = null

    fun pickImageOrUseCamera(context: Activity){
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(context)
    }

    @WorkerThread
    fun recognizeText(result: FirebaseVisionText?, image: Bitmap?): Details {
        if (result == null || image == null) {
            showLog("could not recognise image")
        } else {
            blockText = result.text
            for (block in result.textBlocks) {
                for (line in block.lines) {
                    val lineText = line.text
                    if (RegexPatterns.phonePattern.matcher(lineText).matches()) {
                        phoneNumber = lineText
                    } else if (RegexPatterns.usPhonePattern.matcher(lineText).matches()) {
                        phoneNumber = lineText
                    }
                    if (RegexPatterns.emailPattern.matcher(lineText).matches()) {
                        email = lineText
                    }
                    for (element in line.elements) {
                        if (RegexPatterns.phonePattern.matcher(lineText).matches()) {
                            phoneNumber = lineText
                        } else if (RegexPatterns.usPhonePattern.matcher(lineText).matches()) {
                            phoneNumber = lineText
                        }
                        if (RegexPatterns.emailPattern.matcher(lineText).matches()) {
                            email = lineText
                        }
                    }
                }
            }
        }
        showLog("detail  phone is  $phoneNumber")
        return Details(email, phoneNumber, blockText)
    }
}