/*
 * Copyright 2014 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.muzei.bingimageoftheday

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, SettingsFragment())
                    .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_OK)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : Fragment() {

        private lateinit var rgOrientation: RadioGroup
        private lateinit var spMarket: Spinner
        private lateinit var cbAutoMarket: CheckBox
        private lateinit var marketAdapter: ArrayAdapter<BingMarket>
        private lateinit var btnLicense: Button
        private lateinit var settings: Settings
        private var spCurrentSelected: Int = 0

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            settings = Settings(activity)
            val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
            rgOrientation = rootView.findViewById(R.id.fragment_settings_orientation) as RadioGroup
            spMarket = rootView.findViewById(R.id.fragment_settings_market) as Spinner
            cbAutoMarket = rootView.findViewById(R.id.fragment_settings_automarket) as CheckBox
            marketAdapter = MarketAdapter(activity, R.layout.settings_ab_spinner_list_item_dropdown, BingMarket.selectableValues())
            spMarket.adapter = marketAdapter
            btnLicense = rootView.findViewById(R.id.fragment_settings_button_license) as Button

            rgOrientation.check(if (settings.isOrientationPortrait) R.id.fragment_settings_orientation_portrait else R.id.fragment_settings_orientation_landscape)
            cbAutoMarket.isChecked = settings.isAutoMarket
            spMarket.isEnabled = !settings.isAutoMarket
            spCurrentSelected = getMarketSpinnerSelection()

            rgOrientation.setOnCheckedChangeListener { _, id ->
                when (id) {
                    R.id.fragment_settings_orientation_portrait -> {
                        settings.isOrientationPortrait = true
                    }
                    R.id.fragment_settings_orientation_landscape -> {
                        settings.isOrientationPortrait = false
                    }
                    else -> return@setOnCheckedChangeListener
                }
                Intent(activity, BingImageOfTheDayUpdateReceiver::class.java).also { intent ->
                    activity.sendBroadcast(intent)
                }
            }

            cbAutoMarket.setOnCheckedChangeListener { _, checked ->
                settings.isAutoMarket = checked
                spMarket.isEnabled = !checked
                if (checked) {
                    if (settings.bingMarket.marketCode != settings.marketCode) {
                        spMarket.setSelection(getMarketSpinnerSelection())
                    }
                } else {
                    settings.marketCode = marketAdapter.getItem(spCurrentSelected)!!.marketCode
                }
            }

            spMarket.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                    if (spCurrentSelected != i) {
                        val market = marketAdapter.getItem(i) ?: settings.bingMarket
                        settings.marketCode = market.marketCode
                        Intent(activity, BingImageOfTheDayUpdateReceiver::class.java).also { intent ->
                            activity.sendBroadcast(intent)
                        }
                        spCurrentSelected = i
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {

                }
            }

            btnLicense.setOnClickListener {
                val licenseActivityIntent = Intent(activity, LicenseInfoActivity::class.java)
                startActivity(licenseActivityIntent)
            }

            return rootView
        }

        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)
            spMarket.setSelection(spCurrentSelected)
        }

        private fun getMarketSpinnerSelection(): Int {
            val marketCode = settings.bingMarket.marketCode
            return (0 until marketAdapter.count)
                    .firstOrNull { marketAdapter.getItem(it)!!.marketCode == marketCode }
                    ?: 0
        }
    }

    internal class MarketAdapter(context: Context, resource: Int, objects: Array<BingMarket>) : ArrayAdapter<BingMarket>(context, resource, objects) {

        internal class ViewHolder {
            lateinit var textView: TextView
            lateinit var imageView: ImageView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }

        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var effectiveConvertView = convertView

            val holder: ViewHolder
            if (effectiveConvertView == null) {
                effectiveConvertView = LayoutInflater.from(context).inflate(R.layout.settings_ab_spinner_list_item_dropdown, null)
                holder = ViewHolder()
                holder.imageView = effectiveConvertView.findViewById(R.id.settings_ab_spinner_list_item_dropdown_icon) as ImageView
                holder.textView = effectiveConvertView.findViewById(R.id.settings_ab_spinner_list_item_dropdown_text) as TextView
                effectiveConvertView.tag = holder
            } else {
                holder = effectiveConvertView.tag as ViewHolder
            }

            val market = getItem(position)

            holder.textView.text = market!!.toString()
            holder.imageView.setImageResource(market.logoResourceId)

            return effectiveConvertView!!
        }
    }
}
