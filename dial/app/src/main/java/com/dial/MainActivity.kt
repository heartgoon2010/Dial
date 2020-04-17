package com.dial

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.dial.models.PreferenceHelper
import com.dial.presenters.DialHandler
import io.reactivex.plugins.RxJavaPlugins

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceHelper.init(this)
//        RxJavaPlugins.setErrorHandler { e: Throwable? ->
//        }
        findViewById<Button>(R.id.start_dial_button).setOnClickListener {
            DialHandler(this).start()
        }
    }
}
