package com.example.batchtest.EditGroupProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.example.batchtest.databinding.FragmentViewGroupInfoBinding
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewGroupInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewGroupInfoFragment : Fragment() {
    private var _binding: FragmentViewGroupInfoBinding? = null
    private val binding get() = _binding!!
    var db = Firebase.firestore
    private lateinit var myAdapter: MyGroupAdapter

    private val args: ViewGroupInfoFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
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

            //retrieve info of interest tags
            val interestTags: ArrayList<*> = document.get("interestTags") as ArrayList<*>

            //get the interest tags layout

            val flexboxLayout: FlexboxLayout = binding.interestTags
            for (tag in interestTags){
                val textView = TextView(activity) //create new TextView
                textView.text = tag as String? //set the text of the TextView to current tag
                textView.layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT, //set the width
                    FlexboxLayout.LayoutParams.WRAP_CONTENT, //set the height
                )

                textView.background = this.context?.let { ContextCompat.getDrawable(it,R.drawable.round_corner ) }

                flexboxLayout.addView(textView) //add the TextView to Flexbox

            }




        }


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
            editProfileDialogBtn.text = "Edit Group Profile"
            // perform action on click

            editProfileDialogBtn.setOnClickListener {
                // navigate to the edit page
                findNavController().navigate(R.id.action_viewGroupInfoFragment_to_editGroupProfile)
                dialog.dismiss()
            }

            // add the block dialog button to the bottom dialog view
            view.addView(editProfileDialogBtn)

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewGroupInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewGroupInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}