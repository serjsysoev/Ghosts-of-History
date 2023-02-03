/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghosts.of.history.persistentcloudanchor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.ghosts.of.history.R

/** Multi-select dropdown for selecting Anchors to resolve.  */
class MultiSelectItem(context: Context,
                      resource: Int,
                      private val anchorsList: List<AnchorItem>,
                      private val spinner: Spinner) :
        ArrayAdapter<AnchorItem>(context, resource, anchorsList) {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    // Adjust for the blank initial selection item.
    override fun getCount(): Int {
        return super.getCount() + 1
    }

    override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
        return getCustomView(position, view, parent)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        return getCustomView(position, view, parent)
    }

    internal class ViewHolder(val anchorName: TextView, val creationTime: TextView)

    // Creates a view if convertView is null, otherwise reuse cached view.
    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val resultView: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            resultView = layoutInflater.inflate(R.layout.anchor_item, null, false)
            viewHolder = ViewHolder(
                    resultView.findViewById<View>(R.id.anchor_name) as TextView,
                    resultView.findViewById<View>(R.id.creation_time) as TextView)
            resultView.tag = viewHolder
        } else {
            resultView = convertView
            viewHolder = convertView.tag as ViewHolder
        }
        if (position == 0) {
            viewHolder.anchorName.text = "    Select Ghosts"
            viewHolder.creationTime.text = ""
        } else {
            val anchorPosition = position - 1
            val text = SPACE + if (anchorsList[anchorPosition].isSelected) {
                CHECKED_BOX
            } else {
                UNCHECKED_BOX
            } + SPACE + anchorsList[anchorPosition].anchorName
            viewHolder.anchorName.text = text
            viewHolder.anchorName.tag = anchorPosition
            viewHolder.creationTime.text = anchorsList[anchorPosition].getMinutesSinceCreation()
            viewHolder.anchorName.setOnClickListener { v ->
                spinner.performClick()
                val getPosition = v.tag as Int
                anchorsList[getPosition].isSelected = !anchorsList[getPosition].isSelected
                notifyDataSetChanged()
            }
        }
        return resultView
    }

    companion object {
        private val TAG = MultiSelectItem::class.java.simpleName
        private const val CHECKED_BOX = "\u2611"
        private const val UNCHECKED_BOX = "\u2610"
        private const val SPACE = "    "
    }
}