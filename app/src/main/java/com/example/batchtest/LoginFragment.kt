package com.example.batchtest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class LoginFragment : Fragment() {

    //authentication variable
    private lateinit var auth: FirebaseAuth

    //PhoneAuthorization Variables
    private lateinit var credential: PhoneAuthCredential
    private var verificationId: String = ""
    private lateinit var forceResendingToken: PhoneAuthProvider.ForceResendingToken

    //binding variables
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //SharedPreferences
    private lateinit var myPrefs: SharedPreferences

    //email/password/phoneNumber
    private lateinit var multiFactorResolver: MultiFactorResolver
    private var user: FirebaseUser? = null
    private lateinit var email: String //email
    private lateinit var password: String //password
    private var smsCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
        myPrefs = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE) //sharedPreferences initialization
        user = auth.currentUser
        /*
        This block of code here waits for the smsCode to arrive to finish multi-factor authentication
        This should belong in the signIn(email, password) function but there were complications
        The issue was that the verificationID was null while the reCAPTCHA was taking its time to process
        This meant the application would crash
        I needed a way to have this fragment wait until the captcha was finished processing
        This listener waits until it receives the smsCode to execute, therefore the signIn will resolve here
         */
        setFragmentResultListener("SMS_Code_Key") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            smsCode = bundle.getString("SMS_Code_Value")
            Log.i(TAG, smsCode!!)

            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode!!)
            // Initialize a MultiFactorAssertion object with the
            // PhoneAuthCredential.
            val multiFactorAssertion: MultiFactorAssertion =
                PhoneMultiFactorGenerator.getAssertion(credential)
            // Complete sign-in.
            multiFactorResolver
                .resolveSignIn(multiFactorAssertion)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User successfully signed in with the
                        // second factor phone number.
                        findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
                    }
                }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        /*
        A debugging tool to check if the user is signed in or not upon running the application
        If a user is not explicitly signed out, then it is the case that the user could be
        logged in or out depending on the startup of the application
         */
        if (user != null) {
            Log.i(TAG, "user is signed in")
            Log.i(TAG, user!!.email.toString())
        } else {
            Log.i(TAG, "user is signed out")
        }

        //Sign Up navigates to registration fragment
        binding.fragmentLoginSignUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        /*
        This code block handles automatically loading the email/password without having to type it out again
        if you already selected the rememberMe checkBox.
        If the rememberMe checkBox was checked when logging in at an earlier time,
        then it is the case that we have some preferences saved (email/password).
        If we do have preferences saved, then we want to automatically input them into our
        EditText views. We will then check the rememberMe checkBox again.
         */
        if(myPrefs.getString(emailKey, null) != null && myPrefs.getString(passwordKey, null) != null) {
            binding.fragmentLoginUsernameEt.setText(myPrefs.getString(emailKey, null))
            binding.fragmentLoginPasswordEt.setText(myPrefs.getString(passwordKey, null))
            binding.fragmentLoginRememberMeBtn.isChecked = true
        }

        /*
        The login button will attempt to sign in the user via firebase
        It will read the username edit text and password edit text
        Then pass it into the signIn function
         */
        binding.fragmentLoginLoginBtn.setOnClickListener {
            //We store the email and password upon clicking the login button
            email = binding.fragmentLoginUsernameEt.text.toString()
            password = binding.fragmentLoginPasswordEt.text.toString()
            /*
            If we have the rememberMe checkBox checked when we press the login button,
            then store the email and password into our sharedPreferences object.
            If we have the rememberMe checkBox unchecked when we press the login button,
            then clear the sharePreferences object from any data we had stored in there.
             */
            if(binding.fragmentLoginRememberMeBtn.isChecked) {
                val editor: SharedPreferences.Editor = myPrefs.edit()
                editor.putString(emailKey, email)
                editor.putString(passwordKey, password)
                editor.apply()
            }
            else {
                myPrefs.edit().clear().apply()
            }

            //signIn function using firebase API
            //This is the real code
            signIn(email, password)

            //This is for testing for now
            //findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
        }

        /*
        This is the forgot Password button where it prompts you to enter an email with a dialog fragment
        Upon entering your email, you should receive an email about resetting your password
         */
        binding.fragmentLoginForgotPasswordBtn.setOnClickListener {
            val dialog = ForgotPasswordDialogFragment()
            dialog.show(childFragmentManager, "forgotPasswordDialog")
        }

        /*
        This button remains for debugging purposes and will be removed upon release of the final build
        The user is signed out upon clicking the sign out button
        If the user is signed in, then it will display that in the logcat(although this should never occurr)
        If the user is logged out, then it will display that in the logcat
         */
        binding.fragmentLoginSignOutBtn.setOnClickListener {
            auth.signOut()
            val user = auth.currentUser
            if (user != null) {
                Log.i(TAG, "user is signed in")
                Log.i(TAG, user.email.toString())
            } else {
                Log.i(TAG, "user is signed out")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    Function allows you to sign in with proper email and password authentication via Firebase
     */
    private fun signIn(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please Enter Email and Password.", Toast.LENGTH_SHORT).show()
        }
        else {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // User is not enrolled with a second factor and is successfully
                            // signed in.
                            // ...
                            findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
                            return@OnCompleteListener
                        }
                        if (task.exception is FirebaseAuthMultiFactorException) {
                            // The user is a multi-factor user. Second factor challenge is
                            // required.
                            multiFactorResolver = (task.exception as FirebaseAuthMultiFactorException).resolver
                            val selectedHint = multiFactorResolver.hints[0] as PhoneMultiFactorInfo
                            // Send the SMS verification code.
                            PhoneAuthProvider.verifyPhoneNumber(
                                PhoneAuthOptions.newBuilder()
                                    .setActivity(requireActivity())
                                    .setMultiFactorSession(multiFactorResolver.session)
                                    .setMultiFactorHint(selectedHint)
                                    .setCallbacks(generateCallBacks())
                                    .setTimeout(30L, TimeUnit.SECONDS)
                                    .build()
                            )
                            //navigate to smsCode fragment to get smsCode
                            findNavController().navigate(R.id.action_loginFragment_to_MFAuthenticationVerificationDialogFragment)
                        } else {
                            // Handle other errors, such as wrong password.
                            Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    })
        }

    }

    /*
    This function handles firebase multi-factor authentication
     */
    private fun generateCallBacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1) Instant verification. In some cases, the phone number can be
                    //    instantly verified without needing to send or enter a verification
                    //    code. You can disable this feature by calling
                    //    PhoneAuthOptions.builder#requireSmsValidation(true) when building
                    //    the options to pass to PhoneAuthProvider#verifyPhoneNumber().
                    // 2) Auto-retrieval. On some devices, Google Play services can
                    //    automatically detect the incoming verification SMS and perform
                    //    verification without user action.
                    this@LoginFragment.credential = credential
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in response to invalid requests for
                    // verification, like an incorrect phone number.
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        // ...
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        // ...
                    }
                    // Show a message and update the UI
                    // ...
                }

                override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    // The SMS verification code has been sent to the provided phone number.
                    // We now need to ask the user to enter the code and then construct a
                    // credential by combining the code with a verification ID.
                    // Save the verification ID and resending token for later use.
                    Log.i(LoginFragment.TAG, "onCodeSent:$verificationId")
                    this@LoginFragment.verificationId = verificationId
                    this@LoginFragment.forceResendingToken = forceResendingToken
                    // ...
                }
            }
        return callbacks
    }

    companion object {
        private const val TAG = "LoginFragment" //for logcat debugging
        private const val emailKey: String = "email" //key for emails in SharedPreferences
        private const val passwordKey: String = "password" //key for passwords in SharedPreferences
    }

}