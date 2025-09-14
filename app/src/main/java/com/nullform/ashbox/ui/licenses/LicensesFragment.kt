// In your ui/licenses/LicensesFragment.kt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nullform.ashbox.databinding.FragmentLicensesBinding // Use your binding class
import com.nullform.ashbox.ui.licenses.LicensesAdapter
import kotlinx.coroutines.launch

class LicensesFragment : Fragment() {

    private var _binding: FragmentLicensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LicensesViewModel by viewModels()
    private lateinit var licensesAdapter: LicensesAdapter // You'll need to create this adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLicensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Create and set up your LicensesAdapter
        // licensesAdapter = LicensesAdapter { licenseInfo ->
        //     // Handle click, e.g., show a dialog with the full license text
        // }
        // binding.licensesRecyclerView.adapter = licensesAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.progressBar.isVisible = uiState.isLoading
                binding.licensesRecyclerView.isVisible = !uiState.isLoading

                // Update the adapter with the list of licenses
                // licensesAdapter.submitList(uiState.licenses)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}