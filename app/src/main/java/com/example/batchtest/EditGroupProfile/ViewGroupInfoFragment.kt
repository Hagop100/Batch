package com.example.batchtest.EditGroupProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.example.batchtest.databinding.FragmentViewGroupInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewGroupInfoBinding.inflate(layoutInflater, container, false)

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