package com.chetantuteja.pocketredditreader.Comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.chetantuteja.pocketredditreader.R
import kotlinx.android.synthetic.main.comment_recycler_layout.view.*

class CommentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var commentList: ArrayList<Comment> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_recycler_layout,parent,false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CommentViewHolder -> {
                holder.bind(commentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun submitCommentList(listWeGet: ArrayList<Comment>) {
        commentList = listWeGet
    }

    class CommentViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val commentMain = itemView.commentMain
        val commentAuthor = itemView.commentAuthor
        val commentUpdated = itemView.commentUpdated
        val commentDataProgressDialog = itemView.commentDataProgressDialog

        fun bind(commentPost: Comment) {
            commentMain.text = HtmlCompat.fromHtml(commentPost.comment,HtmlCompat.FROM_HTML_MODE_LEGACY)
            commentAuthor.text = commentPost.author
            commentUpdated.text = commentPost.updated
            commentDataProgressDialog.visibility = View.GONE
        }
    }

}