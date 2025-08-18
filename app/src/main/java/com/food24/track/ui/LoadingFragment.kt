package com.food24.track.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.food24.track.MainActivity
import com.food24.track.R

class LoadingFragment : Fragment() {

    private var loadingCircleView: LoadingCircleView? = null
    private var progressAnimator: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.loading_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingCircleView = view.findViewById(R.id.loadingCircle)

        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 5000
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Int
                loadingCircleView?.setProgress(progress)

                if (progress == 100) {
                    view?.postDelayed({
                        if (isAdded) navigateToMainScreen()
                    }, 500)
                }
            }
            start()
        }
    }

    private fun navigateToMainScreen() {
        (activity as? MainActivity)?.openMainFragment()
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.mainFragmentContainer, MainFragment())
//            .commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        progressAnimator?.cancel()
        progressAnimator = null
        super.onDestroyView()
    }
}
