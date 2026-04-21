package com.example.testmapkit.fragments.setting.friend

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.testmapkit.FRIEND_ID
import com.example.testmapkit.R
import com.example.testmapkit.dataModels.UserWithStatistic
import com.example.testmapkit.databinding.FragmentFriendBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.FriendRepository
import com.example.testmapkit.repositories.FriendResult

class FriendFragment : Fragment() {

    lateinit var binding: FragmentFriendBinding
    private lateinit var friendViewModel: FriendViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val friendRepository = FriendRepository(retrofitClient.apiService)
        friendViewModel = FriendViewModel(friendRepository, tokenManager)

        val userID = arguments?.getInt(FRIEND_ID)
        if (userID != null) loadFriend(userID)

        init(userID)
        observeViewModel()
    }

    private fun init(userID: Int?) {
        binding.btnBackFriend.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_friendsListFragment)
        }

        binding.btnRemoveFriend.setOnClickListener {
            if (userID != null) showUpdateConfirmationDialog(userID)
        }

        showLoading(true)
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        friendViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными пользователя
        friendViewModel.userByID.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUser(user)
                showLoading(false)
            }
        }

        // Наблюдаем за результатом удаления друга
        friendViewModel.removeFriendState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is FriendResult.Loading -> {
                    showLoading(true)
                }
                is FriendResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Пользователь удален из друзей",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is FriendResult.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }

        // Наблюдаем за ошибками
        friendViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                friendViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun updateUser(user: UserWithStatistic) {
        binding.tvUserId.text = user.id.toString()
        binding.tvFriendUsername.text = user.username
        binding.tvFriendStatCount.text = user.routesCount.toString()

        if (!user.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.getFullAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.imgFriend)
        } else {
            binding.imgFriend.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun loadFriend(userID: Int) {
        if (tokenManager.hasToken()) {
            friendViewModel.getUserByID(userID)
        } else {
            Toast.makeText(
                requireContext(),
                "Пользователь не авторизован",
                Toast.LENGTH_SHORT
            ).show()
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbFriend.visibility = View.VISIBLE
            binding.llFriendDetail.visibility = View.GONE
        } else {
            binding.pbFriend.visibility = View.GONE
            binding.llFriendDetail.visibility = View.VISIBLE
        }
    }

    private fun showUpdateConfirmationDialog(userID: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить пользователя из списка друзей?")
            .setPositiveButton("Да") { _, _ ->
                friendViewModel.removeFriend(userID)
                findNavController().navigate(
                    R.id.action_friendFragment_to_friendsListFragment
                )
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}