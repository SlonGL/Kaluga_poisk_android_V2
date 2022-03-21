package ru.kaluga_poisk.portalkalugapoisk

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.*
import java.net.URLEncoder
import java.util.ArrayList
import java.util.HashMap
import android.widget.EditText
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.view.inputmethod.InputMethodManager
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.webkit.WebSettings
import com.dzmitry.sotnikov.kaluga_poisk_android.Auth_New
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.installations.FirebaseInstallations

internal const val siteURL = "https://www.kaluga-poisk.ru/mobile/android/show"

internal lateinit var expandableListAdapter: ExpandableListAdapter
internal lateinit var expandableListView: ExpandableListView
internal var headerList: MutableList<MenuModel> = ArrayList<MenuModel>()
internal var childList = HashMap<MenuModel, List<MenuModel>>()
internal var menuIsLoaded = false

internal lateinit var webView_internal : WebView
internal lateinit var webViewContext : Context

var userInfo = UserInfo()
var token : String? = null

val PHOTO_SAVING_PLACE_UNDEFINED = "undefined"
val PHOTO_SAVING_PLACE_AUTH = "auth"
val PHOTO_SAVING_PLACE_WEB = "web"
var photoSavingPlace = PHOTO_SAVING_PLACE_UNDEFINED // auth - из идентификации, web - из кнопки JavaScript d webView_internal

class UserInfo {
    var user_nickname: String = ""
    var user_name: String = ""
    var user_familyname: String = ""
    var user_birthday: String = ""
    var user_phone: String = ""
    var user_photo_file_name: String = ""
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {

        var tvresult: String? = null
    }

    lateinit var mySearchTextFieldLayout : TextInputLayout
    lateinit var mySearchTextField : TextInputEditText
    lateinit var mySearchCloseButton : ImageButton
    lateinit var mySearchMakeButton : ImageButton
    lateinit var mySearchGrayShadow : ImageView
    lateinit var mySearchWhiteBar : ImageView

