package com.example.filefinder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var btnScan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var txtResult: TextView

    private val storagePermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        progressBar = findViewById(R.id.progressBar)
        txtResult = findViewById(R.id.txtResult)

        btnScan.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(Intent.createChooser(intent, "Choose folder to scan"), 999)
        }
    }

    private fun checkPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            storagePermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storagePermissionCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            scanFiles()
        } else {
            txtResult.text = "Permission denied. Cannot scan files."
        }
    }

    private fun scanFiles() {
        progressBar.visibility = ProgressBar.VISIBLE
        txtResult.text = "Scanning... Please wait."

        CoroutineScope(Dispatchers.IO).launch {
            val rootDir = Environment.getExternalStorageDirectory()
            val files = getAllFiles(rootDir)
            val duplicates = findDuplicates(files)

            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                if (duplicates.isEmpty()) {
                    txtResult.text = "No duplicate files found."
                } else {
                    val result = StringBuilder("Duplicate files found:\n\n")
                    for ((key, list) in duplicates) {
                        if (list.size > 1) {
                            result.append("Group: $key\n")
                            list.forEach { file ->
                                result.append("→ ${file.absolutePath}\n")
                            }
                            result.append("\n")
                        }
                    }
                    txtResult.text = result.toString()
                }
            }
        }
    }

    private fun getAllFiles(dir: File): List<File> {
        val fileList = mutableListOf<File>()
        val files = dir.listFiles() ?: return emptyList()

        for (file in files) {
            if (file.isDirectory) {
                fileList.addAll(getAllFiles(file))
            } else {
                fileList.add(file)
            }
        }
        return fileList
    }

    private fun findDuplicates(files: List<File>): Map<String, List<File>> {
        return files.groupBy { "${it.name}_${it.length()}" }
            .filter { it.value.size > 1 }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val path = uri.path ?: return
            txtResult.text = "Selected folder: $path\nScanning..."

            CoroutineScope(Dispatchers.IO).launch {
                val fileList = mutableListOf<File>()
                val rootDir = Environment.getExternalStorageDirectory() // fallback if SAF fails
                fileList.addAll(getAllFiles(rootDir))

                val duplicates = findDuplicates(fileList)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = ProgressBar.GONE
                    if (duplicates.isEmpty()) {
                        txtResult.text = "No duplicate files found."
                    } else {
                        val result = StringBuilder("Duplicate files found:\n\n")
                        for ((key, list) in duplicates) {
                            if (list.size > 1) {
                                result.append("Group: $key\n")
                                list.forEach { file ->
                                    result.append("→ ${file.absolutePath}\n")
                                }
                                result.append("\n")
                            }
                        }
                        txtResult.text = result.toString()
                    }
                }
            }
        }
    }
}
