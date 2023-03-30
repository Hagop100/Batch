package com.example.batchtest.UserProfileTab

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.example.batchtest.databinding.FragmentUserGuideBinding

/**
 * A simple [Fragment] subclass.
 * Use the [UserGuideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserGuideFragment : Fragment() {
    private var _binding: FragmentUserGuideBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserGuideBinding.inflate(layoutInflater, container, false)
        dialog = Dialog(requireActivity())
//        val backButton = dialog.findViewById<Button>(R.id.btn_user_guide)
        /*
         * dialog for join a group
         */
        binding.joinAGroupLayout.setOnClickListener{
            dialog.setContentView(R.layout.join_group_guide)
            val backButton = dialog.findViewById<Button>(R.id.backto_guide_from_join_a_group)
            dialog.show()
            backButton.setOnClickListener{
                dialog.dismiss()
            }
        }

        /*
         * dialog for create a group
         */

        binding.createAGroupLayout.setOnClickListener{

            dialog.setContentView(R.layout.create_group_guide)
            val backButton = dialog.findViewById<Button>(R.id.backto_guide_from_create_a_group)
            dialog.show()
            backButton.setOnClickListener{
                dialog.dismiss()
            }
        }

        /*
         * dialog for set primary group
         */
        binding.setPrimaryLayout.setOnClickListener{

            dialog.setContentView(R.layout.set_primary_guide)
            val backButton = dialog.findViewById<Button>(R.id.backto_guide_from_set_primary)
            dialog.show()
            backButton.setOnClickListener{
                dialog.dismiss()
            }
        }

        /*
         * dialog for 'batching'
         */
        binding.batchingLayout.setOnClickListener{

            dialog.setContentView(R.layout.batching_guide)
            val backButton = dialog.findViewById<Button>(R.id.backto_guide_from_batching)
            dialog.show()
            backButton.setOnClickListener{
                dialog.dismiss()
            }
        }



        binding.btnToUserProfileTab.setOnClickListener{
            findNavController().navigate(R.id.action_userGuideFragment_to_userProfileTabFragment)
        }

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