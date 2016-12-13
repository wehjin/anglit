package com.rubyhuntersky.angleedit.app

import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.rubyhuntersky.angleedit.app.tools.BaseParcelable
import com.rubyhuntersky.angleedit.app.tools.read
import com.rubyhuntersky.angleedit.app.tools.toViewIntent
import com.rubyhuntersky.angleedit.app.tools.write
import kotlinx.android.synthetic.main.cell_element_attribute.view.*
import kotlinx.android.synthetic.main.fragment_element_details.*

/**
 * @author Jeffrey Yu
 * @since 12/7/16.
 */

class ElementDetailDialogFragment : BottomSheetDialogFragment() {

    val model: Model get() = arguments.getParcelable(MODEL_KEY)

    override fun onStart() {
        super.onStart()
        elementTitleView.text = model.title
        elementBodyView.text = model.description
        elementBodyView.visibility = if (model.description.isNullOrEmpty()) GONE else VISIBLE

        elementAttributes.removeAllViews()
        val attributesList = model.attributes.toList()
        attributesList.forEach {
            val attributeView = activity.layoutInflater.inflate(R.layout.cell_element_attribute, elementAttributes, false)
            attributeView.elementAttributeKey.text = it.first
            attributeView.elementAttributeValue.text = it.second
            elementAttributes.addView(attributeView, MATCH_PARENT, WRAP_CONTENT)
        }
        elementAttributes.visibility = if (elementAttributes.childCount > 0) VISIBLE else GONE

        var elementButtonCount = 0
        val viewIntent = model.description?.toViewIntent
        if (viewIntent == null) {
            elementLinkButton.visibility = GONE
        } else {
            elementButtonCount++
            elementLinkButton.visibility = VISIBLE
            elementLinkButton.setOnClickListener {
                dismiss()
                startActivity(viewIntent)
            }
        }
        elementButtons.visibility = if (elementButtonCount > 0) VISIBLE else GONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_element_details, container, false)

    data class Model(val title: String?, val description: String?, val attributes: Map<String, String>) : BaseParcelable {

        override fun writeToParcel(dest: Parcel, flags: Int) {
            val attributesBundle = Bundle(attributes.size)
            attributes.entries.forEach { attributesBundle.putString(it.key, it.value) }
            dest.write(title, description, attributesBundle)
        }

        companion object {
            @JvmField val CREATOR = BaseParcelable.generateCreator {
                Model(it.read(), it.read(), it.read<Bundle>().toAttributes)
            }

            val Bundle.toAttributes: Map<String, String> get() {
                val map = mutableMapOf<String, String>()
                this.keySet().forEach { map.put(it, getString(it)) }
                return map
            }
        }
    }

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