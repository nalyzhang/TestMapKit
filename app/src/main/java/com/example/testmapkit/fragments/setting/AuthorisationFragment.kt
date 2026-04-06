package com.example.testmapkit.fragments.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentAuthorisationBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository
import com.example.testmapkit.repositories.UserResult

class AuthorisationFragment : Fragment() {

    lateinit var binding: FragmentAuthorisationBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorisationBinding.inflate(
            layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создаем зависимости
        val tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.getInstance(tokenManager)
        val userRepository = UserRepository(retrofitClient.apiService)

        // Создаем ViewModel с зависимостями
        userViewModel = UserViewModel(userRepository, tokenManager)

        init()

        observeViewModel()
    }

    private fun init() {

        binding.btnToReg.isEnabled = false
        binding.tvAuthTitle.text = "Регистрация"

        binding.btnToReg.setOnClickListener {
            showRegistrationForm()

        }

        binding.btnToAuth.setOnClickListener {
            showLoginForm()
        }

        binding.btnAuth.setOnClickListener {
            performLogin()
        }

        binding.btnRegistration.setOnClickListener {
            performRegistration()
        }

        binding.btnBackAuth.setOnClickListener {
            findNavController().navigate(
                R.id.action_authorisationFragment_to_settingFragment)
        }
    }

    private fun showRegistrationForm() {
        binding.llAuthFirstName.visibility = View.VISIBLE
        binding.llAuthLastName.visibility = View.VISIBLE
        binding.llAuthUsername.visibility = View.VISIBLE
        binding.btnAuth.visibility = View.GONE
        binding.btnRegistration.visibility = View.VISIBLE
        binding.btnToReg.isEnabled = false
        binding.btnToAuth.isEnabled = true
        binding.tvAuthTitle.text = "Регистрация"
        clearFields()
    }

    private fun showLoginForm() {
        binding.llAuthFirstName.visibility = View.GONE
        binding.llAuthLastName.visibility = View.GONE
        binding.llAuthUsername.visibility = View.GONE
        binding.btnRegistration.visibility = View.GONE
        binding.btnAuth.visibility = View.VISIBLE
        binding.btnToReg.isEnabled = true
        binding.btnToAuth.isEnabled = false
        binding.tvAuthTitle.text = "Вход"
        clearFields()
    }

    private fun performLogin() {
        val email = binding.etAuthEmail.text.toString().trim()
        val password = binding.etAuthPassword.text.toString().trim()

        // Вызываем вход через ViewModel
        userViewModel.login(email, password)
    }

    private fun performRegistration() {
        val username = binding.etAuthUsername.text.toString().trim()
        val email = binding.etAuthEmail.text.toString().trim()
        val password = binding.etAuthPassword.text.toString().trim()
        val firstName = binding.etAuthFirstName.text.toString().trim()
        val lastName = binding.etAuthLastName.text.toString().trim()

        // Вызываем регистрацию через ViewModel
        userViewModel.register(username, email, password, firstName, lastName)
    }

    private fun clearFields() {
        binding.etAuthEmail.text?.clear()
        binding.etAuthPassword.text?.clear()
        binding.etAuthUsername.text?.clear()
        binding.etAuthFirstName.text?.clear()
        binding.etAuthLastName.text?.clear()
    }


    private fun observeViewModel() {
        // Регистрация
        userViewModel.registrationState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Loading -> showLoading(true)
                is UserResult.Success -> {
                    showLoading(false)
                    showToast("Регистрация успешна! Выполняется вход...")
                }
                is UserResult.Error -> {
                    showLoading(false)
                    showToast(result.message)
                    userViewModel.clearError()
                }
                null -> showLoading(false)
            }
        }

        // Вход
        userViewModel.loginState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Loading -> showLoading(true)
                is UserResult.Success -> {
                    showLoading(false)
                    showToast("Вход выполнен успешно!")
                    findNavController().navigate(
                        R.id.action_authorisationFragment_to_settingFragment)
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
        binding.btnAuth.isEnabled = !isLoading
        binding.btnRegistration.isEnabled = !isLoading
        binding.btnToAuth.isEnabled = !isLoading
        binding.btnToReg.isEnabled = !isLoading
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}