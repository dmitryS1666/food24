package com.food24.track.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val vm: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // notifications: подписка и запись
        viewLifecycleOwner.lifecycleScope.launch {
            vm.notificationsEnabled.collectLatest { enabled ->
                if (binding.switchNotifications.isChecked != enabled) {
                    binding.switchNotifications.isChecked = enabled
                }
            }
        }
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            vm.setNotifications(isChecked)
        }

        // back
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Clear All Data (с подтверждением)
        binding.btnClearData.setOnClickListener {
            confirmClearAll()
        }

        // Share (поделиться приложением)
        binding.itemShare.setOnClickListener { shareApp() }

        // Rate (оценить в маркете)
        binding.itemRate.setOnClickListener { rateApp() }

        // Privacy Policy (ссылка)
        binding.itemPrivacy.setOnClickListener { openPrivacy() }
    }

    private fun confirmClearAll() {
        // простой диалог без зависимостей
        val ctx = requireContext()
        android.app.AlertDialog.Builder(ctx)
            .setTitle("Clear All Data")
            .setMessage("This will remove all local data (plans, meals, progress). Continue?")
            .setPositiveButton("Clear") { d, _ ->
                vm.clearAllData(
                    onDone = {
                        Toast.makeText(ctx, "Data cleared", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(ctx, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                )
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareApp() {
        val shareText =
            "MyMealPlan — offline meal planner. Try it!\nhttps://play.google.com/store/apps/details?id=${requireContext().packageName}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun rateApp() {
        val pkg = requireContext().packageName
        val marketUri = Uri.parse("market://details?id=$pkg")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun openPrivacy() {
        // подставь свою реальную ссылку
        val url = "https://yourdomain.com/privacy"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
