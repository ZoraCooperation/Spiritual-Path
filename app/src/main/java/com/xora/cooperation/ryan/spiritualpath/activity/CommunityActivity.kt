package com.xora.cooperation.ryan.spiritualpath.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xora.cooperation.ryan.spiritualpath.MainActivity
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.download.AndroidDownloader
import com.xora.cooperation.ryan.spiritualpath.util.PermissionManager
import com.xora.cooperation.ryan.spiritualpath.util.checkPermission

@Suppress("DEPRECATION")
class CommunityActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    // on below line we are creating a
    // variable for ad view and ad request
    private var interAd: InterstitialAd? = null

    private var isLike: Boolean = false
    private var isDownload: Boolean = false

    private var likeCount: Int = 0
    private var downloadCount: Int = 0
    private var shareCount: Int = 0

    companion object{
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val permissions = arrayOf<String?>(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Suppress("DEPRECATION")
    @SuppressLint("HardwareIds", "UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val likeDatabaseReference = Firebase.database.reference
        val downloadDatabaseReference = Firebase.database.reference
        val shareDatabaseReference = Firebase.database.reference

        val post = findViewById<ImageView>(R.id.post_image)
        val like = findViewById<ImageView>(R.id.favorite)
        val download = findViewById<ImageView>(R.id.download)
        val share = findViewById<ImageView>(R.id.share)

        val likeText = findViewById<TextView>(R.id.like)
        val downloadText = findViewById<TextView>(R.id.download_text)
        val shareText = findViewById<TextView>(R.id.share_text)

        val url = intent.getStringExtra("url")!!
        val docId = intent.getStringExtra("docId")!!
        val appUrl = "https://play.google.com/store/apps/details?id=com.xora.cooperation.ryan.spiritualpath&pli=1"
        val id: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        //getUrl = url
        val downloader = AndroidDownloader(this@CommunityActivity)
        // on below line we are
        // initializing our mobile ads.
        MobileAds.initialize(this){}
        loadIntAd()

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        toolbar.setNavigationOnClickListener {
            Intent(applicationContext, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Animatoo.animateSwipeLeft(this@CommunityActivity)
            }
        }
        Glide.with(this@CommunityActivity).asBitmap().load(url).into(post)

        likeDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (snapshot.child("Likes").child(docId).hasChild(id)){
                    likeCount = snapshot.child("Likes").child(docId).childrenCount.toInt()
                    likeText.text = likeValidation(likeCount)
                    like.setImageResource(R.drawable.ic_baseline_favorite)
                }else{
                    likeCount = snapshot.child("Likes").child(docId).childrenCount.toInt()
                    likeText.text = likeValidation(likeCount)
                    like.setImageResource(R.drawable.ic_baseline_favorite_border)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })

        downloadDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("Downloads").child(docId).hasChild(id)){
                    downloadCount = snapshot.child("Downloads").child(docId).childrenCount.toInt()
                    downloadText.text = downloadValidation(downloadCount)
                    download.setImageResource(R.drawable.ic_baseline_download_done)
                }else{
                    downloadCount = snapshot.child("Downloads").child(docId).childrenCount.toInt()
                    downloadText.text = downloadValidation(downloadCount)
                    download.setImageResource(R.drawable.ic_baseline_file_download)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })

        shareDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                shareCount = snapshot.child("Share").child(docId).childrenCount.toInt()
                shareText.text = shareValidation(shareCount)
                share.setImageResource(R.drawable.ic_nav_baseline_share)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })

        like.setOnClickListener {
            isLike = true

            likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isLike){
                        if (snapshot.child("Likes").child(docId).hasChild(id)){
                            likeDatabaseReference.child("Likes").child(docId).child(id).removeValue()
                        }else{
                            likeDatabaseReference.child("Likes").child(docId).child(id).setValue(true)
                        }
                        isLike = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
            })
        }
        download.setOnClickListener {
            isDownload = true

            downloadDatabaseReference.addValueEventListener(object :ValueEventListener{
                @SuppressLint("ObsoleteSdkInt")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isDownload){
                        if (snapshot.child("Downloads").child(docId).hasChild(id)){
                            Toast.makeText(this@CommunityActivity,"Already downloaded", Toast.LENGTH_SHORT).show()
                        }else{
                            if (!PermissionManager.getInstance(this@CommunityActivity)!!
                                    .checkPermissions(permissions)){
                                PermissionManager.getInstance(this@CommunityActivity)!!.askPermissions(
                                    this@CommunityActivity,
                                    permissions,
                                    PERMISSION_REQUEST_CODE
                                )
                            }else{
                                Log.d("Tag:", "Permission already granted")
                            }
                            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                                downloader.downloadFile(url)
                                downloadDatabaseReference.child("Downloads").child(docId).child(id).setValue(true)
                            }else if (Build.VERSION.SDK_INT >= 33){
                                downloader.downloadFile(url)
                                downloadDatabaseReference.child("Downloads").child(docId).child(id).setValue(true)
                            }
                            Handler().postDelayed({
                                showIntAd()
                            }, 3000)
                            /*if (Build.VERSION.SDK_INT in 23..26){
                                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                                //launcher permission request dialog
                                permissionLauncherSingle.launch(permission)
                                likeDatabaseReference.child("Downloads").child(docId).child(id).setValue(true)
                                Handler().postDelayed({
                                    showIntAd()
                                }, 3000)
                            }else{
                                downloader!!.downloadFile(url)
                                likeDatabaseReference.child("Downloads").child(docId).child(id).setValue(true)
                            }*/
                        }
                        isDownload = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }

            })
        }
        share.setOnClickListener {
            shareDatabaseReference.child("Share").child(docId).child(getRandomUUID()).setValue(true)

            val bitmapDrawable = post.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Spiritual_Path_${System.currentTimeMillis()}", null)
            val bitmapUri = Uri.parse(bitmapPath)
            Intent(Intent.ACTION_SEND).also {
                it.type = "image/"
                it.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                it.putExtra(Intent.EXTRA_TEXT, "More From Spiritual Path - $appUrl")
                startActivity(Intent.createChooser(it, "Share Image"))
            }
        }
    }
    /*private val permissionLauncherSingle = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        //here we will check if permission was now (from permission request dialog) or already granted or not. the param isGranted contains value true/false
        Log.d("TAG", "onActivityResult: isGranted: $isGranted")

        if (isGranted) {
            //Permission granted now do the required task here or call the function for that
            singlePermissionGranted()
        } else {
            //Permission was denied so can't do the task that requires that permission
            Log.d("TAG", "onActivityResult: Permission denied...")
            Toast.makeText(this@CommunityActivity, "Permission denied...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun singlePermissionGranted() {
        downloader!!.downloadFile(getUrl)
    }*/

    private fun showIntAd() {
        // First we ensure the Interstitial ad is not nullable

        if (interAd != null) {
            interAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    Log.d("Tag","Ad was fail to show full screen")
                }

                override fun onAdShowedFullScreenContent() {
                    //Input your code here
                    super.onAdShowedFullScreenContent()
                    Log.d("Tag","Ad was show full screen")
                }

                // When you exit the ad using the cancel button, the next activity is displayed.

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d("Tag","Ad was dismissed full screen")
                }

                override fun onAdImpression() {
                    // input your code here
                    super.onAdImpression()
                    Log.d("Tag","Ad was ad impression")
                }

                // What will happen when the ad is clicked

                override fun onAdClicked() {
                    //Input your code here
                    super.onAdClicked()
                    Log.d("Tag","Ad was clicked")
                }
            }
            interAd?.show(this@CommunityActivity)
        } else {
            // If the Ad is not loaded, a toast will be displayed and the intent will help to
            // navigate to the second activity
            Log.d("Tag","Ad was not loaded")
        }
    }

    private fun loadIntAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interAd = interstitialAd
                }
            })
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        /*Intent(applicationContext, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }*/
        Intent(applicationContext, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            Animatoo.animateSwipeLeft(this@CommunityActivity)
            finishActivity(1)
        }
    }
}