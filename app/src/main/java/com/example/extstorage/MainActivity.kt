package com.example.extstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
// read write
import android.content.Context
import java.io.*

/*
@Serializable
data class wifi_info(
    val ssid: null,
    val key: null,
    val title: null,
    val desctiption: null
)
*/
class MainActivity : AppCompatActivity() {
    // read write
    private lateinit var file: File
    //private val fileName = "test.txt"
    private val fileName = "paper.json"

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
            readJson()
            val data = readJson()
            Log.v("### External data ###","$data")
        }
    }

    // read write
    // ファイルを読み出し
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

    /*
    fun writeJson(data: String) {
        val context: Context = applicationContext
        // File(applicationContext.filesDir, filename).writer().use {
            it.write(data)
        }
    }
    */

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
    private fun readFile(): String? {
        var text: String? = null

        try {
            openFileInput(fileName).use { fileInputStream ->
                BufferedReader(
                    InputStreamReader(fileInputStream, StandardCharsets.UTF_8)
                ).use { reader ->
                    var lineBuffer: String?
                    while (reader.readLine().also { lineBuffer = it } != null) {
                        text = lineBuffer
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return text
    }
     */
}