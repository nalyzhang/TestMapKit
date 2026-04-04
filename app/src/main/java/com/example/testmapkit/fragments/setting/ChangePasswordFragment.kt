package com.example.testmapkit.fragments.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentChangePasswordBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository
import com.example.testmapkit.repositories.UserResult

class ChangePasswordFragment : Fragment() {

    lateinit var binding: FragmentChangePasswordBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val userRepository = UserRepository(retrofitClient.apiService)
        userViewModel = UserViewModel(userRepository, tokenManager)

        init()

        observeViewModel()
    }

    private fun init() {
        binding.btnBackChange.setOnClickListener {
            findNavController().navigate(
                R.id.action_changePasswordFragment_to_profileFragment)
        }

        binding.btnChangePass.setOnClickListener {
            performChangePassword()
        }
    }

    private fun performChangePassword() {
        val currentPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()

        userViewModel.changePassword(currentPassword, newPassword)
    }

    private fun observeViewModel() {
        // Изменение пароля
        userViewModel.changePassword.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Loading -> showLoading(true)
                is UserResult.Success -> {
                    showLoading(false)
                    showToast("Пароль изменен")
                    findNavController().navigate(
                        R.id.action_changePasswordFragment_to_profileFragment)
                }
                is UserResult.Error -> {
                    showLoading(false)
                    showToast(result.message)
                    userViewModel.clearError()
                }
                null -> showLoading(false)
            }
        }

        // Ошибки валидации и другие сообщения
        userViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showToast(it)
                userViewModel.clearError()
            }
        }

        // Состояние загрузки
        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnBackChange.isEnabled = !isLoading
        binding.btnChangePass.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}