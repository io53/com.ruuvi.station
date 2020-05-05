package com.ruuvi.station.feature

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.text.SpannableString
import android.text.style.SuperscriptSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ruuvi.station.R
import com.ruuvi.station.database.RuuviTagRepository
import com.ruuvi.station.model.RuuviTagEntity
import com.ruuvi.station.util.GraphView
import com.ruuvi.station.util.Utils
import timber.log.Timber

class TagDetailsPagerAdapter constructor(var tags: List<RuuviTagEntity>, val context: Context, val view: View) : PagerAdapter() {
    val VIEW_TAG = "DetailedTag"

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.view_tag_detail, container, false)
        view.tag = VIEW_TAG + position
        container.addView(view, 0)
        updateView(tags[position], false)
        return view
    }

    fun updateView(tagToUpdate: RuuviTagEntity, showGraph: Boolean) {
        Timber.d("tag = ${tagToUpdate.id} showGraph = $showGraph")
        var position = -1
        for ((index, tag) in tags.withIndex()) {
            if (tagToUpdate.id.equals(tag.id)) {
                position = index
                break
            }
        }
        if (position == -1) return

        val rootView = view.findViewWithTag<View>(VIEW_TAG + position) ?: return

        val graph = rootView.findViewById<View>(R.id.tag_graphs)
        val container = rootView.findViewById<View>(R.id.tag_container)
        if (showGraph) {
            if (graph.visibility == View.INVISIBLE) {
                graph.visibility = View.VISIBLE
                container.visibility = View.INVISIBLE
            }
            tagToUpdate.id?.let {
                GraphView(context).drawChart(it, rootView)
            }
        } else {
            if (graph.visibility == View.VISIBLE) {
                graph.visibility = View.INVISIBLE
                container.visibility = View.VISIBLE
            }
            updateDashboard(tagToUpdate, rootView)
        }
    }

    private fun updateDashboard(tag: RuuviTagEntity, view: View) {
        val tagTemperatureView = view.findViewById<TextView>(R.id.tag_temp)
        val tagHumidityView = view.findViewById<TextView>(R.id.tag_humidity)
        val tagPressureView = view.findViewById<TextView>(R.id.tag_pressure)
        val tagSignalView = view.findViewById<TextView>(R.id.tag_signal)
        val tagUpdatedView = view.findViewById<TextView>(R.id.tag_updated)
        val tagTemperatureUnitView = view.findViewById<TextView>(R.id.tag_temp_unit)

        var temperature = RuuviTagRepository.getTemperatureString(context, tag)
        val offset = if (temperature.endsWith("K")) 1 else 2
        val unit = temperature.substring(temperature.length - offset, temperature.length)
        temperature = temperature.substring(0, temperature.length - offset)

        val unitSpan = SpannableString(unit)
        unitSpan.setSpan(SuperscriptSpan(), 0, unit.length, 0)

        tagTemperatureUnitView.text = unitSpan
        tagTemperatureView.text = temperature
        tagHumidityView.text = RuuviTagRepository.getHumidityString(context, tag)
        tagPressureView.text = String.format(context.getString(R.string.pressure_reading), tag.pressure / 100.0)
        tagSignalView.text = String.format(context.getString(R.string.signal_reading), tag.rssi)
        val updatedAt = context.resources.getString(R.string.updated) + " " + Utils.strDescribingTimeSince(tag.updateAt)
        tagUpdatedView.text = updatedAt
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getPageTitle(position: Int): String {
        return tags.get(position).displayName?.toUpperCase().orEmpty()
    }

    override fun getCount(): Int {
        return tags.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View);
    }
}