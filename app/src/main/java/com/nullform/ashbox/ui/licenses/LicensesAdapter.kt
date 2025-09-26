package com.nullform.ashbox.ui.licenses

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.author
import com.nullform.ashbox.R // Make sure this points to your app's R file

class LicenseAdapter(private val libraries: List<Library>) :
    RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    private val NO_LICENSE_TEXT = "No license text available."

    class LicenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val libraryName: TextView = itemView.findViewById(R.id.license_library_name)
        val author: TextView = itemView.findViewById(R.id.license_author)
        val licenseText: TextView = itemView.findViewById(R.id.license_text)
        val readMoreButton: Button = itemView.findViewById(R.id.license_read_more_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_license, parent, false)
        return LicenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        val library = libraries[position]

        holder.libraryName.text = library.name
        holder.author.text = library.author

        // Set license text, handling potential truncation
        val licenseContent = library.licenses.firstOrNull()?.licenseContent
        if (!licenseContent.isNullOrBlank()) {
            holder.licenseText.text = licenseContent
            holder.licenseText.post { // Post to ensure layout has been measured
                if (holder.licenseText.layout != null && holder.licenseText.layout.lineCount > holder.licenseText.maxLines) {
                    holder.readMoreButton.visibility = View.VISIBLE
                    // You would typically handle a click listener here to show the full text
                    // For now, it just appears.
                    holder.readMoreButton.setOnClickListener {
                        // Implement logic to show full license text, e.g., in a dialog or new fragment
                        // For demonstration, let's just log it:
                        // Log.d("LicenseAdapter", "Read More clicked for ${library.name}")
                        // To actually show full text:
                        // val dialogFragment = FullLicenseDialogFragment.newInstance(library.name, licenseContent)
                        // dialogFragment.show((holder.itemView.context as? AppCompatActivity)?.supportFragmentManager!!, "full_license_dialog")
                        // For now, we'll expand it directly for simplicity, but a dialog is better for long text
                        holder.licenseText.maxLines = Int.MAX_VALUE // Expand fully
                        holder.licenseText.ellipsize = null // Remove ellipsis
                        holder.readMoreButton.visibility = View.GONE // Hide button after expanding
                    }
                } else {
                    holder.readMoreButton.visibility = View.GONE
                }
            }
        } else {
            holder.licenseText.text = NO_LICENSE_TEXT
            holder.readMoreButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = libraries.size
}