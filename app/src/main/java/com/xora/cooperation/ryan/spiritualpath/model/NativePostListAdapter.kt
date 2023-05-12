package com.xora.cooperation.ryan.spiritualpath.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.download.AndroidDownloader
import com.xora.cooperation.ryan.spiritualpath.util.PermissionManager

private const val postType = 1
private const val adType = 2
@Suppress("DEPRECATION")
class NativePostListAdapter(private val postList: ArrayList<Post>,
                            private val documentIdList: ArrayList<String>,
                            private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var nativeAd: NativeAd
    // on below line we are creating a
    // variable for ad view and ad request
    private var interAd: InterstitialAd? = null
    private var activity: Activity = context as Activity
    companion object{
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val permissions = arrayOf<String?>(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
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

    inner class AdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(){
            showNativeAd(nativeAd,itemView)
        }
    }

    fun setNativeAds(nativeAd: NativeAd){
        this.nativeAd = nativeAd
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            postType -> {
                PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.postview_linear_model,parent,false))
            }
            else -> {
                AdsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.native_ads_unified,parent,false))
            }
        }
    }

    override fun getItemCount(): Int {
        return documentIdList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (documentIdList[position] == "null"){
            adType
        }else{
            postType
        }
    }

    @SuppressLint("HardwareIds")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == postType){
            loadIntAd()
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
                .into((holder as PostViewHolder).post)

            //println(documentIdList[position])
            //println(id)

            holder.likeDatabaseReference.addValueEventListener(object : ValueEventListener {
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
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }

            })

            holder.downloadDatabaseReference.addValueEventListener(object : ValueEventListener {
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
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }

            })

            holder.shareDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    holder.shareCount = snapshot.child("Share").child(postKey).childrenCount.toInt()
                    holder.shareText.text = shareValidation(holder.shareCount)
                    holder.share.setImageResource(R.drawable.ic_nav_baseline_share)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }

            })

            holder.like.setOnClickListener {
                holder.isLike = true

                holder.likeDatabaseReference.addValueEventListener(object : ValueEventListener {
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
                        Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                    }
                })
            }
            holder.download.setOnClickListener {
                holder.isDownload = true

                holder.downloadDatabaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (holder.isDownload){
                            if (snapshot.child("Downloads").child(postKey).hasChild(id)){
                                Toast.makeText(context,"Already downloaded", Toast.LENGTH_SHORT).show()
                            }else{
                                //downloader.downloadFile(postUrl)
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
                                Handler().postDelayed({
                                    showIntAd()
                                }, 3000)
                            }
                            holder.isDownload = false
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
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
        }else{
            (holder as AdsViewHolder).onBind()
        }
    }

    private fun showNativeAd(nativeAd: NativeAd, itemView: View){
        itemView.apply {
            nativeAd.apply {
                //Init Native Ads Vies
                val adView: NativeAdView = findViewById(R.id.adView)
                val adMedia: MediaView = findViewById(R.id.adMedia)
                val adHeadline: TextView = findViewById(R.id.ad_headline)
                val adBody: TextView = findViewById(R.id.adBody)
                val adBtnAction: Button = findViewById(R.id.ad_call_to_action)
                val adAppIcon: ImageView = findViewById(R.id.ad_app_icon)
                val adPrice: TextView = findViewById(R.id.ad_price)
                val adStars: RatingBar = findViewById(R.id.ad_stars)
                val adStore: TextView = findViewById(R.id.ad_store)
                val adAdvertiser: TextView = findViewById(R.id.ad_advertiser)
                //Assign position of views inside the native ad view
                adView.mediaView = adMedia
                adView.headlineView = adHeadline
                adView.bodyView = adBody
                adView.callToActionView = adBtnAction
                adView.iconView = adAppIcon
                adView.priceView = adPrice
                adView.starRatingView = adStars
                adView.storeView = adStore
                adView.advertiserView = adAdvertiser
                //Assign Values to View
                adView.mediaView?.mediaContent = mediaContent!!
                adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                // The headline and media content are guaranteed to be in every UnifiedNativeAd.
                (adView.headlineView as TextView).text = headline
                // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
                // check before trying to display them.
                if(nativeAd.body == null){
                    (adView.bodyView as TextView).visibility = View.INVISIBLE
                }else{
                    (adView.bodyView as TextView).visibility = View.VISIBLE
                    (adView.bodyView as TextView).text = body
                }
                if (nativeAd.callToAction == null){
                    (adView.callToActionView as Button).visibility = View.INVISIBLE
                }else{
                    (adView.callToActionView as Button).visibility = View.VISIBLE
                    (adView.callToActionView as Button).text = callToAction
                }
                if (nativeAd.icon == null){
                    (adView.iconView as ImageView).visibility = View.GONE
                }else{
                    (adView.iconView as ImageView).visibility = View.VISIBLE
                    (adView.iconView as ImageView).setImageDrawable(icon?.drawable)
                }
                if (nativeAd.price == null){
                    (adView.priceView as TextView).visibility = View.INVISIBLE
                }else{
                    (adView.priceView as TextView).visibility = View.VISIBLE
                    (adView.priceView as TextView).text = price
                }
                if (nativeAd.store == null){
                    (adView.storeView as TextView).visibility = View.INVISIBLE
                }else{
                    (adView.storeView as TextView).visibility = View.VISIBLE
                    (adView.storeView as TextView).text = store
                }
                if (nativeAd.starRating == null){
                    (adView.starRatingView as RatingBar).visibility = View.INVISIBLE
                }else{
                    (adView.starRatingView as RatingBar).visibility = View.VISIBLE
                    (adView.starRatingView as RatingBar).rating = starRating!!.toFloat()
                }
                if (nativeAd.advertiser == null){
                    (adView.advertiserView as TextView).visibility = View.INVISIBLE
                }else{
                    (adView.advertiserView as TextView).visibility = View.VISIBLE
                    (adView.advertiserView as TextView).text = advertiser
                }

                // This method tells the Google Mobile Ads SDK that you have finished populating your
                // native ad view with this native ad.
                adView.setNativeAd(this)
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
            interAd?.show(activity)
        } else {
            // If the Ad is not loaded, a toast will be displayed and the intent will help to
            // navigate to the second activity
            Log.d("Tag","Ad was not loaded")
        }
    }

    private fun loadIntAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.interstitial_ad_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interAd = interstitialAd
                }
            })
    }

}