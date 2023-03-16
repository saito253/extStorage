package com.example.extstorage

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var file: File
    //private val fileName = "test.txt"
    private val fileName = "paper.json"
    private val config = mutableListOf<String>("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val external = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val cache = applicationContext.externalCacheDir

        if (external != null) {
            Log.v("### External ###", "$external")
            Log.v("### External Cache ###", "$cache")

            // read write
            val context: Context = applicationContext
            file = File(context.applicationContext.externalCacheDir, fileName)
            readFile()
            val str: String = readJson()
            try {
                val jsonObject = JSONObject(str)
                val jsonArray = jsonObject.getJSONArray("sample")
                for (i in 0 until jsonArray.length()) {
                    val jsonData = jsonArray.getJSONObject(i)

                    if (jsonData.isNull("ssid") == false) {
                        config.add(jsonData.getString("ssid"))
                        Log.v("check", config[1])
                    }

                    if (jsonData.isNull("key") == false) {
                        config.add(jsonData.getString("key"))
                        Log.v("check", config[2])
                    }

                    if (jsonData.isNull("title") == false) {
                        config.add(jsonData.getString("title"))
                        Log.v("check", config[3])
                    }

                    if (jsonData.isNull("description") == false) {
                        config.add(jsonData.getString("description"))
                        Log.v("check", config[4])
                    }

                    /*
                    Log.d("Check", "$i : ${jsonData.getString("ssid")}")
                    Log.d("Check", "$i : ${jsonData.getString("key")}")
                    Log.d("Check", "$i : ${jsonData.getString("title")}")
                    Log.d("Check", "$i : ${jsonData.getString("description")}")
                    */
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        setwifi(config[1], config[2])
    }

    private fun setwifi(ssid: String, password: String): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // ensure that WiFi is enabled
        wifiManager.isWifiEnabled = true

        //val ssid = "xxxx" // set the desired SSID here
        //val password = "xxxxxxxxx" // set the password here

        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""
        wifiConfig.preSharedKey = "\"$password\""

        // ensure that this network is saved
        wifiConfig.status = WifiConfiguration.Status.ENABLED
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

        // add the network to the list of configured networks
        val networkId = wifiManager.addNetwork(wifiConfig)
        // enable the network
        wifiManager.enableNetwork(networkId, true)
        return "OK"
    }

    // read write
    private fun readFile(): String? {
        var text: String? = null

        try {
            //BufferedReader(FileReader(file)).use { br -> text = br.readLine() }
            BufferedReader(FileReader(file)).use {reader ->
                var lineBuffer: String?
                while (reader.readLine().also { lineBuffer = it } != null) {
                    text = lineBuffer
                    Log.v("### External str ###", "$text")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return text
    }

    private fun readJson(): String {
        val context: Context = applicationContext
        // val readFile = File(applicationContext.filesDir, filename)
        val readFile = File(context.applicationContext.externalCacheDir, fileName)
        if (readFile.exists()) {
            return readFile.bufferedReader().use(BufferedReader::readText)
        }
        return String()
    }
    /*
fun writeJson(data: String) {
    val context: Context = applicationContext
    // File(applicationContext.filesDir, filename).writer().use {
        it.write(data)
    }
}
*/
}