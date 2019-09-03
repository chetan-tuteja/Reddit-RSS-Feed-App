package com.chetantuteja.pocketredditreader.Account

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.chetantuteja.pocketredditreader.Comments.CommentActivity
import com.chetantuteja.pocketredditreader.FeedAPI
import com.chetantuteja.pocketredditreader.R
import com.chetantuteja.pocketredditreader.URLS
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG: String = "LoginActivity"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate: Started ")
        loginScreenProgressbar.visibility = View.GONE
    }

    fun btnLoginClick(view: View){
        val username = input_username.text.toString()
        val password = input_password.text.toString()


        when {
            username.trim().isEmpty() -> {
                input_username.error = getString(R.string.empty_username)
                input_username.requestFocus()
            }
            password.isEmpty() -> {
                input_password.error = getString(R.string.empty_password)
                input_password.requestFocus()
            }
            else -> {

                login(username,password)
                val inputManger = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                loginScreenProgressbar.visibility = View.VISIBLE
                inputManger.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        }
    }

    private fun login(username: String, password: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(URLS.LOGIN_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val feedAPI = retrofit.create(FeedAPI::class.java)
        val headerMap = HashMap<String, String>()
        headerMap["Content-Type"] = "application/json"
        val call = feedAPI.signIN(headerMap,username,username,password,"json")

        call.enqueue(object: Callback<VerifyLogin>{
            override fun onFailure(call: Call<VerifyLogin>, t: Throwable) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.message.toString())
                loginScreenProgressbar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "An Error Occurred.", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<VerifyLogin>, response: Response<VerifyLogin>) {
                Log.d(TAG, "onResponse:  Server Response: $response")
                //Log.d(TAG, "onResponse:  Feed: ${response.body().toString()}")
                processLogin(username,response)
            }

        })
    }

    private fun processLogin(username: String, response: Response<VerifyLogin>) {
        val resBody = response.body()
        if (resBody != null) {
            try {
                val modhash = resBody.json.data.modhash
                val cookie = resBody.json.data.cookie

                Log.d(TAG, "processLogin: Modhash= $modhash ")
                Log.d(TAG, "processLogin: Cookie= $cookie ")
                if(modhash.isNotEmpty() && cookie.isNotEmpty()){
                    saveSessionParams(username,modhash,cookie)
                    loginScreenProgressbar.visibility = View.GONE
                    input_username.setText("")
                    input_password.setText("")
                    Toast.makeText(this@LoginActivity, "Successfully logged in.",Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Something went wrong.",Toast.LENGTH_LONG).show()
                }
            } catch (e: NullPointerException) {
                Log.e(TAG, "processLogin:  NullPointerException"+e.message.toString())
            } catch (e: IllegalStateException) {
                Log.e(TAG, "processLogin:  IllegalStateException"+e.message.toString())
                Toast.makeText(this@LoginActivity, "Something went wrong. Try with correct credentials.",Toast.LENGTH_LONG).show()
                loginScreenProgressbar.visibility = View.GONE
            }
        }
    }

    private fun saveSessionParams(username: String, modhash: String, cookie: String){
        val prefs = getSharedPreferences(getString(R.string.SessionFileName), Context.MODE_PRIVATE)
        val prefsEditor = prefs.edit()
        Log.d(TAG, "saveSessionParams: Saving Session Params: username= $username \n modhash = $modhash \n cookie = $cookie")
        prefsEditor.putString(getString(R.string.SessionUsername),username)
        prefsEditor.apply()
        prefsEditor.putString(getString(R.string.SessionModhash),modhash)
        prefsEditor.apply()
        prefsEditor.putString(getString(R.string.SessionCookies),cookie)
        prefsEditor.apply()

    }
}
