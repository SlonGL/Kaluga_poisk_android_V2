package ru.kaluga_poisk.portalkalugapoisk

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.content_auth__new.*
import com.auth0.android.result.UserProfile


var needToLoadPhoto = false
var auth0 : Auth0? = null
var auth0token : String = ""

class Auth_New : AppCompatActivity() {

    var userPhotoImageButton : ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth__new)

        // auth0 = Auth0(this)
        auth0 = Auth0("poDaTZFxLxoZZae4ZI7J4unbvXwekMsq", "kaluga.auth0.com")
        auth0?.setOIDCConformant(false)

        userPhotoImageButton = findViewById(R.id.auth_photoImageButton) as ImageButton

        setFields()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (userInfo.user_photo_file_name != "" && needToLoadPhoto) {
            userPhotoImageButton?.setImageBitmap(BitmapFactory.decodeFile(userInfo.user_photo_file_name))
        }
    }

    private fun setFields() {

        val mPrefs = getPreferences(MODE_PRIVATE)

        userInfo.user_nickname = mPrefs!!.getString("user_nickname", "") ?: ""
        userInfo.user_name = mPrefs!!.getString("user_name", "") ?: ""
        userInfo.user_familyname = mPrefs!!.getString("user_familyname", "") ?: ""
        userInfo.user_birthday = mPrefs!!.getString("user_birthday", "") ?: ""
        userInfo.user_phone = mPrefs!!.getString("user_phone", "") ?: ""
        userInfo.user_photo_file_name = mPrefs!!.getString("user_photo_file_name", "") ?: ""
        if (userInfo.user_photo_file_name > "") {
            needToLoadPhoto = true
        }

        if (userInfo.user_photo_file_name != "" && needToLoadPhoto) {
            userPhotoImageButton?.setImageBitmap(BitmapFactory.decodeFile(userInfo.user_photo_file_name))
        }
        auth_ti_nickname.setText(userInfo.user_nickname)
        auth_ti_name.setText(userInfo.user_name)
        auth_ti_familyname.setText(userInfo.user_familyname)
        auth_ti_birthday.setText(userInfo.user_birthday)
    }

    // Обработка нажатия на фото

    public fun photoButtonPressed(view : View) {
        photoSavingPlace = PHOTO_SAVING_PLACE_AUTH
        val intent = Intent(this@Auth_New, CameraView::class.java)
        startActivity(intent)
    }

    public fun backButtonPressed(view : View) {
        onBackPressed()
    }

    public fun saveButtonPressed(view : View) {
        userInfo.user_nickname = (findViewById(R.id.auth_ti_nickname) as TextInputEditText).text.toString()
        userInfo.user_name = (findViewById(R.id.auth_ti_name) as TextInputEditText).text.toString()
        userInfo.user_familyname = (findViewById(R.id.auth_ti_familyname) as TextInputEditText).text.toString()
        userInfo.user_birthday = (findViewById(R.id.auth_ti_birthday) as TextInputEditText).text.toString()
        userInfo.user_phone = (findViewById(R.id.auth_ti_phone) as TextInputEditText).text.toString()

        val mPrefs = getPreferences(Context.MODE_PRIVATE)
        val prefsEditor = mPrefs!!.edit()

        prefsEditor.putString("user_nickname", userInfo.user_nickname)
        prefsEditor.putString("user_name", userInfo.user_name)
        prefsEditor.putString("user_familyname", userInfo.user_familyname)
        prefsEditor.putString("user_birthday", userInfo.user_birthday)
        prefsEditor.putString("user_phone", userInfo.user_phone)
        prefsEditor.putString("user_photo_file_name", userInfo.user_photo_file_name)

        prefsEditor.commit()
        onBackPressed()
    }

    fun confidenceButtonPressed(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.kaluga-poisk.ru/privacy-policy"))
        startActivity(browserIntent)
    }

    fun userAgreementButtonPressed(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.kaluga-poisk.ru/terms-of-use"))
        startActivity(browserIntent)
    }

    // Конец нажатия на фото

    public fun vkButtonPressed(view: View) {
        login("vkontakte")
    }

    public fun fbButtonPressed(view: View) {
        login("facebook")
    }

    public fun googleButtonPressed(view: View) {
        login("google-oauth2")
    }

    public fun yandexButtonPressed(view: View) {
        login("yandex")
    }

    public fun instagramButtonPressed(view: View) {
        login("instagram")
    }

    private fun login(social: String) {
        WebAuthProvider.init(auth0!!)
            .withScheme("demo")
            .withAudience(String.format("https://%s/userinfo", "kaluga.auth0.com"))
            .withConnection(social)
            .withScope("openid email profile")
            .start(this@Auth_New, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    // Show error Dialog to user
                    runOnUiThread { dialog.show() }

                }

                override fun onFailure(exception: AuthenticationException) {
                    // Show error to user
                    runOnUiThread { Toast.makeText(window.context, "Ошибка аутентификации: " + exception.description, Toast.LENGTH_SHORT).show() }
                }

                override fun onSuccess(credentials: Credentials) {
                    // Store credentials
                    // Navigate to your main activity
                    auth0token = credentials.accessToken!!
                    val authentication = AuthenticationAPIClient(auth0!!)
                    authentication
                        .userInfo(auth0token)
                        .start(object : BaseCallback<UserProfile, AuthenticationException> {

                            override fun onFailure(error: AuthenticationException) {
                                //user information request failed
                                runOnUiThread { Toast.makeText(window.context, "Ошибка аутентификации: " + error.description, Toast.LENGTH_SHORT).show() }
                            }

                            override fun onSuccess(payload: UserProfile?) {
                                //user information received
                                runOnUiThread {
                                    payload?.let {
                                        auth_ti_nickname.setText(it.nickname)
                                        auth_ti_name.setText(it.givenName)
                                        auth_ti_familyname.setText(it.familyName)
                                        val bm = BitmapFactory.decodeFile(it.pictureURL)
                                        if (bm != null) {
                                            userPhotoImageButton?.setImageBitmap(bm)
                                        }
                                    }
                                }
                            }
                        })
                }
            })
    }



}
