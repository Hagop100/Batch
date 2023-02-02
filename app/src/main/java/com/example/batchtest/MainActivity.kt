package com.example.batchtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.batchtest.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//Hagop has made a change!sdf
//Eman made this
private const val TAG = "NewUserLog"
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navigationFragmentContainer.id) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(binding.navBar.id)
        navView.setupWithNavController(navController)


//        val db = Firebase.firestore;
//
//        // create new user
//        val user = hashMapOf(
//            "name" to "Steven",
//        );
//
//        db.collection("users")
//            .add(user)
//            .addOnSuccessListener { docRef ->
//                Log.d(TAG, "document snapshot added with id: ${docRef.id}");
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "error adding document", e);
//            }
//
//        db.collection("users")
//            .get()
//            .addOnSuccessListener { result ->
//                for (doc in result) {
//                    Log.d(TAG, "${doc.id} => ${doc.data}")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "error getting documents: ", e)
//            }
    }
}