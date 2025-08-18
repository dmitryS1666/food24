package com.food24.track.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.food24.track.BuildConfig
import com.food24.track.R
import com.food24.track.data.database.AppDatabase
import com.food24.track.data.repository.DataRepository
import com.food24.track.viewmodel.DashboardViewModel
import com.food24.track.viewmodel.DashboardViewModelFactory

class SettingsFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var clearDataLayout: View
    private lateinit var contactLink: TextView
    private lateinit var privacyPolicyLink: TextView
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(db.eventDao(), db.participantDao(), db.teamParticipantDao(), db.teamDao())

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        // Найди элементы
        clearDataLayout = view.findViewById(R.id.clearDataLayout)
        contactLink = view.findViewById(R.id.contactLink)
        privacyPolicyLink = view.findViewById(R.id.privacyPolicyLink)
        backButton = view.findViewById(R.id.backButton)

        val appPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        clearDataLayout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all local data?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.clearAllData()
                    appPrefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Data cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

//        contactLink.setOnClickListener {
//            val intent = Intent(requireContext(), PrivacyWebViewActivity::class.java)
//            intent.putExtra("url", "https://food24.xyz/contact.html")
//            startActivity(intent)
//        }
//
//        privacyPolicyLink.setOnClickListener {
//            val bannerPrefs = requireContext().getSharedPreferences("banner_prefs", Context.MODE_PRIVATE)
//            val userId = bannerPrefs.getString("user_id", UUID.randomUUID().toString())
//            val installer = getInstallerPackageName(requireContext())
//
//            val url = "https://food24.xyz/privacy/?installer=$installer&id_user=$userId"
//
//            val intent = Intent(requireContext(), PrivacyWebViewActivity::class.java)
//            intent.putExtra("url", url)
//            startActivity(intent)
//        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun getInstallerPackageName(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName ?: "unknown"
            } else {
                context.packageManager.getInstallerPackageName(context.packageName) ?: "unknown"
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) "debug" else "unknown"
        }
    }
}
