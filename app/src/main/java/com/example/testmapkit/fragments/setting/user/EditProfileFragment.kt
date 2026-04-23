package com.example.testmapkit.fragments.setting.user

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.testmapkit.R
import com.example.testmapkit.dataModels.UserUpdate
import com.example.testmapkit.dataModels.UserWithStatistic
import com.example.testmapkit.databinding.FragmentEditProfileBinding
import com.example.testmapkit.fragments.setting.user.UserViewModel
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.UserRepository
import com.example.testmapkit.repositories.UserResult
import java.io.File

class EditProfileFragment : Fragment() {
    lateinit var binding: FragmentEditProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var tokenManager: TokenManager
    private var originalUserData: UserUpdate? = null

    // Для выбора изображения
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val userRepository = UserRepository(retrofitClient.apiService)
        userViewModel = UserViewModel(userRepository, tokenManager)

        registerImagePickers()
        init()
        observeViewModel()
        loadUserData()
    }

    private fun init() {
        binding.btnBackEditProfile.setOnClickListener {
            showReturnConfirmationDialog()
        }
        binding.btnSaveEditProfile.setOnClickListener {
            showUpdateConfirmationDialog()
        }
        binding.btnEditAvatar.setOnClickListener {
            showEditAvatarDialog()
        }

        binding.btnChangePass.setOnClickListener {
            findNavController().navigate(
                R.id.action_editProfileFragment_to_changePasswordFragment)
        }
    }

    private fun registerImagePickers() {
        // Регистрация для выбора изображения из галереи
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                imageUri?.let {
                    selectedImageUri = it
                    uploadAvatar(it)
                }
            }
        }

        // Регистрация для фото с камеры
        takePhotoLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val uri = Uri.fromFile(File(path))
                    selectedImageUri = uri
                    uploadAvatar(uri)
                }
            } else {
                Toast.makeText(requireContext(), "Фото не сделано", Toast.LENGTH_SHORT).show()
            }
        }
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
                saveOriginalUserData(user)
                showLoading(false)
            }
        }

        // Наблюдаем за результатом обновления
        userViewModel.updateState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Loading -> {
                    showLoading(true)
                }
                is UserResult.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Профиль успешно обновлен", Toast.LENGTH_SHORT).show()
                    // Возвращаемся на профиль
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                }
                is UserResult.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Наблюдаем за результатом обновления аватара
        userViewModel.avatarUpdateState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserResult.Loading -> {
                    showLoading(true)
                }
                is UserResult.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Аватар успешно обновлен", Toast.LENGTH_SHORT).show()
                    // Обновляем данные пользователя
                    userViewModel.getCurrentUser()
                }
                is UserResult.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
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

    private fun loadUserData() {
        if (tokenManager.hasToken()) {
            userViewModel.getCurrentUser()
        } else {
            showLoading(false)
            Toast.makeText(
                requireContext(),
                "Пользователь не авторизован",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveOriginalUserData(user: UserWithStatistic) {
        originalUserData = UserUpdate(
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }

    private fun saveChanges() {
        // Получаем новые значения из полей
        val newUsername = binding.etEditUsername.text.toString().trim()
        val newEmail = binding.etEditEmail.text.toString().trim()
        val newFirstName = binding.etEditFirstName.text.toString().trim()
        val newLastName = binding.etEditLastName.text.toString().trim()

        // Проверяем, были ли изменения
        if (hasChanges(newUsername, newEmail, newFirstName, newLastName)) {
            // Вызываем метод обновления в ViewModel
            userViewModel.putCurrentUser(
                newUsername, newEmail, newFirstName, newLastName
            )
        }
    }

    private fun hasChanges(
        newUsername: String,
        newEmail: String,
        newFirstName: String,
        newLastName: String
    ): Boolean {
        return originalUserData?.let {
            it.username != newUsername ||
                    it.email != newEmail ||
                    it.firstName != newFirstName ||
                    it.lastName != newLastName
        } ?: true
    }

    private fun updateUser(currentUser: UserWithStatistic) {
        binding.etEditEmail.setText(currentUser.email)
        binding.etEditUsername.setText(currentUser.username)
        binding.etEditFirstName.setText(currentUser.firstName)
        binding.etEditLastName.setText(currentUser.lastName)

        if (!currentUser.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentUser.getFullAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.imgEditProfile)
        } else {
            binding.imgEditProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbEditProfile.visibility = View.VISIBLE
            binding.svEditView.visibility = View.GONE
        } else {
            binding.pbEditProfile.visibility = View.GONE
            binding.svEditView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении
        if (tokenManager.hasToken()) {
            userViewModel.getCurrentUser()
        }
    }

    private fun showUpdateConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Сохранение")
            .setMessage("Принять изменения?")
            .setPositiveButton("Да") { _, _ ->
                saveChanges()
                findNavController().navigate(
                    R.id.action_editProfileFragment_to_profileFragment
                )
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun showReturnConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход из редактора")
            .setMessage("Уверены, что не хотите сохранить изменения?")
            .setPositiveButton("Да") { _, _ ->
                findNavController().navigate(
                    R.id.action_editProfileFragment_to_profileFragment
                )
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun showEditAvatarDialog() {
        val options = arrayOf("Выбрать из галереи", "Сделать фото", "Удалить аватар")

        AlertDialog.Builder(requireContext())
            .setTitle("Изменение аватара")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                    2 -> deleteAvatar()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageLauncher.launch(intent)
    }

    private fun takePhotoFromCamera() {
        // Проверяем разрешение на камеру
        if (checkCameraPermission()) {
            createImageFile()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile() {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().cacheDir
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir)
        currentPhotoPath = imageFile.absolutePath

        val photoURI = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
        takePhotoLauncher.launch(photoURI)
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createImageFile()
        } else {
            Toast.makeText(
                requireContext(),
                "Нет разрешения на использование камеры",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return requireContext().checkSelfPermission(
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadAvatar(imageUri: Uri) {
        try {
            // Конвертируем URI в Base64
            val base64Image = convertImageToBase64(imageUri)
            if (base64Image != null) {
                userViewModel.updateAvatar(base64Image)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Не удалось обработать изображение",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Ошибка при загрузке: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun convertImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            bytes?.let {
                // Определяем MIME тип изображения
                val mimeType = getMimeType(imageUri)
                val base64Data = Base64.encodeToString(it, Base64.NO_WRAP)

                // Формируем правильный формат для Django
                "data:$mimeType;base64,$base64Data"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getMimeType(uri: Uri): String {
        val mimeType = requireContext().contentResolver.getType(uri)
        return mimeType ?: "image/jpeg" // По умолчанию JPEG
    }

    private fun compressImage(bytes: ByteArray): ByteArray {
        // Простое сжатие - можно использовать Bitmap для лучшего сжатия
        return bytes
    }

    private fun deleteAvatar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление аватара")
            .setMessage("Вы уверены, что хотите удалить аватар?")
            .setPositiveButton("Да") { _, _ ->
                userViewModel.deleteAvatar()
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}