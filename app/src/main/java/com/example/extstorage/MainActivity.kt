package com.example.extstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
// reada write
import android.content.Context
import java.io.*
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    // read write
    private lateinit var file: File
    private val fileName = "test.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val external = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val cache = applicationContext.externalCacheDir

        Log.v("### External ###", "$external")
        Log.v("### External Cache ###", "$cache")

        // read write
        val context: Context = applicationContext

        //val fileName = "paper.json"
        file = File(context.applicationContext.externalCacheDir, fileName)

        val str = readFile()
        //Log.v("### External str ###", "$str")
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