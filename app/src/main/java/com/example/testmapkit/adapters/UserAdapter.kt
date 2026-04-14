package com.example.testmapkit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.testmapkit.R
import com.example.testmapkit.dataModels.User
import com.example.testmapkit.databinding.ItemFriendBinding
import java.util.Locale.getDefault

class UserAdapter(
    private var users: List<User>,
    private val onItemClick: ((userId: Int) -> Unit)? = null
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = users[position]

        with(viewHolder) {
            binding.tvFriendUsername.text = user.username

            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.getFullAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(binding.imgFriendList)
            } else {
                binding.imgFriendList.setImageResource(R.drawable.ic_profile)
            }

            // Обработка нажатия на элемент
            binding.root.setOnClickListener {
                onItemClick?.invoke(user.id)
            }
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun findItem(query: String) {
        val foundUsers: MutableList<User> = mutableListOf()

        query.lowercase(getDefault())

        for (user in users) {
            val name = user.username.lowercase(getDefault())
            if (name.contains(query.lowercase(getDefault()))) {
                foundUsers += user
            }
        }
        updateData(foundUsers)
    }
}