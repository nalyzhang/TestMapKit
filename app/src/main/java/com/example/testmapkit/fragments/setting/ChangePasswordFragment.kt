package com.example.testmapkit.fragments.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.btnBackChange.setOnClickListener {
            findNavController().navigate(R.id.action_changePasswordFragment_to_settingFragment)
        }

        binding.btnChangePass.setOnClickListener {
            findNavController().navigate(R.id.action_changePasswordFragment_to_settingFragment)
        }
    }
}