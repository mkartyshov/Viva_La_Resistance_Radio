package com.mkartyshov.viva_la_resistance_radio

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : AppCompatActivity() {

    var stream: String = StreamLink().execute().get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        val pager = findViewById<ViewPager2>(R.id.viewPager2)
        val tl = findViewById<TabLayout>(R.id.tabLayout)
        val bg: ImageView = findViewById(R.id.bg)
        pager.adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)

        val tabLogos: Array<Int> = arrayOf(
            R.drawable.info,
            R.drawable.player,
            R.drawable.songlist
        )

        pager.setCurrentItem(1, false)

        TabLayoutMediator(tl, pager) { tab, position ->
            tab.setIcon(tabLogos[position])
        }.attach()

        Timer().scheduleAtFixedRate(2000, 300000) {
            val song = MusicService.SongName().execute().get()
            setBackground(song, bg)
        }
    }

    class StreamLink : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val url =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getStreamURL")
            val connect = url.openConnection() as HttpURLConnection
            connect.requestMethod = "GET"
            connect.connect()

            val streamLink = BufferedReader(InputStreamReader(connect.inputStream)).readText()
            connect.disconnect()
            return streamLink
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }
    }

    private fun setBackground(song: String, bg: ImageView) {
        if (song != "VLR - Live!") {
            runOnUiThread {
                setBg(bg)
            }
        } else {
            runOnUiThread {
                setLiveBg(bg)
            }
        }
    }

    private fun setLiveBg(bg: ImageView) {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        if (bg.drawable.constantState != ContextCompat.getDrawable(
                this,
                R.drawable.livebg
            )?.constantState
        ) {
            bg.startAnimation(fadeOut)
            Handler().postDelayed({
                bg.setImageResource(R.drawable.livebg)
                bg.startAnimation(fadeIn)
                bg.visibility = View.VISIBLE
            }, 250)
        }
    }

    private fun setBg(bg: ImageView) {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        if (bg.drawable.constantState != ContextCompat.getDrawable(
                this,
                R.drawable.bg
            )?.constantState
        ) {
            bg.startAnimation(fadeOut)
            Handler().postDelayed({
                bg.setImageResource(R.drawable.bg)
                bg.startAnimation(fadeIn)
                bg.visibility = View.VISIBLE
            }, 250)
        }

        if (bg.visibility == View.INVISIBLE) {
            bg.startAnimation(fadeIn)
            bg.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Player().mp.stop()
        Player().mp.release()
        Timer().purge()
        Timer().cancel()
        stopService(MusicService.newIntent(this))
    }
}

