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

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.ghosts.of.history.R

/** A DialogFragment for the Save Anchor Dialog Box.  */
class HostDialogFragment : DialogFragment() {
    fun interface OkListener {
        /**
         * This method is called by the dialog box when its OK button is pressed.
         *
         * @param dialogValue the long value from the dialog box
         */
        fun onOkPressed(dialogValue: String)
    }

    private var okListener: OkListener? = null
    fun setOkListener(okListener: OkListener?) {
        this.okListener = okListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val defaultNickname = requireArguments().getString("nickname")
        val activity = checkNotNull(activity) { "The activity cannot be null." }
        val builder = AlertDialog.Builder(activity)

        // Passing null as the root is fine, because the view is for a dialog.
        val dialogView = activity.layoutInflater.inflate(R.layout.save_anchor_dialog, null)
        val nicknameField: EditText = dialogView.findViewById(R.id.nickname_edit_text)
        nicknameField.setText(defaultNickname)
        builder.setView(dialogView)
                .setTitle(R.string.nickname_title_text)
                .setPositiveButton(R.string.nickname_dialog_ok) { _: DialogInterface?, _: Int ->
                    val nicknameText = nicknameField.text
                    okListener?.onOkPressed(nicknameText.toString())
                }
        return builder.create()
    }
}