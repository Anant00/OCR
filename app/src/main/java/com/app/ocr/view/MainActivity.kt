package com.app.ocr.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.app.ocr.R
import com.app.ocr.repository.OCR
import com.app.ocr.utils.Details
import com.app.ocr.utils.RegexPatterns.emailPattern
import com.app.ocr.utils.RegexPatterns.phonePattern
import com.app.ocr.utils.RegexPatterns.usPhonePattern
import com.app.ocr.utils.showLog
import com.app.ocr.utils.showToast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    companion object{
        private val tag by lazy{
            MainActivity::class.java.simpleName
        }
        private var phoneNumber: String? = null
        private var email: String? = null
        private var blockText: String? = null
        private var bitmap: Bitmap? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            OCR.pickImageOrUseCamera(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri = result.uri
                    animation_view.visibility = GONE
                    imageView.visibility = VISIBLE
                    imageView.setImageURI(imageUri)
                    bitmap = getBitmap(contentResolver, imageUri)
                    analyzeImage(bitmap)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    showToast("There was some error : ${result.error.message}")
                }
            }
        }
    }


    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            showToast("Try again, could not get Image")
            return
        }
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
                recognizeText(it, mutableImage)
                CoroutineScope(IO).launch {
                    val result = async { recognizeText(it, mutableImage) }
                    val details = result.await()
                    withContext(Main){
                        setData(details.email, details.phone, details.blockText)
                    }
                }
            }
            .addOnFailureListener {
                showLog(it.localizedMessage)
            }
    }

    @WorkerThread
    private fun recognizeText(result: FirebaseVisionText?, image: Bitmap?): Details {
        if (result == null || image == null) {
          showLog("could not recognise image")
        }
        else {
            showLog("result Text: ${result.text} ")
            blockText = result.text
            for (block in result.textBlocks) {
                Log.d(tag, "block text is :  $blockText")
                for (line in block.lines) {
                    val lineText = line.text
                    if (phonePattern.matcher(lineText).matches()){
                        phoneNumber = lineText
                    }else if (usPhonePattern.matcher(lineText).matches()){
                        phoneNumber = lineText
                    }
                    if (emailPattern.matcher(lineText).matches()){
                        email= lineText
                    }
                    for (element in line.elements) {
                        if (phonePattern.matcher(lineText).matches()){
                            phoneNumber = lineText
                            Log.d(tag, "phone is : $lineText ")
                        }
                        else if (usPhonePattern.matcher(lineText).matches()){
                            phoneNumber = lineText
                        }
                        if (emailPattern.matcher(lineText).matches()){
                            email= lineText
                        }
                    }
                }
            }
        }
        showLog("phone Number: $phoneNumber, email : $email, block: $blockText ")
        return Details(email, phoneNumber, blockText)
    }

    @SuppressLint("SetTextI18n")
    private fun setData(email: String?, phone: String?, result: String?){
        if (phoneNumber == null){
            tvPhone.text = "Sorry, could not get the phone number, make sure image is clear and try again"
        }else{
            tvPhone.text = "Phone:\n $phone"
        }
        if (email == null){
            tvEmail.text = "Sorry, could not get the email, make sure image is clear and try again"
        }else{
            tvEmail.text = "Email:\n $email"
        }
        if (result == null){
            tvOtherDetail.text = "Sorry, could not get the details, make sure image is clear and try again"
        }else{
            tvOtherDetail.text = "Other Details:\n\n $result"
        }
        imageView.visibility = VISIBLE
        tvEmpty.visibility = GONE
        linData.visibility = VISIBLE
        animation_view.visibility = GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", email)
        outState.putString("phone", phoneNumber)
        outState.putString("other", blockText)
        outState.putParcelable("image",bitmap)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        email = savedInstanceState.getString("email")
        phoneNumber = savedInstanceState.getString("phone")
        blockText = savedInstanceState.getString("other")
        bitmap = savedInstanceState.getParcelable("image")
        imageView.setImageBitmap(bitmap)
        if (email!=null && phoneNumber!= null && blockText != null){
            setData(email, phoneNumber, blockText)
        }
    }
}