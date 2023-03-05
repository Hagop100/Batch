package com.example.batchtest.EditGroupProfile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
import com.google.android.material.chip.Chip
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

    /**
     * initialize the values of Group class when the app is starting up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      initialize the values of Group class
        group = Group(
            name = "",
            users = ArrayList<User>(),
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

        /**
         * display text change as user edit group name
         */
//        binding.editGroupName.addTextChangedListener(object: TextWatcher{
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            //text is changing while user enters the group name
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//                //if user deleted the entire sequence of text. alert user to enter at least 1 character.
//                if (s.isNullOrEmpty()){
//                    binding.editGroupName.error = "must enter at least 1 character"
//                }
//                binding.groupName.text = s
//            }
//
//        })

        /**
         * get info from database
         */
//        val db = Firebase.firestore
//        val gName = binding.groupName.text.toString()
//        db.collection("groups").whereEqualTo("name", gName).get()
//            .addOnSuccessListener { document ->
//            if (document != null){
//
//
//            }
//        }


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

