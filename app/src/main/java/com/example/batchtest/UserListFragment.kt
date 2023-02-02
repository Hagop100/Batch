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

private const val TAG = "NewUserLog";
class UserListFragment : Fragment() {
    // check if user list fragment is visible
    private var _binding: FragmentUserListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate and bind user list fragment
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        // use recycler view to display users
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
        // array list of users from firebase
        val users = arrayListOf<User>();
        //  initialize firebase database
        val db = Firebase.firestore;
        // get all users and send to user adapter
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
}