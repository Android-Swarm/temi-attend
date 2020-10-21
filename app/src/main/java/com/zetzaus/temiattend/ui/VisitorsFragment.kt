package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.adapter.VisitorAdapter
import com.zetzaus.temiattend.databinding.FragmentVisitorsBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.addOnTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_visitors.*
import javax.inject.Inject

@AndroidEntryPoint
class VisitorsFragment : BindingFragment<FragmentVisitorsBinding>() {

    private val viewModel by viewModels<VisitorsFragmentViewModel>()

    @Inject
    lateinit var adapter: VisitorAdapter

    override fun layoutId() = R.layout.fragment_visitors

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
        binding.adapter = adapter
        binding.itemDecoration =
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmButton.setOnClickListener { requireActivity().onBackPressed() }

        viewModel.visitorToDisplay.observe(viewLifecycleOwner) {
            Log.d(LOG_TAG, "Displaying ${it.size} visitors")
            adapter.submitList(it)
        }

        inputSearch.addOnTextChangedListener { query, _, _, _ -> viewModel.submitQuery(query) }
    }
}