package com.example.testmapkit.fragments.setting

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.testmapkit.R
import com.example.testmapkit.databinding.FragmentRulesBinding


class RulesFragment : Fragment() {

    lateinit var binding: FragmentRulesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRulesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvRulesDesc.text = Html.fromHtml(getString(R.string.rules))
        init()
    }

    private fun init() {
        binding.btnBackRules.setOnClickListener {
            findNavController().navigate(R.id.action_rulesFragment_to_settingFragment)
        }
    }

}