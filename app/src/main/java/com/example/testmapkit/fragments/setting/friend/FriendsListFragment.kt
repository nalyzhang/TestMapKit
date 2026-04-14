package com.example.testmapkit.fragments.setting.friend

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmapkit.FRIEND_ID
import com.example.testmapkit.R
import com.example.testmapkit.TAG
import com.example.testmapkit.adapters.UserAdapter
import com.example.testmapkit.dataModels.User
import com.example.testmapkit.databinding.FragmentFriendsListBinding
import com.example.testmapkit.network.RetrofitClient
import com.example.testmapkit.network.TokenManager
import com.example.testmapkit.repositories.FriendRepository
import com.example.testmapkit.repositories.FriendResult

class FriendsListFragment : Fragment() {

    lateinit var binding: FragmentFriendsListBinding
    private lateinit var friendViewModel: FriendViewModel
    private lateinit var tokenManager: TokenManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tokenManager = TokenManager(requireContext())
        val retrofitClient = RetrofitClient.Companion.getInstance(tokenManager)
        val friendRepository = FriendRepository(retrofitClient.apiService)
        friendViewModel = FriendViewModel(friendRepository, tokenManager)

        recyclerView = binding.rvFriend
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        init()
        observeViewModel()
        loadFriendsList()
    }

    private fun init() {

        binding.btnBackFriendList.setOnClickListener {
            findNavController().navigate(R.id.action_friendsListFragment_to_settingFragment)
        }

        binding.btnAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        userAdapter = UserAdapter(emptyList()) { userId ->

            val bundle = Bundle().apply {
                putInt(FRIEND_ID, userId)
            }
            findNavController().navigate(
                R.id.action_friendsListFragment_to_friendFragment,
                bundle
            )
        }

        binding.rvFriend.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriend.adapter = userAdapter
        binding.searchFriend.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                Log.d(TAG, "onQueryTextChange")
                observeViewModel()
                userAdapter.findItem(query)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d(TAG, "onQueryTextSubmit")
                binding.searchFriend.hideKeyboard()
                binding.searchFriend.clearFocus()
                observeViewModel()
                userAdapter.findItem(query)
                return false
            }
        })

        binding.root.setOnClickListener {
            if (binding.searchFriend.hasFocus()) {
                binding.searchFriend.hideKeyboard()
                binding.searchFriend.clearFocus()
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun observeViewModel() {

        // Наблюдаем за состоянием загрузки
        friendViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Наблюдаем за данными пользователя
        friendViewModel.friendsList.observe(viewLifecycleOwner) { friendsList ->
            if (friendsList != null) {
                updateFriendsList(friendsList)
                showLoading(false)
            }
        }

        friendViewModel.addFriend.observe(viewLifecycleOwner) { result ->
            when (result) {
                is FriendResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Друг успешно добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadFriendsList() // Обновляем список
                }
                is FriendResult.Error -> {
                    Toast.makeText(requireContext(),
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
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                friendViewModel.clearError()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbFriendsList.visibility = View.VISIBLE
            binding.rvFriend.visibility = View.GONE
        } else {
            binding.pbFriendsList.visibility = View.GONE
            binding.rvFriend.visibility = View.VISIBLE
        }
    }

    private fun loadFriendsList() {
        if (tokenManager.hasToken()) {
            friendViewModel.getFriendsList()
        } else {
            Toast.makeText(
                requireContext(),
                "Пользователь не авторизован",
                Toast.LENGTH_SHORT
            ).show()
            showLoading(false)
        }
    }

    private fun updateFriendsList(friendsList: List<User>) {
        if (friendsList.isEmpty()) {
            binding.rvFriend.visibility = View.GONE
            binding.searchFriend.visibility = View.GONE
            binding.tvEmptyFriendsList.visibility = View.VISIBLE
        } else {
            binding.rvFriend.visibility = View.VISIBLE
            binding.searchFriend.visibility = View.VISIBLE
            binding.tvEmptyFriendsList.visibility = View.GONE
            userAdapter.updateData(friendsList)
        }
    }

    override fun onResume() {
        super.onResume()
        if (tokenManager.hasToken()) {
            loadFriendsList()
        }
    }

    private fun showAddFriendDialog() {
        // Создаем EditText для ввода ID
        val editText = EditText(requireContext()).apply {
            hint = "Введите ID друга"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        // Настраиваем диалог
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить друга")
            .setMessage("Введите ID пользователя, которого хотите добавить в друзья")
            .setView(editText)
            .setPositiveButton("Добавить") { _, _ ->
                val friendId = editText.text.toString().trim()
                if (friendId.isNotEmpty()) {
                    addFriend(friendId.toIntOrNull())
                } else {
                    Toast.makeText(requireContext(), "Введите ID друга", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addFriend(friendId: Int?) {
        if (friendId == null) {
            Toast.makeText(requireContext(), "Неверный ID", Toast.LENGTH_SHORT).show()
            return
        }

        friendViewModel.addFriend(friendId)
    }
}