package com.food24.track.ui.welcome

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.food24.track.MainActivity
import com.food24.track.R

class WelcomeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireActivity().getSharedPreferences("UserData", MODE_PRIVATE)
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.getStartedButton).setOnClickListener {
            sharedPreferences.edit()
                .putBoolean("isFirstLaunch", false)
                .putBoolean("hasStarted", true)
                .apply()

            (activity as? MainActivity)?.openDashboardFragment()
        }
    }
}
