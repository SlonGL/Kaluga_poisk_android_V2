package ru.kaluga_poisk.portalkalugapoisk

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import android.os.AsyncTask
import org.json.JSONArray
import java.util.*

class HttpHandler {

    fun makeServiceCall(reqUrl: String): String? {
        var response: String? = null
        try {
            val url = URL(reqUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            // read the response
            val `in` = BufferedInputStream(conn.inputStream)
            response = convertStreamToString(`in`)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "MalformedURLException: " + e.message)
        } catch (e: ProtocolException) {
            Log.e(TAG, "ProtocolException: " + e.message)
        } catch (e: IOException) {
            Log.e(TAG, "IOException: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }

        return response
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()

        try {
            reader.forEachLine {
                sb.append(it).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return sb.toString()
    }

    companion object {

        private val TAG = HttpHandler::class.java.simpleName
    }
}


class LoadMenu : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params : Void) : Boolean {
        var childModelsList: MutableList<MenuModel> = ArrayList<MenuModel>()
        val loadHandler = HttpHandler()
        val jsonString = loadHandler.makeServiceCall("https://www.kaluga-poisk.ru/api/menu/get")
        if (jsonString != "") {
            try {
                val jObj = JSONObject(jsonString)
                val menuArr = jObj.getJSONArray("menu")

                for (i in 0 until menuArr.length()) {
                    childModelsList = ArrayList<MenuModel>()
                    val menuObj = JSONObject(menuArr[i].toString())
                    val menuTitle = menuObj["title"]
                    val menuUrl = menuObj["url"]
                    val menuItems = menuObj["items"].toString()
                    if (menuItems != "[]") {
                        val menuM = MenuModel(menuTitle.toString(), true, true, menuUrl.toString())
                        headerList.add(menuM)
                        val subMenuArr = JSONArray(menuItems)
                        for (i in 0 until subMenuArr.length()) {
                            val subMenuObj = JSONObject(subMenuArr[i].toString())
                            val subMenuTitle = subMenuObj["title"]
                            val subMenuUrl = subMenuObj["url"]
                            val subMenuM = MenuModel(subMenuTitle.toString(), false, false,subMenuUrl.toString())
                            childModelsList.add(subMenuM)
                            print("")
                        }
                        childList[menuM] = childModelsList
                        print("")
                    } else {
                        val menuM = MenuModel(menuTitle.toString(), true, false, menuUrl.toString())
                        headerList.add(menuM)
                    }
                    print("")
                }

                menuIsLoaded = true
                return true
            } catch (e: JSONException) {
                Log.v("MENU LOAD: ","ERROR JSON menu serialization: " + e.localizedMessage)
                var menuModel = MenuModel(
                  "Не удалось загрузить меню",true,false, siteURL)
                headerList.add(menuModel)
                menuIsLoaded = true
                return false
            }
        }
        Log.v("MENU LOAD: ","Не удалось загрузить JSON меню")
        var menuModel = MenuModel(
            "Не удалось загрузить меню",true,false, siteURL)
        headerList.add(menuModel)
        menuIsLoaded = true
        return false
    }


    override fun onPostExecute(result: Boolean) {
        if (result) {
            Log.v("MENU LOAD: ","Menu loaded")
        } else {
            Log.v("MENU LOAD: ","Menu not loaded")
        }
    }

}
