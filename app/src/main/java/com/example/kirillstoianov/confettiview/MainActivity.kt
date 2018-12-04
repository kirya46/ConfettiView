package com.example.kirillstoianov.confettiview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hily.app.presentation.ui.views.widgets.ConfettiView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val particlesView = ParticlesView(this@MainActivity)
//        container.addView(particlesView)

        val confettiView = ConfettiView(this)
        container.addView(confettiView)
        confettiView.post { confettiView.startAnimate() }
    }
}
