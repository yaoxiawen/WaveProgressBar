package com.example.waveprogressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val wave = findViewById<WaveProgressBar>(R.id.wave)
        var curr = 0
        findViewById<TextView>(R.id.minus).setOnClickListener {
            wave.setProgress(--curr)
        }
        findViewById<TextView>(R.id.plus).setOnClickListener {
            wave.setProgress(++curr)
        }
    }
}