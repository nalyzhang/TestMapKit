package com.example.testmapkit.fragments.setting.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.testmapkit.R
import com.example.testmapkit.STATISTIC
import com.example.testmapkit.dataModels.UserWithStatistic
import com.example.testmapkit.databinding.FragmentProfileBinding
import com.example.testmapkit.fragments.setting.user.UserViewModel
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val userRepository = UserRepository(retrofitClient.apiService)
        userViewModel = UserViewModel(userRepository, tokenManager)

        init()

        observeViewModel()
    }

    private fun init() {
        binding.btnBackProfile.setOnClickListener {
            if(STATISTIC)
                findNavController().navigate(
                    R.id.action_profileFragment_to_userStatisticFragment
                )
            else
                findNavController().navigate(
                R.id.action_profileFragment_to_settingFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_editProfileFragment
            )
        }

        binding.btnUserStatisticProfile.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_userStatisticFragment
            )
        }

        showLoading(true)
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными пользователя
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUser(user)
                showLoading(false)
            }
        }

        // Наблюдаем за ошибками
        userViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                userViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun updateUser(currentUser: UserWithStatistic) {
        binding.tvProfileId.text = currentUser.id.toString()
        binding.tvProfileEmail.text = currentUser.email
        binding.tvProfileUsername.text = currentUser.username
        binding.tvProfileFirstName.text = currentUser.firstName
        binding.tvProfileLastName.text = currentUser.lastName
        binding.tvProfileStatCount.text = currentUser.routesCount.toString()

        if (!currentUser.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentUser.getFullAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.imgProfile)
        } else {
            binding.imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbProfile.visibility = View.VISIBLE
            binding.llProfileDetail.visibility = View.GONE
        } else {
            binding.pbProfile.visibility = View.GONE
            binding.llProfileDetail.visibility = View.VISIBLE
        }
    }
}