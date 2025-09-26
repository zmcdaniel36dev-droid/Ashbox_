package com.nullform.ashbox.ui.licenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import com.nullform.ashbox.R

class LicensesFragment : Fragment() {

    val TAG: String = "LicensesFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_licenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val libs = Libs.Builder().withContext(requireContext()).build()

        val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.licenses_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val licensesAdapter = LicenseAdapter(libs.libraries)
        recyclerView.adapter = licensesAdapter
    }
}