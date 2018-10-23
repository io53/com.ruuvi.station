package com.ruuvi.station.util

import android.content.Context
import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.ruuvi.station.R
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.model.TagSensorReading
import java.text.SimpleDateFormat
import java.util.*

class GraphView (val context: Context) {
    private var from: Long = 0
    private var to: Long = 0

    fun drawChart( tagId: String, view: View) {
        val readings = TagSensorReading.getForTag(tagId)
        if (readings.size == 0) return

        val tempData: MutableList<Entry> = ArrayList()
        val humidData: MutableList<Entry> = ArrayList()
        val pressureData: MutableList<Entry> = ArrayList()

        val tempUnit = RuuviTag.getTemperatureUnit(context)

        val cal = Calendar.getInstance()
        to = cal.time.time;
        cal.add(Calendar.HOUR, -24)
        from = cal.time.time

        readings.map { reading ->
            val timestamp = (reading.createdAt.time - from).toFloat()
            if (tempUnit.equals("C")) tempData.add(Entry(timestamp, reading.temperature.toFloat()))
            else tempData.add(Entry(timestamp, Utils.celciusToFahrenheit(reading.temperature).toFloat()))
            humidData.add(Entry(timestamp, reading.humidity.toFloat()))
            pressureData.add(Entry(timestamp, reading.pressure.toFloat()))
        }

        addDataToChart(tempData, view.findViewById(R.id.tempChart), "Temperature")
        addDataToChart(humidData, view.findViewById(R.id.humidChart), "Humidity")
        addDataToChart(pressureData, view.findViewById(R.id.pressureChart), "Pressure")
    }

    fun addDataToChart(data: MutableList<Entry>, chart: LineChart, label: String) {
        val set = LineDataSet(data, label)
        set.setDrawValues(false)
        set.setDrawFilled(true)
        set.highLightColor = context.resources.getColor(R.color.main)
        set.circleRadius = (2).toFloat()
        chart.xAxis.axisMaximum = (to - from).toFloat()
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.textColor = Color.WHITE
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.getAxis(YAxis.AxisDependency.LEFT).textColor = Color.WHITE
        chart.getAxis(YAxis.AxisDependency.RIGHT).setDrawLabels(false)
        chart.description.text = label
        chart.description.textColor = Color.WHITE
        chart.description.textSize = context.resources.getDimension(R.dimen.graph_description_size)
        chart.setNoDataTextColor(Color.WHITE)
        try {
            chart.description.typeface = ResourcesCompat.getFont(context, R.font.montserrat)
        } catch (e: Exception) { /* ¯\_(ツ)_/¯ */ }
        chart.legend.isEnabled = false
        chart.data = LineData(set)

        chart.xAxis.valueFormatter = object : IAxisValueFormatter {
            private val mFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return mFormat.format(Date(value.toLong() + from))
            }
        }

        chart.notifyDataSetChanged()
        chart.invalidate()
    }
}