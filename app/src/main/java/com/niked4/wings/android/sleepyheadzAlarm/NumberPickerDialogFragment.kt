package com.niked4.wings.android.sleepyheadzAlarm

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class NumberPickerDialogFragment : DialogFragment(){
    private lateinit var numPicker: NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.fragment_number_picker_dialog, null)
        numPicker = view.findViewById(R.id.np_count)
        val count = arguments?.getInt("count")
        //min,maxの後にvalueをセットしないとならない
        numPicker.minValue = 2
        numPicker.maxValue = 30
        numPicker.value = count ?: 0

        val builder = AlertDialog.Builder(activity, R.style.DialogTheme)
            .setTitle(R.string.np_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.np_dialog_ok, DialogClickListener())
            .setNegativeButton(R.string.np_dialog_ng, DialogClickListener())
        val dialog = builder.create()
        return dialog
    }

    private inner class DialogClickListener : DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            when(which){
                DialogInterface.BUTTON_POSITIVE ->{
                    val btSetCount = activity?.findViewById<Button>(R.id.bt_set_count)
                    btSetCount?.text = numPicker.value.toString()
                }
            }
        }
    }
}