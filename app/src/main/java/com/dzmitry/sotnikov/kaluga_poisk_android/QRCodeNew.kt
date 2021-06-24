package ru.kaluga_poisk.portalkalugapoisk

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import pub.devrel.easypermissions.EasyPermissions

class QRCodeNew : AppCompatActivity() , ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            EasyPermissions.requestPermissions(
                this,
                "Камера используется если вы захотите зарегистрировать свое фото или отправить в портал фото-отзыв",
                19711,
                Manifest.permission.CAMERA)
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WAKE_LOCK)) {
            EasyPermissions.requestPermissions(
                this,
                "Необходимо для работы сканера QR кодов",
                19716,
                Manifest.permission.WAKE_LOCK)
        }

        mScannerView = ZXingScannerView(this)   // Programmatically initialize the scanner view
        setContentView(mScannerView)                // Set the scanner view as the content view
    }

    // Для запроса разрешений в версиях старше Oreo
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        // Log.v("tag", rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        MainActivity.tvresult =rawResult.text
        onBackPressed()

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}
