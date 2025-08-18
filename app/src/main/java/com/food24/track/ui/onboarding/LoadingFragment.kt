package com.food24.track.ui.onboarding

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.food24.track.R

class LoadingFragment : Fragment() {
    private var loadingCircleView: LoadingCircleView? = null
    private var progressAnimator: ValueAnimator? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        return i.inflate(R.layout.loading_screen, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingCircleView = view.findViewById(R.id.loadingCircle)
        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 1000 // анимация круга ~1s (не критично)
            addUpdateListener { a ->
                loadingCircleView?.setProgress(a.animatedValue as Int)
            }
            start()
        }
    }

    override fun onDestroyView() {
        progressAnimator?.cancel()
        progressAnimator = null
        super.onDestroyView()
    }
}
