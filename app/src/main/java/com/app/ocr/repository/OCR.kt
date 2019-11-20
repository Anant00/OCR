package com.app.ocr.repository

import android.app.Activity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

object OCR {

    fun pickImageOrUseCamera(context: Activity){
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(context)
    }
}