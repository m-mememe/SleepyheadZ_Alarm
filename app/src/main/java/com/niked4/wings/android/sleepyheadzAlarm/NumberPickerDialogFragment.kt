package com.niked4.wings.android.sleepyheadzAlarm

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class NumberPickerDialogFragment : DialogFragment(){
    private var _count = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.fragment_number_picker_dialog, null)
        val numPicker = view?.findViewById<NumberPicker>(R.id.np_count)
        val count = arguments?.getInt("count")
        numPicker?.value = count ?: 5
        numPicker?.minValue = 2
        numPicker?.maxValue = 30
        numPicker?.setOnValueChangedListener(ValueChangeListener())

        val builder = AlertDialog.Builder(activity)
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
                    //TODO:numPicker?.value.toString()で返せるようにしたい
                    //TODO:現状では初期状態が現在の値ではなく強制的に2になる、値を動かさないと0が代入されてしまう点が問題
//                    val view = this@NumberPickerDialogFragment.view
//                    val numPicker = view?.findViewById<NumberPicker>(R.id.np_count)
                    val btSetCount = activity?.findViewById<Button>(R.id.bt_set_count)
                    btSetCount?.text = _count.toString()
//                    btSetCount?.text = numPicker?.value.toString()
                }
            }
        }
    }

    private inner class ValueChangeListener : NumberPicker.OnValueChangeListener{
        override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
            _count = newVal
        }
    }
}