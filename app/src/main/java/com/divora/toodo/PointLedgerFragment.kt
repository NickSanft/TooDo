package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.divora.toodo.databinding.FragmentPointLedgerBinding

class PointLedgerFragment : Fragment() {

    private var _binding: FragmentPointLedgerBinding? = null
    private val binding get() = _binding!!

    private lateinit var pointLedgerViewModel: PointLedgerViewModel
    private lateinit var adapter: PointLedgerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointLedgerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pointLedgerViewModel = ViewModelProvider(this).get(PointLedgerViewModel::class.java)

        adapter = PointLedgerAdapter()
        binding.ledgerList.adapter = adapter
        binding.ledgerList.layoutManager = LinearLayoutManager(context)

        pointLedgerViewModel.allLedgerEntries.observe(viewLifecycleOwner) {
            entries -> adapter.submitList(entries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
