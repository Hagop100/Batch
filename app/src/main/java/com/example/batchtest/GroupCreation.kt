package com.example.batchtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.batchtest.databinding.ActivityGroupCreationBinding

class GroupCreation : AppCompatActivity() {
    private lateinit var binding: ActivityGroupCreationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_group_creation)
        binding = ActivityGroupCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}