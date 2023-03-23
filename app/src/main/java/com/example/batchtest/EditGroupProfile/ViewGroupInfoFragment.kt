package com.example.batchtest.EditGroupProfile

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.example.batchtest.databinding.FragmentViewGroupInfoBinding
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import com.example.batchtest.myGroupsTab.MyGroupFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [ViewGroupInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewGroupInfoFragment : Fragment() {
    private var _binding: FragmentViewGroupInfoBinding? = null
    private val binding get() = _binding!!
    var db = Firebase.firestore
    private lateinit var userRecyclerView: RecyclerView
    private val args: ViewGroupInfoFragmentArgs by navArgs()
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewGroupInfoBinding.inflate(layoutInflater, container, false)

        /**
         * Retrieve data from MyGroupFragment using arguments
         * display the user group information including:
         * group name, profile picture, tags, and about us
         */
        binding.groupName.text = args.groupName
        binding.aboutUsDescription.text = args.groupDesc

        val groupName = binding.groupName.text

        //get info from the group collection in firebase
        db.collection("groups").document(groupName as String).get().addOnSuccessListener { document ->

            Log.i("print", "view group info here")
            //set info about group pic
            val groupPic = document.getString("image")
            if (groupPic.isNullOrEmpty()){
                binding.groupPicture.setImageResource(R.drawable.placeholder)
            }
            else{
                Glide.with(this).load(document.getString("image").toString()).into(binding.groupPicture)

            }

            /**
             * retrieve info of interest tags
             */
            //retrieve arraylist of interest tags from the current group in firebase
            val interestTags: ArrayList<*> = document.get("interestTags") as ArrayList<*>
            //get the interest tags layout
            val flexboxLayout: FlexboxLayout = binding.interestTags
            for (tag in interestTags){
                //inflate the interest_tags.xml layout
                val textView = inflater.inflate(R.layout.interest_tag, flexboxLayout, false) as TextView
                textView.text = tag as String? //set the text of the TextView to current tag
                flexboxLayout.addView(textView) //add the TextView to Flexbox

            }

            /**
             * retrieve list of users in the group to show all the team members
             */
            //retrieve arraylist of users from the current group in firebase
            val users: ArrayList<*> = document.get("users") as ArrayList<*>

            //set recyclerview
            userRecyclerView = binding.userRecyclerView
            userRecyclerView.layoutManager = LinearLayoutManager(this.context)
            userRecyclerView.setHasFixedSize(true)
            // Initialize arraylist of all users
            userList = arrayListOf()

            // Loop through the user IDs in the list
            for (userId in users){
                //access the user document based on the list
                db.collection("users").document(userId as String).get().addOnSuccessListener { document ->
                    //add the user into the userList
                    val userInfo: User? = document.toObject(User::class.java)
                    userList.add(userInfo!!)
                    userAdapter = UserInfoAdapter(requireActivity(),userList)
                    userRecyclerView.adapter = userAdapter
                }
                    .addOnFailureListener { e->
                        Log.i("print", "error getting user from documents: ", e)
                    }

            }

        }//end of firebase collection retrieve


        // more button on match page opens dialog
        binding.groupProfileMoreBtn.setOnClickListener {
            // create a bottom sheet dialog
            val dialog = BottomSheetDialog(requireContext())
            // inflate the view with the dialog linear layout
            val view: LinearLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout

            // create block dialog button to add into the dialog layout dynamically
            // using the dialog button layout
            // inflate a text view to hold the edit profile dialog
            val editProfileDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            editProfileDialogBtn.text = "Edit group profile"
            // perform action on click

            editProfileDialogBtn.setOnClickListener {
                // navigate to the edit page
                findNavController().navigate(R.id.action_viewGroupInfoFragment_to_editGroupProfile)
                //send data using shared view model
                sendData()
                dialog.dismiss()
            }

            // inflate a text view to hold the edit profile dialog
            val groupInviteBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            groupInviteBtn.text = "Invite a user"

            // perform action on click
            groupInviteBtn.setOnClickListener {
                // fetch group ID from firebase using groupName
                db.collection("groups").document(groupName)
                    .get()
                    .addOnSuccessListener { result ->
                        // start an intent to send group id using different actions
                        // such as copying to clipboard, email, messaging
                        activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, result.data?.get("groupId").toString())
                            type = "text/plain"
                        }
                        // user chooses which method to share
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        // starts intent
                        activity?.startActivity(shareIntent)
                    }
            }
            // add the edit profile dialog button to the bottom dialog view
            view.addView(editProfileDialogBtn)
            // add the group invite dialog button to the bottom dialog view
            view.addView(groupInviteBtn)

            // set the view of the dialog using the inflated layout
            dialog.setContentView(view)
            // show the dialog
            dialog.show()
        }

        //Exit to navigate back to the my groups page
        binding.exitViewBtn.setOnClickListener{
            findNavController().navigate(R.id.action_viewGroupInfoFragment_to_myGroupFragment)
        }
        return binding.root
    }

    /**
     * send data through shared view model so other fragments can receive the data
     */
    private fun sendData() {
        sharedViewModel.groupName.value = args.groupName
        sharedViewModel.groupDesc.value = args.groupDesc
    }

    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



