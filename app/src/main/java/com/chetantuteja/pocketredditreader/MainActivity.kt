package com.chetantuteja.pocketredditreader

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.chetantuteja.pocketredditreader.Account.LoginActivity
import com.chetantuteja.pocketredditreader.Comments.CommentActivity
import com.chetantuteja.pocketredditreader.URLS.Companion.BASE_URL
import com.chetantuteja.pocketredditreader.datamodels.Feed
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity"

    }

    private lateinit var myAdapter: FeedRecyclerViewAdapter
    private lateinit var currentFeed: String
    private lateinit var postList: ArrayList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupRecyclerView()

    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarMain)
        toolbarMain.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {item ->
            Log.d(TAG, "onMenuItemClicked: clicked menu item $item")
            when {
                item.itemId == R.id.navLogin -> {
                    val intent = Intent(this@MainActivity,LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            false
        })
    }

    fun getFeedClick(view: View) {
        if (!isNetworkAvailable()) {
            dialogMaker(getString(R.string.net_connection))
        } else {
            val sReddit = etGetFeed.text.toString()
            if (sReddit.trim().isEmpty()) {
                etGetFeed.error = getString(R.string.empty_sreddit)
                etGetFeed.requestFocus()

            } else {
                currentFeed = sReddit
                setupRetrofit()
                val inputManger = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManger.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        }

    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        val feedAPI = retrofit.create(FeedAPI::class.java)
        val feed = feedAPI.getFeed(currentFeed)
        feed.enqueue(object : Callback<Feed> {
            override fun onFailure(call: Call<Feed>, t: Throwable) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.message.toString())
                Toast.makeText(this@MainActivity, "An Error Occurred.", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                Log.d(TAG, "onResponse:  Feed: " + response.body().toString())
                Log.d(TAG, "onResponse:  Server Response: $response")

                setupPostFeed(response)
            }

        })

    }

    private fun setupPostFeed(response: Response<Feed>) {
        val resBody = response.body()
        if (resBody != null) {
            val entries = resBody.entries

            postList = ArrayList<Post>()
            for (i in 0 until entries.size) {
                var postContent: ArrayList<String?>

                val extractXML = ExtractXML(entries[i].content, "<a href=")
                postContent = extractXML.start()

                val extractXML2 = ExtractXML(entries[i].content, "<img src=")
                try {
                    postContent.add(extractXML2.start()[0])
                } catch (e: NullPointerException) {
                    postContent.add(null)
                    Log.e(TAG, "onResponse: NullPointerException(thumbnail) " + e.message.toString())
                } catch (e: IndexOutOfBoundsException) {
                    postContent.add(null)
                    Log.e(TAG, "onResponse: IndexOutOfBoundsException(thumbnail) " + e.message.toString())
                }

                try {
                    val index = getPostLinkIndex(postContent)

                    val toPutPost = Post(
                        entries[i].title,
                        entries[i].author.name,
                        entries[i].updated, postContent[index],
                        postContent[postContent.lastIndex],
                        entries[i].id
                    )
                    postList.add(toPutPost)
                } catch (e: NullPointerException) {
                    Log.e(TAG, "onResponse: NullPointerException(No Author) " + e.message.toString())
                    val index = getPostLinkIndex(postContent)
                    val toPutPost = Post(
                        entries[i].title,
                        "None",
                        entries[i].updated, postContent[index],
                        postContent[postContent.lastIndex],
                        entries[i].id
                    )
                    postList.add(toPutPost)
                }


            }
            myAdapter = FeedRecyclerViewAdapter()
            mainRecyclerView.adapter = myAdapter
            myAdapter.submitList(postList)
        }
    }

    private fun getPostLinkIndex(postContent: ArrayList<String?>):Int {
        var toRet = 0
        for(i in 0 until postContent.size) {
            if(postContent[i] != null) {
                if(postContent[i]!!.contains("/comments/")) {
                    toRet = i
                }
            }
        }
        return toRet
    }

    private fun setupRecyclerView() {
        mainRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        mainRecyclerView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                Log.d(TAG, "onItemClick: RVMain, Clicked on "+postList[position].toString())
                val intent = Intent(this@MainActivity, CommentActivity::class.java)
                intent.putExtra(getString(R.string.post_url),postList[position].postURL)
                intent.putExtra(getString(R.string.post_thumbnail),postList[position].thumbnailURL)
                intent.putExtra(getString(R.string.post_title),postList[position].title)
                intent.putExtra(getString(R.string.post_author),postList[position].author)
                intent.putExtra(getString(R.string.post_updated),postList[position].postDate)
                intent.putExtra(getString(R.string.post_id),postList[position].id)
                startActivity(intent)
            }

        })

    }

    private fun isNetworkAvailable(): Boolean {
        val cManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeInfo = cManager.activeNetwork
        if (activeInfo != null) {
            val cap = cManager.getNetworkCapabilities(activeInfo)
            if (cap != null) {
                cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                return true
            }
        }
        return false
    }

    private fun dialogMaker(dialogText: String) {
        MaterialDialog(this).show {
            title(R.string.error)
            message(text = dialogText)
            positiveButton(R.string.btn_OK) { dialog ->
                dialog.dismiss()
            }
            cornerRadius(16f)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }
}
