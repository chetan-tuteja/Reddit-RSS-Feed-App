package com.chetantuteja.pocketredditreader.Comments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.renderscript.ScriptGroup
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chetantuteja.pocketredditreader.*
import com.chetantuteja.pocketredditreader.Account.LoginActivity
import com.chetantuteja.pocketredditreader.URLS.Companion.BASE_URL
import com.chetantuteja.pocketredditreader.datamodels.Feed
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.comment_input_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException

class CommentActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "CommentActivity"
    }

    private lateinit var urlPost: String
    private var thumbnailPost: String? = ""
    private lateinit var titlePost: String
    private lateinit var authorPost: String
    private lateinit var updatedPost: String
    private lateinit var idPost: String
    private lateinit var dialog: Dialog

    private lateinit var username:String
    private lateinit var modhash: String
    private lateinit var cookie: String

    private lateinit var currentFeed: String
    private lateinit var commentList: ArrayList<Comment>
    private lateinit var myAdapter: CommentRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        getSessionParams()

        setupToolbar()
        commentProgressBar.visibility = View.VISIBLE
        loadingCommentsTV.visibility = View.VISIBLE
        setupRecyclerView()
        fetchIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarComment)
        toolbarComment.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            Log.d(TAG, "onMenuItemClicked: clicked menu item $item")
            when {
                item.itemId == R.id.navLogin -> {
                    val intent = Intent(this@CommentActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            false
        })
    }

    private fun fetchIntent() {
        val intent = intent
        if (intent != null && intent.hasExtra(getString(R.string.post_url))) {
            urlPost = intent.getStringExtra(getString(R.string.post_url))!!
            thumbnailPost = intent.getStringExtra(getString(R.string.post_thumbnail))
            titlePost = intent.getStringExtra(getString(R.string.post_title))!!
            authorPost = intent.getStringExtra(getString(R.string.post_author))!!
            updatedPost = intent.getStringExtra(getString(R.string.post_updated))!!
            idPost = intent.getStringExtra(getString(R.string.post_id))!!
        }

        Log.d(TAG, "urlPOST: $urlPost")

        try {
            val splitURL = urlPost.split(BASE_URL)
            currentFeed = splitURL[1]
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.d(TAG, "fetchIntent: NSFW_Post: Exception"+e.message.toString())
        }
        setupRetrofit()
        setupPostHeaderInActivity()
    }

    private fun setupRecyclerView() {
        commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
        }
        commentRecyclerView.addOnItemClickListener(object: OnItemClickListener{
            override fun onItemClicked(position: Int, view: View) {
                popupCommentDialog(commentList[position].id)
            }

        })
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        val feedAPI = retrofit.create(FeedAPI::class.java)
        val call = feedAPI.getFeed(currentFeed)
        call.enqueue(object: Callback<Feed>{
            override fun onFailure(call: Call<Feed>, t: Throwable) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.message.toString())
                Toast.makeText(this@CommentActivity, "An Error Occurred.", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                Log.d(TAG, "onResponse:  Server Response: $response")
                setupCommentFeed(response)
            }

        })
    }

    private fun setupCommentFeed(response: Response<Feed>) {
        val resBody = response.body()
        if (resBody != null) {
            val entries = resBody.entries

            commentList = ArrayList()
            for(i in 0 until entries.size) {
                var commentContent: ArrayList<String?>
               // Log.d(TAG, "setupCommentFeed: Entries: "+entries[i].toString()+"\n")
                val extractXML = ExtractXML(entries[i].content, "<div class=\"md\"><p>","</p>")
                commentContent = extractXML.start()

                try {
                    commentList.add(Comment(
                        commentContent[0],
                        entries[i].author.name,
                        entries[i].updated,
                        entries[i].id))
                } catch (e: IndexOutOfBoundsException) {
                    Log.e(TAG, "setupCommentFeed:  IndexOutOfBoundsException"+e.message.toString())
                    commentList.add(Comment(
                        "Error reading the Comment.",
                        "None",
                        "None",
                        "None"))
                } catch (e: NullPointerException) {
                    Log.e(TAG, "setupCommentFeed:  NullPointerException"+e.message.toString())
                    commentList.add(Comment(
                        commentContent[0],
                        "None",
                        entries[i].updated,
                        entries[i].id))
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "setupCommentFeed:  IllegalStateException"+e.message.toString())
                    commentList.add(Comment(
                        commentContent[0],
                        "None",
                        entries[i].updated,
                        entries[i].id))
                }
            }

            myAdapter = CommentRecyclerViewAdapter()
            commentRecyclerView.adapter = myAdapter
            myAdapter.submitCommentList(commentList)

            commentProgressBar.visibility = View.GONE
            loadingCommentsTV.text = ""
            loadingCommentsTV.visibility = View.GONE

        }
    }

    private fun setupPostHeaderInActivity() {

        postTitle.text = titlePost
        postAuthor.text = authorPost
        postUpdated.text = updatedPost

        val requestOpt = RequestOptions()
            .placeholder(R.drawable.reddit_alien)
            .error(R.drawable.reddit_alien)

        Glide.with(this@CommentActivity)
            .applyDefaultRequestOptions(requestOpt)
            .load(thumbnailPost)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    postProgressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                   postProgressBar.visibility = View.GONE
                    return false
                }

            })
            .into(postThumbnail)

        postThumbnail.setOnClickListener{
            Log.d(TAG, "onClick: Opening URL in webpage. ")
            val intent = Intent(this@CommentActivity, WebViewActivity::class.java)
            intent.putExtra(getString(R.string.webpage_url), urlPost)
            startActivity(intent)
        }
    }

    fun replyButtonCommentClick(view: View) {
        popupCommentDialog(idPost)
    }

    private fun popupCommentDialog(post_id: String) {
        Log.d(TAG, "popupCommentDialog: Entered the method")
        val width = (resources.displayMetrics.widthPixels*0.95).toInt()
        val height = (resources.displayMetrics.heightPixels*0.65).toInt()

         dialog = Dialog(this@CommentActivity)
        dialog.setTitle(R.string.post_a_comment)
        dialog.setContentView(R.layout.comment_input_dialog)
        dialog.window!!.setLayout(width, height)
        dialog.show()



        dialog.btnPostCommentDialog.setOnClickListener {
            Log.d(TAG, "onClick: Attempting to post comment ")
            val commentText = dialog.dialogComment.text.toString()
            postCommentRetrofit(post_id,commentText)

        }

    }

    private fun postCommentRetrofit(post_id: String, commentText: String){
        if(commentText.isEmpty()){
            Toast.makeText(this@CommentActivity, "Comment cannot be empty.",Toast.LENGTH_SHORT).show()
        }
        else {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(URLS.COMMENT_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val feedAPI = retrofit.create(FeedAPI::class.java)
                val headerMap = HashMap<String, String>()
                headerMap["User-Agent"] = username
                headerMap["X-Modhash"] = modhash
                headerMap["cookie"] = "reddit_session=$cookie"

                Log.d(
                    TAG,
                    "btnPostComment: Getting Session Params: username= $username \n modhash = $modhash \n cookie = $cookie"
                )

                val call = feedAPI.submitComment(headerMap, "comment", post_id, commentText)
                call.enqueue(object : Callback<CheckComment> {
                    override fun onFailure(call: Call<CheckComment>, t: Throwable) {
                        Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.message.toString())
                        Toast.makeText(this@CommentActivity, "An Error Occurred.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<CheckComment>, response: Response<CheckComment>) {
                        Log.d(TAG, "onResponse:  Server Response: $response")
                        checkCommentPostSuccess(response)
                    }

                })
            } catch (e: UninitializedPropertyAccessException) {
                Log.e(TAG, "postCommentRetrofit:  UninitializedPropertyAccessException"+e.message.toString())
                Toast.makeText(this@CommentActivity, "An Error Occurred. Did you Sign in?",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun checkCommentPostSuccess(response: Response<CheckComment>) {
        val resBody = response.body()
        if(resBody!=null){
            val postSuccess = resBody.success
            Log.d(TAG, "checkCommentPost: value = $postSuccess ")
            if(postSuccess == "true"){
                Toast.makeText(this@CommentActivity, "Comment Successfully posted.",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this@CommentActivity, "An Error Occurred.",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            /*Toast.makeText(this@CommentActivity, "Comment Successfully posted.",Toast.LENGTH_SHORT).show()
            dialog.dismiss()*/
        }
    }

    private fun getSessionParams() {
        val prefs = getSharedPreferences(getString(R.string.SessionFileName), Context.MODE_PRIVATE)
        //Log.d("Chetu", "Entering getSessionParams")

        if(prefs.contains(getString(R.string.SessionUsername))) {
            //Log.d("Chetu", "Getting getSessionParams")
            username = prefs.getString(getString(R.string.SessionUsername), "")!!
            modhash = prefs.getString(getString(R.string.SessionModhash), "")!!
            cookie = prefs.getString(getString(R.string.SessionCookies), "")!!

            Log.d(TAG, "getSessionParams: Getting Session Params: username= $username \n modhash = $modhash \n cookie = $cookie")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.d(TAG, "onPostResume: Resuming Activity ")
        getSessionParams()
    }
}

