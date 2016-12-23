package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.data.AccentCenter
import kotlinx.android.synthetic.main.cell_text_and_switch.view.*
import kotlinx.android.synthetic.main.fragment_taglist_details.view.*
import java.util.*

/**
 * @author Jeffrey Yu
 * @since 12/23/16.
 */

class TagListDetailsDialogFragment(tagList: List<String>) : BottomSheetDialogFragment() {

    init {
        arguments = Bundle(1)
        arguments.putStringArrayList("model", ArrayList(tagList))
    }

    val tagList: List<String> by lazy {
        arguments.getStringArrayList("model").toList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_taglist_details, container, false)
        view.accentStatusLayout.textView.text = getString(R.string.accented)
        view.accentStatusLayout.switchView.isChecked = AccentCenter.containsAccent(tagList)
        view.accentStatusLayout.switchView.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                AccentCenter.addAccent(tagList)
            } else {
                AccentCenter.removeAccent(tagList)
            }
        }

        view.pageTitleStatusLayout.textView.text = getString(R.string.page_title)
        return view
    }
}