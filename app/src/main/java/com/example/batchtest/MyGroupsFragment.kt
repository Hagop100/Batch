package com.example.batchtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.batchtest.databinding.FragmentMyGroupsBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyGroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "MyGroupsFragment";
class MyGroupsFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_my_groups, container, false)
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


        }




}

