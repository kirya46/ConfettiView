package com.example.kirillstoianov.confettiview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hily.app.presentation.ui.views.widgets.ConfettiView
import kotlinx.android.synthetic.main.activity_main.*
import nl.dionsegijn.konfetti.emitters.Emitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val particlesView = ParticlesView(this@MainActivity)
//        container.addView(particlesView)

        val confettiView = ConfettiView(this)
        container.addView(confettiView)
        confettiView.post { confettiView.startAnimate() }


//        viewKonfetti.build()
//            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
//            .setDirection(0.0, 359.0)
//            .setSpeed(1f, 9f)
//            .setFadeOutEnabled(true)
//            .setTimeToLive(5000L)
//            .addShapes(Shape.RECT, Shape.CIRCLE)
//            .addSizes(Size(6),Size(12),Size(20))
//            .setPosition(screenWidthPx()/2f,screenHeightPx()/2f)
//            .burst(150)
    }


    internal fun Context.screenWidthPx() = resources.displayMetrics.widthPixels

    internal fun Context.screenHeightPx() = resources.displayMetrics.heightPixels
}
