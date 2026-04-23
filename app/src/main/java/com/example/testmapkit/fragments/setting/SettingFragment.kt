package com.example.testmapkit.fragments.setting

import android.app.AlertDialog
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
import com.example.testmapkit.databinding.FragmentSettingBinding
import com.example.testmapkit.fragments.setting.user.UserViewModel
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository
import com.example.testmapkit.repositories.UserResult

class SettingFragment : Fragment() {
    lateinit var binding: FragmentSettingBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val userRepository = UserRepository(retrofitClient.apiService)
        userViewModel = UserViewModel(userRepository, tokenManager)

        STATISTIC = false

        init()

        updateUIByAuthStatus()

        observeViewModel()

        loadUserData()
    }

    private fun init() {

        binding.authorisationCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingFragment_to_authorisationFragment)
        }

        binding.profileCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingFragment_to_profileFragment)
        }

        binding.profileFriendsCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingFragment_to_friendsListFragment)
        }
        
        binding.rulesCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingFragment_to_rulesFragment)
        }

        binding.logoutCard.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        updateUIByAuthStatus()
    }


    private fun observeViewModel() {
        // Наблюдаем за статусом авторизации
        userViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            updateUIByAuthStatus()
            if (!isAuthenticated) {
                // Если пользователь вышел, очищаем аватар
                clearAvatar()
            }
        }

        // Наблюдаем за данными текущего пользователя
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserAvatar(it.getFullAvatarUrl())
                updateUserName(it.username)
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
            }
        }

        // Наблюдаем за состоянием выхода
        userViewModel.logoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Вы вышли из аккаунта",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUIByAuthStatus()
                    clearAvatar()
                }
                is UserResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun loadUserData() {
        if (tokenManager.hasToken()) {
            userViewModel.getCurrentUser()
        }
    }

    private fun updateUIByAuthStatus() {
        val isAuthenticated = tokenManager.hasToken()

        if (isAuthenticated) {
            binding.authorisationCard.visibility = View.GONE
            binding.profileCard.visibility = View.VISIBLE
            binding.profileFriendsCard.visibility = View.VISIBLE
            binding.logoutCard.visibility = View.VISIBLE
        } else {
            binding.authorisationCard.visibility = View.VISIBLE
            binding.profileCard.visibility = View.GONE
            binding.profileFriendsCard.visibility = View.GONE
            binding.logoutCard.visibility = View.GONE
            binding.tvSettingUsername.visibility = View.GONE
        }
    }

    private fun updateUserAvatar(avatarUrl: String?) {
        if (!avatarUrl.isNullOrEmpty()) {
            // Загружаем аватар с помощью Glide (добавьте зависимость)
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile)  // Пока загружается
                .error(R.drawable.ic_profile)        // Если ошибка
                .circleCrop()                        // Круглый аватар
                .into(binding.imgProfile)
        } else {
            // Если аватара нет, показываем иконку по умолчанию
            binding.imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun updateUserName(username: String) {
        binding.tvSettingUsername.text = username
        binding.tvSettingUsername.visibility = View.VISIBLE
    }

    private fun clearAvatar() {
        binding.imgProfile.setImageResource(R.drawable.ic_profile)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun performLogout() {
        userViewModel.logout()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на фрагмент
        loadUserData()
        updateUIByAuthStatus()
    }

}