package com.example.batchtest.myGroupsTab

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentPreferencesBinding
import com.google.android.gms.location.*


/**
 * A simple [Fragment] subclass.
 * Use the [PreferencesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreferencesFragment : Fragment() {

    companion object {
        const val LATITUDE:String = "latitude"
        const val LONGITUDE: String = "longitude"
        const val SEARCHDISTANCE: String = "distance"
    }

    //View Model
    private lateinit var viewModel: PreferencesViewModel
    private lateinit var groupId: String

    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!

    //Location Variables including fused variable
//    private val locationHashMap = HashMap<String, Any>()
//    private var minimumAge: Int = 18
//    private var maxAge: Int = 100
//    private var latitude: Double = 0.0
//    private var longitude: Double = 0.0
//    private var distance: Int = 0

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
        //initialize modelview - might not use
        viewModel = ViewModelProvider(this).get(PreferencesViewModel::class.java)

        //get groupId from ViewGroupInfoFragment
        val args = PreferencesFragmentArgs.fromBundle(requireArguments())
        viewModel.groupId = args.groupId
        //Initialize the Fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //HashMap with user location attributes
        viewModel.locationHashMap[LATITUDE] = 0.0
        viewModel.locationHashMap[LONGITUDE] = 0.0
        viewModel.locationHashMap[SEARCHDISTANCE] = 0

        //TODO button listener for btn_to_user_profile_tab
        //Button returns user to their ViewGroupInfo page without saving info
        binding.btnToUserProfileTab.setOnClickListener {

            findNavController().navigate(R.id.action_preferencesFragment_to_viewGroupInfoFragment)
        }

        //TODO GET GROUP AGE RANGE
        //Age range slider that determines the minimum and max ages of group members the other group can have
        binding.rsAge.addOnChangeListener { slider, value, fromUser ->
            binding.tvAgeSelected.text = "${slider.values[0].toInt()} - ${slider.values[1].toInt()}"
            viewModel.minimumAge = slider.values[0].toInt()
            viewModel.maxAge = slider.values[1].toInt()
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
        //TODO DISPLAY LOCATION CITY or ZIP

        //TODO get Max distance from current location

        //TODO implement tags, for deal breakers

        //TODO set Gender of group to discover

        //TODO save changes
        //TODO send changes to firestore.
    }

    /**Check if location services are enabled
     * Such as GPS or Network Provider*/
    //TODO check that it works
    private fun isLocationServicesEnabled(): Boolean {
        Toast.makeText(context, "in location service", Toast.LENGTH_SHORT)
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE)!! as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //object callback used in the requestLocationUpdate
    private val locationCallBack = object : LocationCallback()
    {
        override fun onLocationResult(result: LocationResult) {
            val lastLocation: Location? = result.lastLocation
            viewModel.latitude = lastLocation!!.latitude
            viewModel.longitude = lastLocation.longitude
            binding.tvLocation.text = viewModel.longitude.toString()
        }
    }

    /** Function used to launch fused location client
     * Suppress Requirement to check for permission prior to using RequestLocationUpdates
     * */
    @SuppressLint("MissingPermission")
    private fun requestLocationData(){

        //must create location request first
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 1000
        locationRequest.numUpdates = 1

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
    }

    /**Make sure location permission are enabled,
     * Otherwise Request Coarse and Fine Location permissions */
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

}