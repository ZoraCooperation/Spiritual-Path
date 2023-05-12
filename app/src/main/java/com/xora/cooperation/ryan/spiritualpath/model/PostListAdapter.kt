package com.xora.cooperation.ryan.spiritualpath.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.download.AndroidDownloader
import com.xora.cooperation.ryan.spiritualpath.util.PermissionManager

class PostListAdapter(private val postList: ArrayList<Post>,
                      private val documentIdList: ArrayList<String>,
                      private val context: Context) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    private var activity: Activity = context as Activity
    companion object{
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val permissions = arrayOf<String?>(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var isLike: Boolean = false
        var isDownload: Boolean = false

        var likeCount: Int = 0
        var downloadCount: Int = 0
        var shareCount: Int = 0

        val likeDatabaseReference = Firebase.database.reference
        val downloadDatabaseReference = Firebase.database.reference
        val shareDatabaseReference = Firebase.database.reference

        val post: ImageView = itemView.findViewById(R.id.post_image)
        val like: ImageView = itemView.findViewById(R.id.favorite)
        val download: ImageView = itemView.findViewById(R.id.download)
        val share: ImageView = itemView.findViewById(R.id.share)

        val likeText: TextView = itemView.findViewById(R.id.like)
        val downloadText: TextView = itemView.findViewById(R.id.download_text)
        val shareText: TextView = itemView.findViewById(R.id.share_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.postview_linear_model, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    @Suppress("DEPRECATION")
    @SuppressLint("HardwareIds")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        val postKey: String = documentIdList[position]
        val postUrl: String = postList[position].url.toString()
        val appUrl = "https://play.google.com/store/apps/details?id=com.xora.cooperation.ryan.spiritualpath&pli=1"
        val downloader = AndroidDownloader(context)
        val id: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        Glide.with(context)
            .asBitmap()
            .load(postList[position].url)
            .placeholder(R.drawable.loading_img)
            .error(R.drawable.warning_background)
            .into(holder.post)

        //println(documentIdList[position])
        //println(id)

        holder.likeDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.child("Likes").child(postKey).hasChild(id)){
                    holder.likeCount = dataSnapshot.child("Likes").child(postKey).childrenCount.toInt()
                    holder.likeText.text = likeValidation(holder.likeCount)
                    holder.like.setImageResource(R.drawable.ic_baseline_favorite)
                }else{
                    holder.likeCount = dataSnapshot.child("Likes").child(postKey).childrenCount.toInt()
                    holder.likeText.text = likeValidation(holder.likeCount)
                    holder.like.setImageResource(R.drawable.ic_baseline_favorite_border)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        holder.downloadDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (snapshot.child("Downloads").child(postKey).hasChild(id)){
                    holder.downloadCount = snapshot.child("Downloads").child(postKey).childrenCount.toInt()
                    holder.downloadText.text = downloadValidation(holder.downloadCount)
                    holder.download.setImageResource(R.drawable.ic_baseline_download_done)
                }else{
                    holder.downloadCount = snapshot.child("Downloads").child(postKey).childrenCount.toInt()
                    holder.downloadText.text = downloadValidation(holder.downloadCount)
                    holder.download.setImageResource(R.drawable.ic_baseline_file_download)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        holder.shareDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                holder.shareCount = snapshot.child("Share").child(postKey).childrenCount.toInt()
                holder.shareText.text = shareValidation(holder.shareCount)
                holder.share.setImageResource(R.drawable.ic_nav_baseline_share)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        holder.like.setOnClickListener {
            holder.isLike = true

            holder.likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (holder.isLike){
                        if (snapshot.child("Likes").child(postKey).hasChild(id)){
                            holder.likeDatabaseReference.child("Likes").child(postKey).child(id).removeValue()
                        }else{
                            holder.likeDatabaseReference.child("Likes").child(postKey).child(id).setValue(true)
                        }
                        holder.isLike = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
        }
        holder.download.setOnClickListener {
            holder.isDownload = true

            holder.downloadDatabaseReference.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (holder.isDownload){
                        if (snapshot.child("Downloads").child(postKey).hasChild(id)){
                            Toast.makeText(context,"Already downloaded", Toast.LENGTH_SHORT).show()
                        }else{
                            if (!PermissionManager.getInstance(context)!!
                                    .checkPermissions(permissions)){
                                PermissionManager.getInstance(context)!!.askPermissions(
                                    activity,
                                    permissions,
                                    PERMISSION_REQUEST_CODE
                                )
                            }else{
                                Log.d("Tag:", "Permission already granted")
                            }
                            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                // Permission is already granted
                                downloader.downloadFile(postUrl)
                                holder.downloadDatabaseReference.child("Downloads").child(postKey).child(id).setValue(true)
                            }else if (Build.VERSION.SDK_INT >= 33){
                                downloader.downloadFile(postUrl)
                                holder.downloadDatabaseReference.child("Downloads").child(postKey).child(id).setValue(true)
                            }
                        }
                        holder.isDownload = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }

            })
        }
        holder.share.setOnClickListener {

            holder.shareDatabaseReference.child("Share").child(postKey).child(getRandomUUID()).setValue(true)

            val bitmapDrawable = holder.post.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val bitmapPath = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Spiritual_Path_${System.currentTimeMillis()}", null)
            val bitmapUri = Uri.parse(bitmapPath)
            Intent(Intent.ACTION_SEND).also {
                it.type = "image/"
                it.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                it.putExtra(Intent.EXTRA_TEXT, "More From Spiritual Path - $appUrl")
                context.startActivity(Intent.createChooser(it, "Share Image"))
            }
        }
    }

    private fun likeValidation(like: Int) : String{

        val numberString: String = if (kotlin.math.abs(like / 1000000) > 1){
            "+" + (like.toDouble() / 1000000.0).toString() + "m Like"
        }else if (kotlin.math.abs(like / 100) > 1){
            "+" + (like.toDouble() / 1000.0).toString() + "k Like"
        }else {
            "$like Like"
        }
        return numberString
    }

    private fun downloadValidation(download: Int) : String{
        val numberString: String = if (kotlin.math.abs(download / 1000000) > 1){
            "+" + (download.toDouble() / 1000000.0).toString() + "m Download"
        }else if (kotlin.math.abs(download / 100) > 1){
            "+" + (download.toDouble() / 1000.0).toString() + "k Download"
        }else {
            "$download Download"
        }
        return numberString
    }

    private fun shareValidation(share: Int) : String{
        val numberString: String = if (kotlin.math.abs(share / 1000000) > 1){
            "+" + (share.toDouble() / 1000000.0).toString() + "m Share"
        }else if (kotlin.math.abs(share / 100) > 1){
            "+" + (share.toDouble() / 1000.0).toString() + "k Share"
        }else {
            "$share Share"
        }
        return numberString
    }

    private fun getRandomUUID() : String{
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { charset.random() }
            .joinToString("")
    }

}