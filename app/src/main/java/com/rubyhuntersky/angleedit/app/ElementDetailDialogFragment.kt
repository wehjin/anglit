package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.rubyhuntersky.angleedit.app.tools.BaseParcelable
import com.rubyhuntersky.angleedit.app.tools.read
import com.rubyhuntersky.angleedit.app.tools.toViewIntent
import com.rubyhuntersky.angleedit.app.tools.write
import kotlinx.android.synthetic.main.fragment_element_details.*

/**
 * @author Jeffrey Yu
 * @since 12/7/16.
 */

class ElementDetailDialogFragment : BottomSheetDialogFragment() {

    data class Model(val title: String?, val description: String?) : BaseParcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) = dest.write(title, description)

        companion object {
            @JvmField val CREATOR = BaseParcelable.generateCreator { Model(it.read(), it.read()) }
        }
    }

    val model: Model get() = arguments.getParcelable(MODEL_KEY)

    override fun onStart() {
        super.onStart()
        elementTitleView.text = model.title
        elementBodyView.text = model.description

        val viewIntent = model.description?.toViewIntent
        if (viewIntent == null) {
            elementViewLinkButton.visibility = GONE
        } else {
            elementViewLinkButton.visibility = VISIBLE
            elementViewLinkButton.setOnClickListener {
                dismiss()
                startActivity(viewIntent)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_element_details, container, false)

    companion object {
        val TAG: String = ElementDetailDialogFragment::class.java.simpleName
        const val MODEL_KEY = "model"

        fun create(model: Model): ElementDetailDialogFragment {
            val fragment = ElementDetailDialogFragment()
            fragment.arguments = model.toArguments
            return fragment
        }

        val Model.toArguments: Bundle get() {
            val bundle = Bundle()
            bundle.putParcelable(MODEL_KEY, this)
            return bundle
        }
    }
}