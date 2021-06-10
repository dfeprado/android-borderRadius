package dev.danielprado.borderradius

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.danielprado.borderradius.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit private var layoutBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)

        layoutBinding.button.setOnClickListener {
            layoutBinding.boxBorderRadius.borderRadius = layoutBinding.borderRadiusInput.text.toString()
        }
    }
}