package com.example.batchtest.DiscoveryPreferences

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentPreferencesBinding
import com.example.batchtest.myGroupsTab.GetAddressFromLatLng
import com.example.batchtest.DiscoveryPreferences.PreferencesFragmentArgs
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [PreferencesFragment.newInstance] factory method to
 * create an instance of this fragment.*/
private const val TAG = "PreferencesFragment"
class PreferencesFragment : Fragment() {

    companion object
    {
        const val LATITUDE: String = "latitude"
        const val LONGITUDE: String = "longitude"
        const val MIN_AGE: String = "minimumAge"
        const val MAX_AGE: String = "maxAge"
        const val MAX_DISTANCE: String = "maxDistance"
        const val GENDER: String = "gender"
        const val CITY: String = "city"
        const val PREFERENCES: String = "preferences"
    }

    //View Model
    private lateinit var viewModel: PreferencesViewModel
    private lateinit var viewModelFactory: PreferencesViewModelFactory


    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseFirestore.getInstance()

    //used for location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //Variables to used to launch the permission requests
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {permissions->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted)
                {
                    Toast.makeText(context,"$permissionName is Granted", Toast.LENGTH_SHORT).show()
                    if(permissionName == Manifest.permission.ACCESS_FINE_LOCATION)
                    {
                        requestLocationData()
                    }
                    else if(permissionName == Manifest.permission.ACCESS_COARSE_LOCATION)
                    {
                        requestLocationData()
                    }
                }
                else{
                    Toast.makeText(context, "$permissionName is Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //Initialize the Fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)

        //initialize factory to pass in group name necessary to get Group from database
        viewModelFactory = PreferencesViewModelFactory(PreferencesFragmentArgs.fromBundle(requireArguments()).groupId)
        //initialize modelview - using a viewModelFactory to pass in the group Name
        viewModel = ViewModelProvider(this, viewModelFactory).get(PreferencesViewModel::class.java)

        //Observe the minimum age, max age, distance and update the viewModel hashMap in case the user saves the preferences
        viewModel.minimumAge.observe(viewLifecycleOwner, Observer { newMinAge->
            viewModel.preferencesHash[MIN_AGE] = newMinAge
        })
        viewModel.maxAge.observe(viewLifecycleOwner, Observer { newMaxAge->
            viewModel.preferencesHash[MAX_AGE] = newMaxAge
        })
        viewModel.distance.observe(viewLifecycleOwner, Observer { newDistance->
            viewModel.preferencesHash[MAX_DISTANCE] = newDistance
        })

        viewModel.gender.observe(viewLifecycleOwner, Observer { gen ->
            viewModel.preferencesHash[GENDER] = gen
        })

        viewModel.city.observe(viewLifecycleOwner, Observer { city->
            viewModel.preferencesHash[CITY] = city
        })

        viewModel.latitude.observe(viewLifecycleOwner, Observer { lat ->
            viewModel.preferencesHash[LATITUDE] = lat
        })
        viewModel.longitude.observe(viewLifecycleOwner, Observer { long ->
            viewModel.preferencesHash[LONGITUDE] = long
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getGroupDatabase()


        //TODO button listener for btn_to_user_profile_tab
        //Button returns user to their ViewGroupInfo page without saving info
        binding.btnToUserProfileTab.setOnClickListener {
            findNavController().navigate(R.id.action_preferencesFragment_to_viewGroupInfoFragment)
        }

        //Age range slider that determines the minimum and max ages of group members the other group can have
        binding.rsAge.addOnChangeListener { slider, value, fromUser ->
            binding.tvAgeSelected.text = "${slider.values[0].toInt()} - ${slider.values[1].toInt()}"
            viewModel.minimumAge.value = slider.values[0].toDouble()
            viewModel.maxAge.value = slider.values[1].toDouble()
        }

        binding.rsDistance.addOnChangeListener { slider, value, fromUser ->
            binding.tvDistance.text = "${value.toInt()} Miles"
            viewModel.distance.value = value.toDouble()
        }



        //Button that gets the user location
        binding.btnCurrentLocation.setOnClickListener {
            //Check that the phone has a GPS or Network Connection
            //If there is no location service, sends user to Settings to activate location services
            if(!isLocationServicesEnabled())
            {
                Toast.makeText(context,
                    "Your Location provider is turned off\nPlease Turn it on to use location",
                    Toast.LENGTH_LONG).show()

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            //Location services are on
            else
            {
                //launches permissions if they have not been enabled
                //otherwise gets the user location
                launchUserLocation()
            }

        }



        binding.btnSavePreferences.setOnClickListener { view ->
            setGender()
            saveToDataBase()
        }

    }

    /**
     *
     * */
    private fun updateGroupViews()
    {
        binding.rsAge.setValues(viewModel.minimumAge.value?.toFloat(), viewModel.maxAge.value?.toFloat() )
        //binding.rsAge.setValues(binding.rsAge.values[1],viewModel.maxAge.value)

        binding.tvLocation.text = viewModel.city.value as String
//        binding.rsDistance.values[1] = viewModel.distance.value
        var gen = when(viewModel.gender.value)
        {
            "male"   -> binding.rbMale.id
            "female" -> binding.rbFemale.id
            else     -> binding.rbAll.id
        }
        binding.rsDistance.setValues(viewModel.distance.value?.toFloat())
        binding.rgGender.check(gen)


    }
/**Check if location services are enabled
     * Such as GPS or Network Provider*/

    //TODO check that it works
    private fun isLocationServicesEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE)!! as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //object callback used in the requestLocationUpdate
    //Result of getting the location will be placed here
    private val locationCallBack = object : LocationCallback()
    {
        override fun onLocationResult(result: LocationResult) {
            val lastLocation: Location? = result.lastLocation
            viewModel.latitude.value = lastLocation!!.latitude
            viewModel.longitude.value  = lastLocation.longitude
            viewModel.preferencesHash[LATITUDE] = viewModel.latitude.value!!
            viewModel.preferencesHash[LONGITUDE] = viewModel.longitude.value!!

            //Coroutine to get the city location
            viewLifecycleOwner.lifecycleScope.launch {
                val addressGeocoder = GetAddressFromLatLng(requireContext(), viewModel.latitude.value!!, viewModel.longitude.value!!)
                viewModel.city.value = addressGeocoder.getAddress()
                binding.tvLocation.text = addressGeocoder.getAddress()
            }

        }
    }

/** Function used to launch fused location client
     * Suppress Requirement to check for permission prior to using RequestLocationUpdates
     **/

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        viewLifecycleOwner.lifecycleScope.launch{
            //must create location request first
            var locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            locationRequest.interval = 1000
            locationRequest.numUpdates = 1

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
        }

    }

/**Function sets the city name in the location text after calling the geo
*Make sure location permission are enabled,
     * Otherwise Request Coarse and Fine Location permissions*/

    private fun launchUserLocation()
    {

        //Check if Permission have already been granted
        if(ContextCompat.checkSelfPermission(requireView().context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireView().context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "permissions found", Toast.LENGTH_SHORT)
            requestLocationData()
        }
        else //Request for Permissions
        {
            //Launch the permission launcher
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
        }

    }

    private fun getGroupDatabase()
    {
        database.collection("groups").document(viewModel.gName as String).get().addOnSuccessListener { document->


            viewModel.group = document!!.toObject(Group::class.java)!!
            Log.i("Preference", viewModel.group.preferences.toString())

            if (viewModel.group.preferences == null)
            {
                viewModel.minimumAge.value = 18.0
                viewModel.maxAge.value = 100.0
                viewModel.distance.value = 0.0
                viewModel.longitude.value = 0.0
                viewModel.latitude.value = 0.0
                viewModel.gender.value = "all"
                viewModel.city.value = "Location"
            }
            else
            {
                Log.i(TAG,viewModel.group.preferences?.get(MIN_AGE).toString() )
                viewModel.city.value = viewModel.group.preferences?.get(CITY) as String
                viewModel.gender.value = viewModel.group.preferences?.get(GENDER) as String
                viewModel.latitude.value = viewModel.group.preferences?.get(LATITUDE) as Double
                viewModel.longitude.value  = viewModel.group.preferences?.get(LONGITUDE) as Double


                viewModel.minimumAge.value =  viewModel.group.preferences?.get(MIN_AGE) as Double
                viewModel.maxAge.value = viewModel.group.preferences?.get(MAX_AGE) as Double
                viewModel.distance.value = viewModel.group.preferences?.get(MAX_DISTANCE) as Double

            }




            updateGroupViews()

        }. addOnFailureListener {
            Log.i("print", "error getting user from documents: ", it)
        }
    }



    private fun setGender() {
        viewModel.gender.value = when (binding.rgGender.checkedRadioButtonId) {
            binding.rbMale.id -> "male"
            binding.rbFemale.id -> "female"
            else -> "All"
        }
    }

    /**Save any changes to the database*/
    //TODO check if user just deleted previous tags
    private fun saveToDataBase()
    {
        if (viewModel.dBreakers.isNotEmpty())
        {
            database.collection("groups").document(viewModel.gName).update("dealBreakers", viewModel.dBreakers)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(context,"Failed To Update Group Preferences", Toast.LENGTH_SHORT).show()
                }
        }
        //update preferences to the database
        database.collection("groups")
            .document(viewModel.gName)
            .update("preferences", viewModel.preferencesHash).addOnSuccessListener {
                findNavController().navigate(R.id.action_preferencesFragment_to_viewGroupInfoFragment)
            }.addOnFailureListener {
                Toast.makeText(context,"Failed To Update Group Preferences", Toast.LENGTH_SHORT).show()
            }

    }

}




