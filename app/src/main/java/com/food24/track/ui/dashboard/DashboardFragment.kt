package com.food24.track.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.widget.ViewPager2
import com.food24.track.MainActivity
import com.food24.track.R
import com.food24.track.data.database.AppDatabase
import com.food24.track.util.DebugLogger
import com.food24.track.data.repository.DataRepository
import com.food24.track.ui.event.AllEventsFragment
import com.food24.track.ui.event.CreateEventFragment
import com.food24.track.ui.event.EditEventFragment
import com.food24.track.ui.result.ResultsFragment
import com.food24.track.ui.team.CreateTeamFragment
import com.food24.track.viewmodel.DashboardViewModel
import com.food24.track.viewmodel.DashboardViewModelFactory
import com.google.android.material.imageview.ShapeableImageView

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var imageSlider: ViewPager2
    private val images = listOf(
        R.drawable.slide_1,
        R.drawable.slide_2,
        R.drawable.slide_3
    )
    private lateinit var sliderDots: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(
            db.eventDao(),
            db.participantDao(),
            db.teamParticipantDao(),
            db.teamDao()
        )

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        imageSlider = view.findViewById(R.id.imageSlider)
        sliderDots = view.findViewById(R.id.sliderDots1)
        imageSlider.adapter = ImageSliderAdapter(images)

        setupSliderDots(images.size)
        imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })

        viewModel.lastEvent.observe(viewLifecycleOwner) { event ->
            val manageButton = view.findViewById<Button>(R.id.manageEventButton)

            if (event != null) {
                val eventTitle = "🏁 ${event.name}"
                val eventInfoDate = "📅 ${event.date}"

                // Получаем участников
                viewModel.getParticipantsForEvent(event.id)
                    .observe(viewLifecycleOwner) { participants ->
                        val count = participants.size
                        val max = event.maxParticipants
                        val eventInfoPart = "👥 $count / $max"

                        view.findViewById<TextView>(R.id.eventInfoPart).text = eventInfoPart
                    }

                view.findViewById<TextView>(R.id.eventTitle).text = eventTitle
                view.findViewById<TextView>(R.id.eventInfoDate).text = eventInfoDate


                manageButton.setOnClickListener {
                    val fragment = EditEventFragment().apply {
                        arguments = Bundle().apply {
                            putInt("event_id", event.id)
                        }
                    }
                    (activity as? MainActivity)?.openFragment(fragment)
                }
            } else {
                view.findViewById<TextView>(R.id.eventTitle).text = "🏁 Create new Event"
                view.findViewById<TextView>(R.id.eventInfoPart).visibility = View.GONE
                view.findViewById<TextView>(R.id.eventInfoDate).visibility = View.GONE

                manageButton.setOnClickListener {
                    (activity as? MainActivity)?.openFragment(CreateEventFragment())
                }
            }
        }

        // view CREATE EVENT
        val card1 = view.findViewById<CardView>(R.id.card1)
        DebugLogger.d("TAG", "card1View is $card1")
        val title1 = card1.findViewById<TextView>(R.id.title)
        val subtitle1 = card1.findViewById<TextView>(R.id.subtitle)
        val img1 = card1.findViewById<ShapeableImageView>(R.id.imageCard)

        title1.text = "Create Event"
        subtitle1.text = "Set up a new offline event"
        img1.setImageResource(R.drawable.dash_event_1)

        val clickContainer1 = card1.findViewById<View>(R.id.clickContainer)
        clickContainer1.setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateEventFragment())
            (activity as? MainActivity)?.updateNavIcons("events")
        }

        // view MY EVENTS
        val card2 = view.findViewById<CardView>(R.id.card2)
        val title2 = card2.findViewById<TextView>(R.id.title)
        val subtitle2 = card2.findViewById<TextView>(R.id.subtitle)
        val img2 = card2.findViewById<ShapeableImageView>(R.id.imageCard)

        title2.text = "My Events"
        subtitle2.text = "View and manage saved events"
        img2.setImageResource(R.drawable.dash_event_2)

        val clickContainer2 = card2.findViewById<View>(R.id.clickContainer)
        clickContainer2.setOnClickListener {
            (activity as? MainActivity)?.openFragment(AllEventsFragment())
            (activity as? MainActivity)?.updateNavIcons("events")
        }

        // manage TEAMS
        val card3 = view.findViewById<CardView>(R.id.card3)
        val title3 = card3.findViewById<TextView>(R.id.title)
        val subtitle3 = card3.findViewById<TextView>(R.id.subtitle)
        val img3 = card3.findViewById<ShapeableImageView>(R.id.imageCard)

        title3.text = "Manage Team"
        subtitle3.text = "Build and edit your teams"
        img3.setImageResource(R.drawable.dash_team)

        val clickContainer3 = card3.findViewById<View>(R.id.clickContainer)
        clickContainer3.setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateTeamFragment())
            (activity as? MainActivity)?.updateNavIcons("teams")
        }

        // view RESULTS
        val card4 = view.findViewById<CardView>(R.id.card4)
        val title4 = card4.findViewById<TextView>(R.id.title)
        val subtitle4 = card4.findViewById<TextView>(R.id.subtitle)
        val img4 = card4.findViewById<ShapeableImageView>(R.id.imageCard)

        title4.text = "View Results"
        subtitle4.text = "Browse event outcomes"
        img4.setImageResource(R.drawable.dash_result)

        val clickContainer4 = card4.findViewById<View>(R.id.clickContainer)
        clickContainer4.setOnClickListener {
            (activity as? MainActivity)?.openFragment(ResultsFragment())

        }
    }

    private fun setupSliderDots(count: Int) {
        sliderDots.removeAllViews()
        for (i in 0 until count) {
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.dot_inactive) // создаёшь drawable
            }
            sliderDots.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in 0 until sliderDots.childCount) {
            val dot = sliderDots.getChildAt(i)
            dot.setBackgroundResource(
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }
}
