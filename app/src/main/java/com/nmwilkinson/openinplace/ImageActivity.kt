package com.nmwilkinson.openinplace

import android.content.Intent
import android.content.Intent.ACTION_EDIT
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.nmwilkinson.openinplace.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val imageViewModel: ImageViewModel by viewModels()

    private var editImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                binding.image.setImageDrawable(null)
                binding.image.setImageURI(localFileUri())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.editButton.setOnClickListener { editImage(localFileUri()) }

        if (imageViewModel.sourceUri == null && intent?.data != null) {
            readViewImageIntent(intent)
        }

        setSourceUri(imageViewModel.sourceUri)
    }

    private fun editImage(uri: Uri) {
        val intent = Intent(ACTION_EDIT).apply {
            setDataAndType(uri, "image/jpeg")
            flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        editImageLauncher.launch(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent: $intent")
        if (intent != null) {
            readViewImageIntent(intent)
            setSourceUri(imageViewModel.sourceUri)
        }
    }

    private fun readViewImageIntent(intent: Intent) {
        if (intent.action == ACTION_VIEW && intent.type?.startsWith("image/") == true) {
            intent.data?.let { createLocalCopyOfImage(it) }
        }
    }

    private fun setSourceUri(sourceUri: Uri?) {
        binding.editButton.isEnabled = sourceUri != null
        binding.filePath.text = sourceUri?.toString() ?: "No file received yet"
        binding.image.setImageURI(null)
        binding.image.setImageURI(sourceUri)
    }

    private fun createLocalCopyOfImage(sourceUri: Uri) {
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(localFile()).use { outputStream ->
                copyStreams(inputStream, outputStream)
                imageViewModel.setSourceUri(localFileUri())
            }
        }
    }

    private fun localFileUri() =
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, localFile())

    private fun localFile() = File(filesDir, "tempimage.jpeg")
}

fun copyStreams(inputStream: InputStream, outputStream: OutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var len = inputStream.read(buffer)
    while (len != -1) {
        outputStream.write(buffer, 0, len)
        len = inputStream.read(buffer)
        if (Thread.interrupted()) {
            throw InterruptedException()
        }
    }
}