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

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

import java.util.concurrent.TimeUnit


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
//import android.graphics.ImageDecoder
import android.net.Uri

import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
//import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var file: File
    private val fileName = "paper.json"
    private val config = mutableListOf<String>("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()

        readFile()
        val str: String = readJson()
        getParam(str)
        setwifi(config[1], config[2])
        TimeUnit.SECONDS.sleep(3)

        val webSocketClient = WebSocketClient(config[3], this)
        webSocketClient.send("Hello from Android")
    }

    // ------------------ camera start
    private val REQUEST_IMAGE_CAPTURE = 2
    private val RECORD_REQUEST_CODE = 1000

    private lateinit var camera_iv: ImageView
    private lateinit var camera_btn: Button
    private lateinit var currentPhotoPath: String

    // カメラを開くためのメソッド1
    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // カメラで撮った写真をイメージファイルに作り
                val photoFile: File? =
                    try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Log.d("TAG", "イメージファイルを生成中にエラーが発生")
                        null
                    }

                // イメージファイルを成功に作った場合onActivityForResultに送る
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this, "com.example.extstorage", it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }


    // カメラで撮った写真をイメージファイルに格納するためのメソッド
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.v("### CreateImageFile ###", "$timeStamp")
        Log.v("### CreateImageFile ###", "$storageDir")

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /*
    // onActivityResultにイメージ設定
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            2 -> {
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

                    // カメラから受け取ったデーターがある場合
                    val file = File(currentPhotoPath)
                    // SDKのバージョンが28以下の場合
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media
                            .getBitmap(contentResolver, Uri.fromFile(file))  //Deprecated
                        camera_iv.setImageBitmap(bitmap)
                    } else {
                        val decode = ImageDecoder.createSource(
                            this.contentResolver,
                            Uri.fromFile(file)
                        )
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        camera_iv.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
     */

    //パーミッションのチェックを設定するためのメソッド
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    //パーミッションをリクエストするためのメソッド
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE)
    }

    /*
    //パーミッションの許可の結果による実行されるメソッド
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when(requestCode){
            RECORD_REQUEST_CODE ->{
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "カメラ機能が許可されませんでした。", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, "カメラ機能が許可されました。", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
     */
    // ------------------ camera end

    private fun readFile(): String? {

        var text: String? = null
        val external = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val cache = applicationContext.externalCacheDir

        if (external != null) {
            Log.v("### External ###", "$external")
            Log.v("### External Cache ###", "$cache")

            // read write
            val context: Context = applicationContext
            file = File(context.applicationContext.externalCacheDir, fileName)

            try {
                //BufferedReader(FileReader(file)).use { br -> text = br.readLine() }
                BufferedReader(FileReader(file)).use { reader ->
                    var lineBuffer: String?
                    while (reader.readLine().also { lineBuffer = it } != null) {
                        text = lineBuffer
                        Log.v("### External str ###", "$text")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
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

    private fun getParam(str: String): String {
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

                if (jsonData.isNull("ipaddr") == false) {
                    config.add(jsonData.getString("ipaddr"))
                    Log.v("check", config[3])
                }
                /*
                if (jsonData.isNull("description") == false) {
                    config.add(jsonData.getString("description"))
                    Log.v("check", config[4])
                }
                */
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
        return "OK"
    }

    private fun setwifi(ssid: String, password: String): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

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
}

class WebSocketClient(val ipaddr: String, val main: MainActivity) : WebSocketListener() {

    private val ws: WebSocket

    init {
        val client = OkHttpClient()

        // 接続先のエンドポイント
        // localhostとか127.0.0.1ではないことに注意
        val request = Request.Builder()
            //.url("ws://10.0.2.2:8080")
            .url("ws://" + "$ipaddr" + ":3000")
            .build()

        ws = client.newWebSocket(request, this)
        Log.v("### Websocket init ###", "$ws")
    }

    fun send(message: String) {
        ws.send(message)
        Log.v("### Websocket messge ###", "$message")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("WebSocket opened successfully")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Received text message: $text")
        main.dispatchTakePictureIntent()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("Received binary message: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Connection closed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Connection failed: ${t.localizedMessage}")
    }
}