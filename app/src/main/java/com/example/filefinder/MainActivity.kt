package com.example.filefinder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.filefinder.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private data class FileInfo(val name: String, val size: Long, val uri: Uri)

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val results = scanFilesFromUri(it)
                    withContext(Dispatchers.Main) {
                        showResults(results)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectFolder.setOnClickListener {
            openFolderPicker()
        }
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderPickerLauncher.launch(intent)
    }

    private suspend fun scanFilesFromUri(uri: Uri): Map<String, List<FileInfo>> {
        val files = mutableListOf<FileInfo>()

        suspend fun scanDirectory(dirUri: Uri, treeUri: Uri) {
            val dirDocId = DocumentsContract.getDocumentId(dirUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)

            contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val size = cursor.getLong(sizeIndex)
                    val mimeType = cursor.getString(mimeIndex)
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)

                    if (DocumentsContract.Document.MIME_TYPE_DIR == mimeType) {
                        scanDirectory(documentUri, treeUri)
                    } else {
                        files.add(FileInfo(name, size, documentUri))
                    }
                }
            }
        }

        withContext(Dispatchers.IO) {
            val treeDocId = DocumentsContract.getTreeDocumentId(uri)
            val treeDocUri = DocumentsContract.buildDocumentUriUsingTree(uri, treeDocId)
            scanDirectory(treeDocUri, uri)
        }

        return files.groupBy { "${it.name} | Size: ${it.size}" }
            .filter { it.value.size > 1 }
    }


    private fun showResults(duplicates: Map<String, List<FileInfo>>) {
        binding.resultContainer.removeAllViews()

        if (duplicates.isEmpty()) {
            binding.txtResult.text = "No duplicates found!"
            return
        }

        binding.txtResult.text = "Duplicates found: ${duplicates.size}"

        duplicates.forEach { (key, files) ->
            val textView = TextView(this)
            textView.text = "Duplicate: $key (${files.size} files)"
            textView.setPadding(8)
            binding.resultContainer.addView(textView)
        }
    }
}
