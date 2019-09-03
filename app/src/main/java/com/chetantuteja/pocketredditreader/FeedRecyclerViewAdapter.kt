package com.chetantuteja.pocketredditreader

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.card_recycler_layout.view.*

class FeedRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var postList: ArrayList<Post> = ArrayList<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_recycler_layout,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is FeedViewHolder -> {
                holder.bind(postList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun submitList(listWeGet: ArrayList<Post>) {
        postList = listWeGet
    }

    class FeedViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {

        val cardImage = itemView.cardImage
        val cardTitle = itemView.cardTitle
        val cardAuthor = itemView.cardAuthor
        val cardUpdated = itemView.cardUpdated
        val cardProgressDialog = itemView.cardProgressDialog

        fun bind(post: Post) {
            cardTitle.text = post.title
            cardAuthor.text = post.author
            cardUpdated.text = post.postDate

            /*if(post.thumbnailURL!=null){
                Log.d("Chetu",post.thumbnailURL)
            }*/


            val requestOpt = RequestOptions()
                .placeholder(R.drawable.reddit_alien)
                .error(R.drawable.reddit_alien)

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOpt)
                //.asBitmap()
                .load(post.thumbnailURL)
                .listener(object: RequestListener<Drawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        cardProgressDialog.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        cardProgressDialog.visibility = View.GONE
                        return false
                    }

                })
                .into(cardImage)
        }

    }


}