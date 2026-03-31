package com.example.testmapkit.fragments.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.TOKEN
import com.example.testmapkit.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {

        if (TOKEN != null) {
            binding.authorisationCard.visibility = View.GONE
            binding.profileCard.visibility = View.VISIBLE
            binding.profileFriendsCard.visibility = View.VISIBLE
        }
        else {
            binding.authorisationCard.visibility = View.VISIBLE
            binding.profileCard.visibility = View.GONE
            binding.profileFriendsCard.visibility = View.GONE
        }

        binding.authorisationCard.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_authorisationFragment)
        }

        binding.profileCard.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_profileFragment)
        }

        binding.profileFriendsCard.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_friendsListFragment)
        }
        
        binding.rulesCard.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_rulesFragment)
        }
    }
}