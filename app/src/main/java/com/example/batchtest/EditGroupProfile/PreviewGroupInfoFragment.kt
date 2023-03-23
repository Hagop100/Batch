package com.example.batchtest.EditGroupProfile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentPreviewGroupInfoBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PreviewGroupInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreviewGroupInfoFragment : Fragment() {

    private var _binding: FragmentPreviewGroupInfoBinding? = null
    private val binding get() = _binding!!
    var db = Firebase.firestore
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()

    override fun onCreate( savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("print", "preview")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPreviewGroupInfoBinding.inflate(layoutInflater, container, false)



        //get group name
        binding.groupName.text = sharedViewModel.getGName().value
        val groupName = binding.groupName.text

        //get about us
        binding.aboutUsDescription.text = sharedViewModel.getGDesc().value

        //get group profile
        if (sharedViewModel.getGroupPicture().value.isNullOrEmpty()){
            binding.groupPicture.setImageResource(R.drawable.placeholder)
        }
        else{
            Glide.with(this).load(sharedViewModel.getGroupPicture().value).into(binding.groupPicture)
        }



        //observe the changes in tag arraylist from edit
        //get the updated arraylist of tags
        sharedViewModel.groupTags.observe(viewLifecycleOwner, Observer<ArrayList<String>>{ newTags: ArrayList<String> ->
            val flexboxLayout: FlexboxLayout = binding.interestTags

            //observer append to the view. must remove the previous view to display new arraylist view
            flexboxLayout.removeAllViews()
            for (tag in newTags) {
                //inflate the interest_tags.xml layout
                val textView =
                    inflater.inflate(R.layout.interest_tag, flexboxLayout, false) as TextView
                textView.text = tag as String? //set the text of the TextView to current tag
                flexboxLayout.addView(textView) //add the TextView to Flexbox
            }
        })


        return binding.root
    }

    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

