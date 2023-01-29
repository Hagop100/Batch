package com.example.batchtest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentMyGroupsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "InputFragment";
class InputFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentMyGroupsBinding? = null;
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        };
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyGroupsBinding.inflate(inflater, container, false)
//        Log.d(TAG, "${binding.submitBtn}");
//        binding.submitBtn.setOnClickListener {
//            val db = Firebase.firestore;
//
//            // create new user
//            val user = User(binding.name.text.toString())
//
//            db.collection("users")
//                .add(user)
//                .addOnSuccessListener { docRef ->
//                    findNavController().navigate(R.id.show_user_list_fragment);
//                }
//                .addOnFailureListener { e ->
//                    Log.w(TAG, "error adding document", e);
//                }
//        }

//        test for Group creation intent
        binding.button2.setOnClickListener{
            val intent = Intent(this.context, GroupCreationFragment::class.java)
            startActivity(intent)

        }
        // Inflate the layout for this fragment
        return binding.root


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InputFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InputFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}