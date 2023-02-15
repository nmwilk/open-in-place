package com.nmwilkinson.openinplace

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.nmwilkinson.openinplace.databinding.ActivityEditBinding
import timber.log.Timber

class EditActivity : AppCompatActivity() {
    private var receivedUri: Uri? = null
    private lateinit var binding: ActivityEditBinding


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.imageContainer.closeListener = {
            receivedUri?.let { imageUri ->
                val editedBitmap = contentResolver.openInputStream(imageUri).use {
                    val createBitmap = Bitmap.createBitmap(
                        binding.imageContainer.width, binding.imageContainer.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(createBitmap)
                    binding.imageContainer.draw(canvas)
                    createBitmap
                }

                editedBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    80,
                    contentResolver.openOutputStream(imageUri)
                )
                setResult(RESULT_OK)
                finish()
            }
        }

        if (intent?.data != null) {
            handleIntent(intent)
        }

        binding.editImage.text =
            receivedUri?.let { "Editing Uri: $receivedUri" } ?: "Open a jpeg from a file manager"
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val hasWriteFlag = intent.flags.and(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0
            if (!hasWriteFlag) {
                binding.editImage.text = "Sending app didn't grant a write permission"
                binding.imageContainer.isEnabled = false
            }
            Timber.d("intent has write flag? $hasWriteFlag")
            if (intent.type?.startsWith("image/") == true) {
                intent.data?.let {
                    receivedUri = it
                    binding.image.setImageURI(it)
                }
            }
        }
    }
}