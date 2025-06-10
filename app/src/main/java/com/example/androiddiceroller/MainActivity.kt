package com.example.androiddiceroller // Make sure this is your actual package name

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androiddiceroller.databinding.ActivityMainBinding // Import your ViewBinding class

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding
        setContentView(binding.root)

        // Set an OnClickListener for the button
        // The button's ID from the XML is go_to_dice_roller_button,
        // ViewBinding converts this to goToDiceRollerButton (camelCase)
        binding.goToDiceRollerButton?.setOnClickListener {
            // Create an Intent to start DiceRollerApp activity
            // Replace DiceRollerApp::class.java if your dice rolling activity has a different name
            val intent = Intent(this, DiceRollerApp::class.java)
            startActivity(intent)
        }
    }
}