    var permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE
    )

    // Начало onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        webView_internal = findViewById<WebView>(R.id.webView)
        webViewContext = baseContext

        mySearchTextFieldLayout = findViewById(R.id.searchTextFieldLayout) as TextInputLayout
        mySearchTextField = findViewById(R.id.searchTextField) as TextInputEditText
        mySearchCloseButton = findViewById(R.id.searchCloseButton) as ImageButton
        mySearchMakeButton = findViewById(R.id.searchMakeButton) as ImageButton
        mySearchGrayShadow = findViewById(R.id.searchGrayShadow) as ImageView
        mySearchWhiteBar = findViewById(R.id.mySearchWhiteBar) as ImageView

        mySearchCloseButton.setOnClickListener(View.OnClickListener {view ->
            hideSearchBar()
        })

        mySearchMakeButton.setOnClickListener(View.OnClickListener {
            // Поиск
            makeSearch()
        })

        mySearchTextField.setOnEditorActionListener() { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                // Поиск
                makeSearch()
                true
            } else {
                false
            }
        }

        hideSearchBar()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val searchButton = findViewById(R.id.searchButton) as FloatingActionButton
        searchButton.setOnClickListener(View.OnClickListener { view ->
            if (mySearchGrayShadow.visibility == View.INVISIBLE) {
                showSearchBar()
            } else {
                hideSearchBar()
            }
        })


        val backButton = findViewById(R.id.backButton) as FloatingActionButton
        backButton.setOnClickListener(View.OnClickListener { view ->
            hideSearchBar()
            if (findViewById<WebView>(R.id.webView).canGoBack()) {
                findViewById<WebView>(R.id.webView).goBack()
            }
        })

        // Загружаем левое меню
        expandableListView = findViewById(R.id.main_menu_expandableListView)
        prepareMenuData()
        while (!menuIsLoaded) {

        }
        populateExpandableList()

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener {
            token = it.result!!.token
            // DO your thing with your firebase token
        }

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        val webViewClient = MyWebViewClient()
        findViewById<WebView>(R.id.webView).setWebViewClient(webViewClient)

        findViewById<WebView>(R.id.webView).settings.javaScriptEnabled = true

        val webSettings = findViewById<WebView>(R.id.webView).getSettings()
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")
        findViewById<WebView>(R.id.webView).getSettings().setPluginState(WebSettings.PluginState.ON)


        val jsInterface = JavaScriptInterface(this)
        findViewById<WebView>(R.id.webView).addJavascriptInterface(jsInterface, "JSInterface")

        findViewById<WebView>(R.id.webView).loadUrl(siteURL + "?gp=1&device=android&version=2")

        // webView.loadUrl("file:///android_asset/JavaScriptTest.html")
        // webView.loadUrl("http://obninsk-poisk.ru/testjs")
        // webView.loadUrl("https://is-slon.wixsite.com/mysite")
    }

    // Окончание onCreate

    // Для запроса разрешений в версиях старше M

    private fun checkPermissions() : Boolean {
        var result: Int
        var listPermissionsNeeded = emptyArray<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded = listPermissionsNeeded.plus(p)
            }
        }
        // listPermissionsNeeded = permissions
        if (!listPermissionsNeeded.isEmpty()) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Внимание")
            builder.setMessage("Сейчас приложение запросит у вас разрешения на доступ к камере и микрофону для размещения фото и аудио сообщений в отзывах. Также приложению нужен доступ к хранилищу для их временного хранения.")
            builder.setPositiveButton("Понятно"){dialog, which ->
                ActivityCompat.requestPermissions(this, listPermissionsNeeded, 100)
            }
            builder.create().show()
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return
        }
    }

    /*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        // EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    */

    // Убирает софт-кейс снизу
    private fun enableImmersiveMode() {
        // Hide soft keys
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        this.supportActionBar?.hide()
    }

    // Обработка возврата в основную активность из других
    // Запуск поиска если строка из QR сканера не пустая

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // enableImmersiveMode()
            if (tvresult != null && tvresult.toString() > "") {
                mySearchTextField.setText(tvresult.toString())
                tvresult = null
                makeSearch()
            }
        }
    }

    // Конец обработки возврата из других активностей


    //--------------------
    // Блок обработки нажатий на кнопки
    //--------------------


    // Начало обработки нажания на вызов левого меню

    override fun onBackPressed() {
        hideSearchBar()
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (findViewById<WebView>(R.id.webView).canGoBack()) {
                findViewById<WebView>(R.id.webView).goBack()
            }
        }
    }


    // Конец обработки нажания на вызов левого меню

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        hideSearchBar()
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // НЕ ИСПОЛЬЗУЕТСЯ
    // Обработка нажатия Settings в верхнем меню

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        hideSearchBar()

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    // Конец обработки нажатия Settings


    // Начало - выбор фиксированных (не созданных) пунктов бокового меню

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        hideSearchBar()

        // if (id == R.id.nav_camera) {
            // Handle the camera action
        // }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    // Конец выбора фиксированных пунктов в боковом меню

    // Обработка нажатия на QRCode

    public fun qrCodeButtonPressed(view : View) {
        onBackPressed()
        val intent = Intent(this, QRCodeNew::class.java)
        startActivity(intent)
    }

    // Конец нажатия на QRCode

    // Обработка нажатия на Auth

    public fun authButtonPressed(view : View) {
        onBackPressed()
        val intent = Intent(this, Auth_New::class.java)
        startActivity(intent)
    }

    // Конец нажатия на Auth

    // Начало нажатия на Поделиться

    public fun shareButtonPressed(view : View) {
        onBackPressed()
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        val shareBody = "В этом приложении можно найти все что захочешь про Калугу!\nhttps://play.google.com/store/apps/details?id=ru.kaluga_poisk.portalkalugapoisk&hl=ru"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Хочу поделиться с тобой приложением Калуга-поиск.")
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Поделиться через"))
    }

    // Конец нажатия на Поделиться


    //--------------------
    // Конец блока обработки нажатий на кнопки
    //--------------------

    // Начало создания бокового меню

    private fun prepareMenuData() {
        LoadMenu().execute()
    }

    private fun populateExpandableList() {

        expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        expandableListView.setAdapter(expandableListAdapter)

        expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
            if (headerList[groupPosition].isGroup) {
                if (!headerList[groupPosition].hasChildren) {
                    // val webView = findViewById(R.id.webView) as WebView
                    // val jsInterface = JavaScriptInterface(this)
                    // webView.settings.javaScriptEnabled = true
                    // webView.addJavascriptInterface(jsInterface, "JSInterface")
                    findViewById<WebView>(R.id.webView).loadUrl(headerList[groupPosition].url + "?gp=1&device=android&version=2")
                    onBackPressed()
                }
            }

            false
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            if (childList[headerList[groupPosition]] != null) {
                val model = childList[headerList[groupPosition]]!![childPosition]
                if (model.url.length > 0) {
                    // val webView = findViewById(R.id.webView) as WebView
                    // val jsInterface = JavaScriptInterface(this)
                    // webView.settings.javaScriptEnabled = true
                    // webView.addJavascriptInterface(jsInterface, "JSInterface")
                    findViewById<WebView>(R.id.webView).loadUrl(model.url + "?gp=1&device=android&version=2")
                    onBackPressed()
                }
            }

            false
        }
    }

    // Конец создания бокового меню

    // Начало поиска

    private fun hideSearchBar() {
        mySearchGrayShadow.visibility = View.INVISIBLE
        mySearchTextFieldLayout.visibility = View.INVISIBLE
        mySearchCloseButton.visibility = View.INVISIBLE
        mySearchMakeButton.visibility = View.INVISIBLE
        mySearchWhiteBar.visibility = View.INVISIBLE
        mySearchTextField.clearFocus()
        hideKeyboard(window.context)
    }

    private fun showSearchBar() {
        mySearchWhiteBar.bringToFront()
        mySearchWhiteBar.invalidate()
        mySearchWhiteBar.visibility = View.VISIBLE

        mySearchGrayShadow.bringToFront()
        mySearchGrayShadow.invalidate()
        mySearchGrayShadow.visibility = View.VISIBLE

        mySearchTextFieldLayout.bringToFront()
        mySearchTextFieldLayout.invalidate()
        mySearchTextFieldLayout.visibility = View.VISIBLE

        mySearchCloseButton.bringToFront()
        mySearchCloseButton.invalidate()
        mySearchCloseButton.visibility = View.VISIBLE

        mySearchMakeButton.bringToFront()
        mySearchMakeButton.invalidate()
        mySearchMakeButton.visibility = View.VISIBLE

        mySearchTextField.requestFocus()

        showKeyboard(window.context,mySearchTextField)
    }

    private fun makeSearch() {
        hideSearchBar()
        val searchString = URLEncoder.encode(mySearchTextField.text.toString(), "UTF-8")
        val url = "https://www.kaluga-poisk.ru/search?search=" + searchString + "&gp=1&device=android&version=2"
        val jsInterface = JavaScriptInterface(this)
        findViewById<WebView>(R.id.webView).settings.javaScriptEnabled = true
        findViewById<WebView>(R.id.webView).addJavascriptInterface(jsInterface, "JSInterface")
        findViewById<WebView>(R.id.webView).loadUrl(url)
    }

    fun hideKeyboard(activityContext: Context) {

        val imm = activityContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = (activityContext as Activity)
            .findViewById<View>(android.R.id.content).rootView

        imm.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    fun showKeyboard(activityContext: Context, editText: EditText) {

        val imm = activityContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!editText.hasFocus()) {
            editText.requestFocus()
        }

        editText.post { imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED) }
    }

    // Конец поиска





}

