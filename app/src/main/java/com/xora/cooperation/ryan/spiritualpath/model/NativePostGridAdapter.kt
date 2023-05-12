package com.xora.cooperation.ryan.spiritualpath.model

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.activity.CommunityActivity


class NativePostGridAdapter(private val postList: ArrayList<Post>,
                            private val documentIdList: ArrayList<String>,
                            private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private lateinit var nativeAd: NativeAd
    val postType = 1
    val adType = 2

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val post: ImageView = itemView.findViewById(R.id.post_image)
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
                PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.postview_grid_model,parent,false))
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
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == postType){
            val postKey: String = documentIdList[position]
            val postUrl: String = postList[position].url.toString()

            Glide.with(context)
                .asBitmap()
                .load(postList[position].url)
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.warning_background)
                .into((holder as PostViewHolder).post)

            holder.post.setOnClickListener {
                Intent(context, CommunityActivity::class.java).also {
                    it.putExtra("url", postUrl)
                    it.putExtra("docId", postKey)
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(it)
                    Animatoo.animateInAndOut(context)
                }
            }
        }else{
            (holder as AdsViewHolder).onBind()
        }
    }

    private fun showNativeAd(nativeAd: NativeAd, itemView: View){
        itemView.apply {
            nativeAd.apply {
                //Init Native Ads Views
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
}