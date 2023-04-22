package com.example.batchtest.MatchTab

import android.app.Dialog
import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isNotEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.MatchGroupCardBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext


private const val TAG = "CardStackAdapter"

// CardStackAdapter.kt
//  the adapter receives a list of groups from the parent fragment and sends to the holder.
//  the holder will bind the views that will be recycled and change the text according to
//  group being displayed. the fragment uses the adapter to display data in a recycler view
//
// returns a recycler view of cards for each group
// @params groups   list of potential groups to be matched

// keep state of undo button
class CardStackAdapter(
    userId: String,
    private val context: Context?,
    private val groups: ArrayList<Group>,
    private val listener: CardStackAdapterListener
    ) : RecyclerView.Adapter<CardStackAdapter.CardStackHolder>() {
        // initialize firestore to access database
        private val db = Firebase.firestore
        private lateinit var binding: MatchGroupCardBinding
        private val currentUser = userId
        // document reference in firebase of current user
        private val currentUserDocRef = db.collection("users").document(userId)
        // inflate parent fragment with card item layout when ViewHolder is created
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackHolder {
            // inflater from parent fragment
            val inflater = LayoutInflater.from(parent.context)

            // inflate parent using group card layout
            binding = MatchGroupCardBinding.inflate(inflater, parent, false)



            // more button on match page opens dialog
//            binding.matchMoreBtn.setOnClickListener {
//                // create a bottom sheet dialog
//                val dialog = BottomSheetDialog(parent.context)
//
//                // inflate the view with the dialog linear layout
//                val view:LinearLayout = LayoutInflater.from(parent.context).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout
//
//                // create block dialog button to add into the dialog layout dynamically
//                // using the dialog button layout
//                // inflate a text view to hold the block dialog
//                val blockDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
//                blockDialogBtn.text = context?.getString(R.string.block_group)
//                // perform action on click
//                blockDialogBtn.setOnClickListener {
//                    dialog.dismiss()
//                }
//                // add the block dialog button to the bottom dialog view
//                view.addView(blockDialogBtn)
//
//                // inflate a text view to hold the block dialog
//                val reportDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
//                reportDialogBtn.text = context?.getString(R.string.report_group)
//                // perform action on click
//                reportDialogBtn.setOnClickListener {
//                    // add block group logic
//                    dialog.dismiss()
//                }
//                // add the report dialog button to the bottom dialog view
//                view.addView(reportDialogBtn)
//
//                // set the view of the dialog using the inflated layout
//                dialog.setContentView(view)
//                // show the dialog
//                dialog.show()
//            }
            // pass into a holder to bind
            return CardStackHolder(binding)
        }

        // set group information to be displayed using holder once bound
        override fun onBindViewHolder(holder: CardStackHolder, position: Int) {
            // create recycle layout for group
            val group = groups[position]

            //Moved matchMoreBtn listener to onBindViewHolder to get the access to the group name
            binding.matchMoreBtn.setOnClickListener {
                // create a bottom sheet dialog
                val dialog = BottomSheetDialog(context!!)

                // inflate the view with the dialog linear layout
                val view:LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout

                // create block dialog button to add into the dialog layout dynamically
                // using the dialog button layout
                // inflate a text view to hold the block dialog
                val blockDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                blockDialogBtn.text = context?.getString(R.string.block_group)

                // on Block button clicked, new Bottom sheet dialog will slide up informing user of action results
                blockDialogBtn.setOnClickListener { block ->
                    dialog.dismiss()
                    //create new bottomSheet Dialog
                    val blockDialog = Dialog(context,R.style.pop_up_dialog_corners)
                    //inflate dialog with layout
                    val blockView = LayoutInflater.from(view.context).inflate(R.layout.block_bottom_view,binding.root,false)
                    //get string to inform user of results
                    val blockText = context.getString(R.string.block_group_warning, group.name)
                    //set up text values withing the bottom dialog
                    blockView.findViewById<TextView>(R.id.tv_question_verify).text = blockText


                    //get access to buttons inside the blockView layout
                    val btnYes = blockView.findViewById<Button>(R.id.btn_ok)
                    val btnCancel = blockView.findViewById<Button>(R.id.btn_cancel)
                    blockDialog.setContentView(blockView)
                    blockDialog.show()

                    btnYes.setOnClickListener {
                        //update user and all user's in the primary group by removing group from future discovery
                        blockDialog.dismiss()
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = currentUserDocRef.get().await().toObject(User::class.java)
                            if (user?.primaryGroup != null)
                            {
                                blockGroupGroupUpdate(user.primaryGroup, group.name!!)
                            }
                            else
                            {
                                blockDialog.dismiss()
                                val notLeaderDialog = Dialog(view.context, R.style.pop_up_dialog_corners)
                                val leaderLayout = LayoutInflater.from(view.context).inflate(R.layout.not_leader_dialog,binding.root,false)
                                val okButton = leaderLayout.findViewById<Button>(R.id.btn_okLeader)
                                notLeaderDialog.setContentView(leaderLayout)
                                notLeaderDialog.show()
                                okButton.setOnClickListener{
                                    notLeaderDialog.dismiss()
                                }
                            }

                        }

                    }
                    btnCancel.setOnClickListener {
                        blockDialog.dismiss()
                    }
                    Log.i(TAG, "${group.name}")

                }
                // add the block dialog button to the bottom dialog view
                view.addView(blockDialogBtn)

                // inflate a text view to hold the block dialog
                val reportDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                reportDialogBtn.text = context?.getString(R.string.report_group)
                // perform action on click
                reportDialogBtn.setOnClickListener {
                    // add block group logic
                    dialog.dismiss()
                }
                // add the report dialog button to the bottom dialog view
                view.addView(reportDialogBtn)

                // set the view of the dialog using the inflated layout
                dialog.setContentView(view)
                // show the dialog
                dialog.show()
            }
            //Log.v(TAG, group.name.toString())
            // get interest tags of group
            val tags = group.interestTags
            // set group name
            holder.name.text = group.name
            // set biscuit value
            holder.biscuits.text = group.biscuits.toString()
            // set group description
            holder.description.text = group.aboutUsDescription
            // group image for group
            if (group.image.isNullOrEmpty()){
                holder.groupPicture.setImageResource(R.drawable.placeholder)
            } else {
                if (context != null) {
                    Glide.with(context)
                        .load(group.image)
                        .into(holder.groupPicture)
                }
            }
            // change ui of button dynamically based on undo state of user
            listener.observeUndoState(holder.undoBtn)

            // inflate the interest tag container
            val inflater: LayoutInflater = LayoutInflater.from(holder.interestTags.context)
            var interestTag: TextView
            // set the layout parameter and margins for interest tag
            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            params.setMargins(10, 10, 10, 10)
            if (holder.interestTags.isNotEmpty()) {
                holder.interestTags.removeAllViews()
            }
            // dynamically add group tags
            for (tag in tags!!) {
                // use existing interest tag layout
                interestTag = inflater.inflate(
                    R.layout.interest_tag,
                    holder.interestTags,
                    false
                ) as TextView
                // set text of tag
                interestTag.text = tag
                // set the defined layout parameters
                interestTag.layoutParams = params
                // add the interest tag text view to the layout
                holder.interestTags.addView(interestTag)
            }

            // undo on click listener
            binding.undoBtn.setOnClickListener {
                if (holder.undoBtn.alpha == 1F) {
                    // listener from MatchTabFragment listens when undo button is clicked and will call method
                    listener.onUndoBtnClick(position)
                }
            }

            // accept button accepts group when clicked
            binding.acceptBtn.setOnClickListener {
                // listener from MatchTabFragment listens when accept button is clicked and will call method
                listener.onAcceptBtnClick()
            }

            // reject button rejects group when clicked
            binding.rejectBtn.setOnClickListener {
                // listener from MatchTabFragment listens when reject button is clicked and will call method
                listener.onRejectBtnClick(group.name.toString())
            }
        }

        // returns number of groups
        override fun getItemCount(): Int {
            return groups.size
        }

        // holder class for each group card
        class CardStackHolder(val binding: MatchGroupCardBinding) : RecyclerView.ViewHolder(binding.root) {

            // get the views of card
            // name of group
            val name: TextView = binding.groupName

            // amount of biscuits
            val biscuits: TextView = binding.biscuitValue
            // description of group
            val description: TextView = binding.aboutUsDescription
            // interest tags of groups
            val interestTags: FlexboxLayout = binding.interestTags
            // undo button
            val undoBtn: ImageButton = binding.undoBtn
            // group picture
            val groupPicture: ImageView = binding.groupPicture
        }

    /**Function takes in the current user id and the groupName of the group they are blocking
     * User's Matched Group and Blocked groups fields are updated in the database*/
    private suspend fun blockGroupUserUpdate(currentUserId: String, groupName: String)
    {

        //get user
        val user = db.collection("users").document(currentUserId).get().await().toObject(User::class.java)
        //Initialize a blocked groups array
        var blocked = java.util.ArrayList<String>()
        //check that user was returned
        if(user != null)
        {
            //check if user already has a blocked groups list
            if(user.blockedGroups.isNotEmpty())
            {
                blocked = user.blockedGroups
                blocked.add(groupName)
            }
            else //Create a blocked groups list if not yet initialized
            {
                blocked.add(groupName)
            }
            //get user's matched groups list
            val matchedGroups = user.matchedGroups

            //remove group from matched groups
            matchedGroups.remove(groupName)
            //update user's matched and blocked groups
            //update user's matched and blocked groups
            var updateGroups = HashMap<String, Any>()
            updateGroups["matchedGroups"] = matchedGroups
            updateGroups["blockedGroups"] = blocked
            db.collection("users").document(currentUserId).update(updateGroups).await()
        }
    }
    /**Function will remove blocked group, from the primary groups matchedGroups field
     * Function will also iterate through all the group's members and call blockGroupUserUpdate*/
    private suspend fun  blockGroupGroupUpdate(primaryGroup: String, blockedGroupName: String)
    {
        //get user
        val group = db.collection("groups").document(primaryGroup).get().await().toObject(Group::class.java)

        var blocked = java.util.ArrayList<String>()
        //check that user was returned
        if(group != null)
        {

            //get a list of matched groups and blocked groups
            val matched = group.matchedGroups
            if (group.blockedGroups.isNotEmpty())
            {
                blocked = group.blockedGroups
                blocked.add(blockedGroupName)
            }
            else{
                blocked.add(blockedGroupName)
            }
            //update matched groups and blocked groups
            matched.remove(blockedGroupName)
            var updateGroup = HashMap<String, Any>()
            updateGroup["matchedGroups"] = matched
            updateGroup["blockedGroups"] = blocked
            //get users from from the group
            val users = group.users!!

            CoroutineScope(Dispatchers.IO).launch{
                for (user in users)
                {
                    //iterate through all members of the group and remove group from matched
                    //and add them to blocked
                    blockGroupUserUpdate(user, blockedGroupName)
                }
            }
            //update group's matched and blocked groups
            db.collection("groups").document(primaryGroup).update(updateGroup).await()
        }
    }

        // match tab fragment listens to when undo or more button is clicked
        interface CardStackAdapterListener {
            fun onUndoBtnClick(position: Int)
            fun onAcceptBtnClick()
            fun onRejectBtnClick(group:String)
            fun observeUndoState(undoBtn:ImageButton)
        }
}
