package com.example.batchtest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.ListItemUserBinding

class UserHolder(
    val binding: ListItemUserBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        binding.nameListTitle.text = user.getName()
    }
}

class UserAdapter(
    private val users: List<User>
) : RecyclerView.Adapter<UserHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemUserBinding.inflate(inflater, parent, false)
        return UserHolder(binding)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }

}