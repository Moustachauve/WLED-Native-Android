package ca.cgagnier.wlednativeandroid.fragment

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

abstract class WiderDialogFragment : DialogFragment() {

    private var isLargeLayout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isLargeLayout = it.getBoolean(IS_LARGE_LAYOUT)
        }
    }

    override fun onResume() {
        super.onResume()
        var width = WindowManager.LayoutParams.MATCH_PARENT
        if (isLargeLayout) {
            width = (getScreenWidth() * 0.70).toInt()
        }
        val window = dialog!!.window!!
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
    }

    private fun getScreenWidth(): Int {
        val windowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val windowInsets: WindowInsets = windowMetrics.windowInsets

            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
            )
            val insetsWidth = insets.right + insets.left

            val b = windowMetrics.bounds
            width = b.width() - insetsWidth
        } else {
            val size = Point()
            // This branch is only to support old devices, so deprecation is fine.
            @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display?.getSize(size)
            width = size.x
        }

        return width
    }

    companion object {
        private const val TAG = "WiderDialogFragment"
        private const val IS_LARGE_LAYOUT = "is_large_layout"

        @JvmStatic
        fun newInstance(isLargeLayout: Boolean) =
            DeviceEditFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_LARGE_LAYOUT, isLargeLayout)
                }
            }
    }
}