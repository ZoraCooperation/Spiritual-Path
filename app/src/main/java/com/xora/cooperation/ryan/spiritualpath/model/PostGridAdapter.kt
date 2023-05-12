package com.xora.cooperation.ryan.spiritualpath.model

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.activity.CommunityActivity

class PostGridAdapter(private val postList: ArrayList<Post>,
                      private val documentIdList: ArrayList<String>,
                      private val context: Context) : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val post: ImageView = itemView.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.postview_grid_model, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        val postKey: String = documentIdList[position]
        val postUrl: String = postList[position].url.toString()

        Glide.with(context)
            .asBitmap()
            .load(postList[position].url)
            .placeholder(R.drawable.loading_img)
            .error(R.drawable.warning_background)
            .into(holder.post)

        holder.post.setOnClickListener {
            Intent(context, CommunityActivity::class.java).also {
                it.putExtra("url", postUrl)
                it.putExtra("docId", postKey)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(it)
            }
        }
    }
}