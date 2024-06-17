package com.vladima.socialhub.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.vladima.socialhub.R

class FavoriteToggle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {

    private val toggleText: TextView by lazy { findViewById(R.id.toggle_text) }
    private val toggleButton: ToggleButton by lazy { findViewById(R.id.toggle_btn) }
    private val rootLayout: ConstraintLayout by lazy { findViewById(R.id.root_layout) }
    private var onCheckedListener: ((Boolean) -> Unit)? = null
    var isChecked: Boolean
        get() = toggleButton.isChecked
        set(value) {
            toggleButton.isChecked = value
            onChanged()
        }
    init {
        inflate(context, R.layout.like_toggle, this)
        rootLayout.setOnClickListener {
            toggleButton.isChecked = !toggleButton.isChecked
            onCheckedListener?.invoke(toggleButton.isChecked)
            onChanged()
        }
        toggleButton.setOnClickListener {
            onCheckedListener?.invoke(toggleButton.isChecked)
            onChanged()
        }
    }

    private fun onChanged() {
        toggleText.text = if (toggleButton.isChecked) {
            context.getString(R.string.remove_from_favorites)
        } else {
            context.getString(R.string.add_to_favorites)
        }
    }

    fun setOnCheckedListener(listener: (Boolean) -> Unit) {
        onCheckedListener = listener
    }
}