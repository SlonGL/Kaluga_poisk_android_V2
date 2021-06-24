package ru.kaluga_poisk.portalkalugapoisk

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yongchun.library.view.ImageSelectorActivity
import android.os.StrictMode
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

class CameraView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_view)

        // Что бы не быол ошибке при съемке фото.
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        ImageSelectorActivity.start(this, 1, ImageSelectorActivity.MODE_SINGLE, true, true, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            val images = data!!.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT) as ArrayList<String>
            // Обработка фото
            if (images.count() > 0) {
                if (photoSavingPlace == PHOTO_SAVING_PLACE_AUTH) {
                    userInfo.user_photo_file_name = images[0]
                    needToLoadPhoto = true
                }
                if (photoSavingPlace == PHOTO_SAVING_PLACE_WEB) {
                    savePhotoToWeb(images[0])
                }
                photoSavingPlace = PHOTO_SAVING_PLACE_UNDEFINED
            }
        }
        onBackPressed()
    }

    private fun savePhotoToWeb(fileName : String) {
        // ОБрезаю картинку до 300х300
        val bm = shrinkBitmap(fileName, 300, 300)
        val stream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteA = stream.toByteArray()
        val base64 = android.util.Base64.encodeToString(byteA, android.util.Base64.DEFAULT)
        // ВНИМАНИЮ ЗАКАЗЧИКА !!! URLEncoder !!! ИНаче не передает в функцию Java
        var url64 = URLEncoder.encode(base64,"UTF-8")

        webView_internal.post(Runnable {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                webView_internal.evaluateJavascript("javascript:window.photoFileReceiver('$url64')", { unit ->
                    Log.d("JAVA PHOTO: ", "RESULT: $unit")
                })
            } else {
                webView_internal.loadUrl("javascript:window.photoFileReceiver('$url64')")  // Было с API 17, но в новых API не работает
            }
        })
        photoSavingPlace = PHOTO_SAVING_PLACE_UNDEFINED
    }

    private fun shrinkBitmap(file: String, width: Int, height: Int): Bitmap {

        val bmpFactoryOptions = BitmapFactory.Options()
        bmpFactoryOptions.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions)

        val heightRatio = Math.ceil((bmpFactoryOptions.outHeight / height.toFloat()).toDouble()).toInt()
        val widthRatio = Math.ceil((bmpFactoryOptions.outWidth / width.toFloat()).toDouble()).toInt()

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions)
        return bitmap
    }


}
