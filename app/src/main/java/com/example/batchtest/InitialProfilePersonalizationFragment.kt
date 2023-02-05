package com.example.batchtest

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.batchtest.databinding.FragmentInitialProfilePersonalizationBinding
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [InitialProfilePersonalizationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InitialProfilePersonalizationFragment : Fragment() {

    private var _binding: FragmentInitialProfilePersonalizationBinding? = null
    private val binding get() = _binding!!

    private lateinit var displayName: String
    private lateinit var birthday: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInitialProfilePersonalizationBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBirthdayPicker.setOnClickListener{
            birthdayPicker()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Open a DatePicker Dialog that the user can use to select their birthdate
    private fun birthdayPicker(): Calendar
    {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this.requireContext(),
            DatePickerDialog.OnDateSetListener { view, yearSelected, monthSelected, daySelected ->
                //Create a String with mm/dd/yyyy format
                val dateSelected = "${monthSelected+1}/$daySelected/$yearSelected"
                //set the text of the birthdayPicker button
                binding.btnBirthdayPicker.text = dateSelected

                //Create a simple date format, format the date string
                val sdf = SimpleDateFormat("mm/dd/yyyy", Locale.US)
                birthday = sdf.parse(dateSelected) as Date

            }
            ,year,month,day)

        //TODO: Make sure the date entered is corresponds to the user being over 18
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
        return calendar
    }

    //TODO: Update User information in Database

}