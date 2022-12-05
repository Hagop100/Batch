package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.databinding.FragmentUserListBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "NewUserLog";
class UserListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentUserListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

//    private val userViewModel: UserViewModel by viewModels();
    private var job: Job? = null;

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                val users = arrayListOf<User>();
//                val db = Firebase.firestore;
//                db.collection("users")
//                    .get()
//                    .addOnSuccessListener { result ->
//                        for (doc in result) {
//                            val user:User? = doc.toObject(User::class.java);
//                            if (user != null) {
//                                users.add(user)
//                            }
//                            //Log.d(TAG, "$user")
//                        }
//                        binding.userRecyclerView.adapter = UserAdapter(users)
////                Log.d(TAG, "$users")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w(TAG, "error getting documents: ", e)
//                    }
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
        val users = arrayListOf<User>();
        val adapter = UserAdapter(users)
        val db = Firebase.firestore;
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val user:User? = doc.toObject(User::class.java);
                    if (user != null) {
                        users.add(user)
                    }
                }
                binding.userRecyclerView.adapter = UserAdapter(users)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "error getting documents: ", e)
            }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}