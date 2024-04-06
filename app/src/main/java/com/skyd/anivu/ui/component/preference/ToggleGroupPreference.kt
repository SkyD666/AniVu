package com.skyd.anivu.ui.component.preference

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.skyd.anivu.R
import com.skyd.anivu.ext.dp
import kotlinx.parcelize.Parcelize

class ToggleGroupPreference : Preference {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, androidx.preference.R.attr.preferenceStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, R.style.Preference_Material3)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        layoutResource = R.layout.m3_preference_toggle_group
        onPreferenceClickListener = null
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.apply {
            isClickable = false
            isFocusable = false
        }

        val toggleGroup = holder.findViewById(R.id.toggleButton) as? MaterialButtonToggleGroup
        if (toggleGroup != null) {
            toggleGroup.isSingleSelection = isSingleSelection
            if (needSetButtons) {
                needSetButtons = false
                toggleGroup.removeAllViews()
                buttons.forEach { (tag, text) ->
                    toggleGroup.addView(
                        MaterialButton(
                            context,
                            null,
                            com.google.android.material.R.attr.materialButtonOutlinedStyle,
                        ).apply {
                            setPadding(5.dp)
                            this.tag = tag
                            this.text = text
                        },
                        LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                    )
                }
            }
            needCheckTag.removeIf { tag ->
                val id = toggleGroup.findViewWithTag<View>(tag)?.id
                if (id != null) toggleGroup.check(id)
                true
            }
            if (addOnButtonCheckedListener) {
                addOnButtonCheckedListener = false
                toggleGroup.clearOnButtonCheckedListeners()
                toggleGroup.addOnButtonCheckedListener { _, id, checked ->
                    if (checked) {
                        val button = toggleGroup.findViewById<View>(id) as MaterialButton
                        checkedButtonIds += id
                        callChangeListener(button.tag)
                    } else {
                        checkedButtonIds -= id
                    }
                }
            }
        }
    }

    private var addOnButtonCheckedListener = true
    private var needSetButtons = false
    var buttons: List<ButtonData> = listOf()
        set(value) {
            field = value
            needSetButtons = true
            notifyChanged()
        }

    var isSingleSelection: Boolean = true
        set(value) {
            field = value
            notifyChanged()
        }

    private val needCheckTag: MutableList<Any> = mutableListOf()
    fun check(tag: Any) {
        needCheckTag += tag
        notifyChanged()
    }

    fun check(tags: List<Any>) {
        needCheckTag.addAll(tags)
        notifyChanged()
    }

    data class ButtonData(
        val tag: Any,
        val text: String,
    )

    private var checkedButtonIds: MutableList<Int> = mutableListOf()

    override fun onDetached() {
        super.onDetached()
        checkedButtonIds.clear()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        return SavedState(
            superState = superState,
            checkedButtonIds = checkedButtonIds,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)

        myState.checkedButtonIds?.let { checkedButtonIds ->
            check(checkedButtonIds)
        }
    }

    @Parcelize
    data class SavedState(
        @JvmField
        val superState: Parcelable?,
        val checkedButtonIds: List<Int>? = null,
    ) : BaseSavedState(superState)
}