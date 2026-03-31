package com.example.testmapkit.fragments.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentAuthorisationBinding

class AuthorisationFragment : Fragment() {

    lateinit var binding: FragmentAuthorisationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorisationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.btnToReg.setOnClickListener {
            binding.llAuthFirstName.visibility = View.VISIBLE
            binding.llAuthLastName.visibility = View.VISIBLE
            binding.llAuthUsername.visibility = View.VISIBLE
            binding.btnAuth.visibility = View.VISIBLE
            binding.btnRegistration.visibility = View.GONE
        }

        binding.btnToAuth.setOnClickListener {
            binding.llAuthFirstName.visibility = View.GONE
            binding.llAuthLastName.visibility = View.GONE
            binding.llAuthUsername.visibility = View.GONE
            binding.btnRegistration.visibility = View.VISIBLE
            binding.btnAuth.visibility = View.GONE
        }

        binding.btnAuth.setOnClickListener {
            // TODO auth
            findNavController().navigate(R.id.action_authorisationFragment_to_settingFragment)
        }

        binding.btnRegistration.setOnClickListener {
            // TODO registration
            findNavController().navigate(R.id.action_authorisationFragment_to_settingFragment)
        }

        binding.btnBackAuth.setOnClickListener {
            findNavController().navigate(R.id.action_authorisationFragment_to_settingFragment)  }
    }
}