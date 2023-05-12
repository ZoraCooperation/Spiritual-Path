package com.xora.cooperation.ryan.spiritualpath.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class PermissionManager private constructor(){
    private var context: Context? = null

    companion object{
        @SuppressLint("StaticFieldLeak")
        private var instance: PermissionManager? = null
        fun getInstance(context: Context): PermissionManager? {
            if (instance == null){
                instance = PermissionManager()
            }
            instance!!.init(context)
            return instance
        }
    }
    private fun init(context: Context){
        this.context = context
    }
    fun checkPermissions(permissions: Array<String?>): Boolean{
        val size = permissions.size
        for (i in 0 until size){
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permissions[i]!!
            ) == PermissionChecker.PERMISSION_DENIED){
                return false
            }
        }
        return true
    }
    fun askPermissions(activity: Activity?, permissions: Array<String?>?, requestCode: Int){
        ActivityCompat.requestPermissions(activity!!, permissions!!, requestCode)
    }
    @Suppress("unused")
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ): Boolean{
        var isAllPermissionsGranted = true
        if (grantResults.isNotEmpty()){
            for (i in grantResults.indices){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(context,"Permission granted", Toast.LENGTH_SHORT).show()
                }else{
                    isAllPermissionsGranted = false
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                    showPermissionRational(activity, requestCode, permissions, permissions[i])
                    break
                }
            }
        }else{
            isAllPermissionsGranted = false
        }
        return isAllPermissionsGranted
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showPermissionRational(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String?>,
        deniedPermission: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission!!)){
                showMessageOKCancel(
                    "you need to allow access to the permission(s)!"
                ){ _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        askPermissions(activity, permissions, requestCode)
                    }
                }
                return
            }
        }
    }

    private fun showMessageOKCancel(msg: String?, onClickListener: OnClickListener?) {
        AlertDialog.Builder(context!!)
            .setMessage(msg)
            .setPositiveButton("Ok", onClickListener)
            .setNegativeButton("Cancel", onClickListener)
            .create()
            .show()
    }
}