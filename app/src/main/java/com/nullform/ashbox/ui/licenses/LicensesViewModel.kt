// In a new file, e.g., ui/licenses/LicensesViewModel.kt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.oss.licenses.OssLicensesService
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.lowercase

// Data class to represent a single license entry
data class LicenseInfo(
    val libraryName: String,
    val licenseText: String
)

// UI state for the screen
data class LicensesUiState(
    val licenses: List<LicenseInfo> = emptyList(),
    val isLoading: Boolean = true
)

class LicensesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LicensesUiState())
    val uiState: StateFlow<LicensesUiState> = _uiState.asStateFlow()

    init {
        loadLicenses()
    }

    private fun loadLicenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // The license loading task is blocking, so run it on a background thread
            val loadedLicenses = withContext(Dispatchers.IO) {
                val licensesTask = OssLicensesService.getLicenseInfo(getApplication())
                // Tasks.await() is a blocking call to wait for the result
                val licenses = Tasks.await(licensesTask)

                licenses.map { license ->
                    LicenseInfo(
                        libraryName = license.libraryName,
                        // Load the full text for each license
                        licenseText = Tasks.await(OssLicensesService.getLicenseText(getApplication(), license))
                    )
                }.sortedBy { it.libraryName.lowercase() } // Sort alphabetically
            }

            _uiState.update {
                it.copy(
                    licenses = loadedLicenses,
                    isLoading = false
                )
            }
        }
    }
}