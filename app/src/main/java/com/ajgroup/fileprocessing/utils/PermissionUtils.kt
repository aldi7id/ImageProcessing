package com.ajgroup.fileprocessing.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

object PermissionUtils {
    const val REQUEST_CODE_PERMISSIONS = 100

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun isShouldShowRationale(activity: Activity, vararg permissions: String): Boolean = permissions.any {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }

    fun isPermissionsGranted(activity: Activity,permissions: Array<String>): Boolean {
        return if (!hasPermissions(activity,*permissions)) {
            if (isShouldShowRationale(activity, *permissions)) {
                showPermissionDeniedDialog(activity)
            } else {
                ActivityCompat.requestPermissions(activity, permissions,
                    REQUEST_CODE_PERMISSIONS
                )
            }
            false
        } else {
            true
        }
    }

    private fun showPermissionDeniedDialog(context: Context) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton(
                "App Settings"
            ) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

}