package com.kr.ac.kumoh.s20150088.iulist

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.security.AccessController.getContext
import java.text.ParseException
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    companion object{
        const val QUEUE_TAG="VolleyRequest"
        const val SERVER_URL="http://rnjsgur12.cafe24.com/IU/"
    }
    data class Song(var image:String, var song:String, var album:String)
    var arraySong  = ArrayList<Song>()
    lateinit var mQueue : RequestQueue
    var  mResult: JSONObject? = null
    var mAdapter = SongAdapter()
    lateinit var mImageLoader:ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(applicationContext)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        recyclerView.addItemDecoration(DividerItemDecoration(this, 1));


        CookieHandler.setDefault(CookieManager())
        mQueue = Volley.newRequestQueue(this)
        mImageLoader = ImageLoader(mQueue,
            object:ImageLoader.ImageCache{
                private val cache = LruCache<String, Bitmap>(20)
                override fun getBitmap(url: String?): Bitmap? {
                    return cache.get(url)
                }
                override fun putBitmap(url: String?, bitmap: Bitmap?) {
                    cache.put(url,bitmap)
                }
            })
        requestInternet()
    }
    override fun onStop() {
        super.onStop()
        mQueue.cancelAll(QUEUE_TAG)

    }
    private fun requestInternet() {
        val url = SERVER_URL+"select_iu.php"
        val request = JsonObjectRequest(Request.Method.GET,
            url, null,
            Response.Listener { response ->
                mResult = response
                drawList()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this,error.toString(),Toast.LENGTH_SHORT).show()
            })
        request.tag = QUEUE_TAG
        mQueue.add(request)
    }
    fun drawList() {
        val items = mResult?.getJSONArray("list")?:return
        arraySong.clear()
        try {
            for (i in 0 until items.length()) {
                var info = items[i] as JSONObject
                var imageSt = info.getString("image")
                var songSt = info.getString("song")
                var albumSt = info.getString("album")
                arraySong.add(Song(imageSt, songSt, albumSt))
            }
        } catch (e: JSONException) {

        } catch (e: NullPointerException) {

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        mAdapter.notifyDataSetChanged()
    }
    inner class SongAdapter(): RecyclerView.Adapter<SongAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var ivImage:NetworkImageView = itemView.findViewById(R.id.imageView)
            var tvSong:TextView = itemView.findViewById(R.id.textSong)
            var tvAlbum:TextView = itemView.findViewById<TextView>(R.id.textAl)


        }
        override fun getItemCount(): Int {
            return arraySong.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.custom_layout, parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.ivImage.setImageUrl(SERVER_URL+arraySong[position].image,mImageLoader)
            holder.tvSong.text = arraySong[position]?.song
            holder.tvAlbum.text = arraySong[position]?.album
            holder.itemView.setOnClickListener {
                var uri = Uri.parse("https://www.youtube.com/results?search_query="+arraySong[position].song+" 듣기")
                val intent = Intent(Intent.ACTION_VIEW,uri)
                startActivity(intent)
            }
        }
    }
}