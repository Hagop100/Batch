package com.example.batchtest.EditGroupProfile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditGroupInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditGroupInfoFragment : Fragment() {
    private var _binding: FragmentEditGroupInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var group: Group
    private lateinit var tagsArray: ArrayList<String>
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()

    /**
     * initialize the values of Group class when the app is starting up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      initialize the values of Group class
        group = Group(
            name = "",
            users = ArrayList<String>(),
            interestTags = ArrayList(),
            aboutUsDescription = "",
            biscuits = 0,
            image = null
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditGroupInfoBinding.inflate(layoutInflater, container, false)

        val db = FirebaseFirestore.getInstance()

        /**
         * display by retrieve group info using sharedviewmodel
         */
        binding.groupName.text = sharedViewModel.getGName().value


        /**
         * user hits the add button to add tag to the list
         * Validating text fields if empty or not
         */
        binding.addTag.setOnClickListener{
            if (binding.editTextAddTag.text.isNotEmpty()) {
                addChip(binding.editTextAddTag.text.toString())
            }
            //validate tags if empty or not
            else if (binding.editTextAddTag.text.isEmpty()) {
                binding.editTextAddTag.error = "Missing Tag"
            }

        }

        return binding.root
    }

    /**
    Function: add individual tag to group profile
     */
    private fun addChip(text: String){
        val tags = group.interestTags
        val chip = Chip(this.context)
        chip.text = text


//    display the close button to remove tag from the list
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.purple_500)
        chip.setTextAppearance(R.style.page_text)


//    remove chip from the interest tag as user removes it from view
        chip.setOnCloseIconClickListener{
            binding.tagGroupChip.removeView(chip)
            tags?.remove(chip.text as String)
        }

//   add chip to the arraylist of interest tags
        binding.tagGroupChip.addView(chip)
        tags?.add(chip.text.toString())

    }

    companion object {

        private const val TAG = "print" //for logcat debugging
    }
}

