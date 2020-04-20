package com.dial

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.dial.models.PreferenceHelper
import com.dial.presenters.DialHandler

class MainActivity : FragmentActivity() {

    companion object {
        private const val TARGET_APP = "com.tubitv.ott"
        private const val TARGET_APP_FOR_ROKU = "41468"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceHelper.init(this)
//        RxJavaPlugins.setErrorHandler { e: Throwable? ->
//        }
        findViewById<Button>(R.id.start_dial_button).setOnClickListener {
            DialHandler(this, TARGET_APP, TARGET_APP_FOR_ROKU).start()
        }
    }
}
