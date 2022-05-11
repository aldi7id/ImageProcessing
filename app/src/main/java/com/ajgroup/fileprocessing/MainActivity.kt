package com.ajgroup.fileprocessing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ajgroup.fileprocessing.databinding.ActivityMainBinding
import com.ajgroup.fileprocessing.utils.PermissionUtils
import com.ajgroup.fileprocessing.utils.StorageUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import java.util.*



class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data
                    if (fileUri != null) {
                        loadImage(fileUri)
                    }

                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {

                }
            }
        }


    private val cameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                val uri = StorageUtils.savePhotoToExternalStorage(
                    contentResolver,
                    UUID.randomUUID().toString(),
                    bitmap
                )
                uri?.let {
                    loadImage(it)
                }
            }
        }

    private fun loadImage(uri: Uri) {
        binding.ivImagePreview.setImageURI(uri)
    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()
            .saveDir(
                File(
                    externalCacheDir,
                    "ImagePicker"
                )
            ) //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }
    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            loadImage(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners(){
        binding.btnFromCamera.setOnClickListener {
            if(PermissionUtils.isPermissionsGranted(this,getRequiredPermission())){
                openCamera()
            }
        }
        binding.btnFromGallery.setOnClickListener {
            if(PermissionUtils.isPermissionsGranted(this,getRequiredPermission())){
                openGallery()
            }
        }
        binding.btnFromLibrary.setOnClickListener {
            openImagePicker()
        }
    }


    private fun openGallery() {
        intent.type = "image/*"
        galleryResult.launch("image/*")
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResult.launch(cameraIntent)
    }

    private fun getRequiredPermission(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }
}