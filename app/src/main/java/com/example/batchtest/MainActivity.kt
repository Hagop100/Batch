package com.example.batchtest

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.batchtest.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase


private const val TAG = "NewUserLog"
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // bind and inflate the main activity layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // find the nav host fragment from main activity layout
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navigationFragmentContainer.id) as NavHostFragment
        // create nav controller instance on nav host fragment to navigate between fragments
        val navController: NavController = navHostFragment.navController
        // find bottom navigation bar from main activity layout
        val navView: BottomNavigationView = findViewById(binding.navBar.id)
        // change visibility of bottom nav bar depending on which fragment is displayed
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if((destination.id == R.id.loginFragment) || (destination.id == R.id.groupCreationFragment) || (destination.id == R.id.registrationFragment) || (destination.id == R.id.accountSettingFragment) ||
                    destination.id == R.id.initialProfilePersonalizationFragment || (destination.id == R.id.editProfileFragment) || (destination.id == R.id.viewGroupInfoFragment) ||
                    destination.id == R.id.editGroupProfile ||  destination.id == R.id.joinGroupFragment || destination.id == R.id.groupChatFragment || (destination.id == R.id.viewUserInfoFragment) ||
                    (destination.id == R.id.preferencesFragment) || (destination.id == R.id.accountSettingFragment) || (destination.id == R.id.userGuideFragment)) {
                binding.navBar.visibility = View.GONE
            } else {
                binding.navBar.visibility = View.VISIBLE
            }
        }
        // connect bottom navigation bar with nav controller to navigate
        navView.setupWithNavController(navController)
    }
}