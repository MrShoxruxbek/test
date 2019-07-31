package com.example.myapplicatio.calendar

import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.myapplicatio.R
import com.example.myapplicatio.common.base.app.BaseFragment
import com.example.myapplicatio.fragment.ScheduleFragment
import com.example.myapplicatio.memos.MemoContentFragment
import com.example.myapplicatio.util.ToastUtils
import com.example.myapplicatio.voice_record.RecordActiv
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.main_container.*
import uz.greenwhite.lib.mold.Mold
import uz.greenwhite.lib.mold.MoldContentFragment
import java.util.*
import kotlin.collections.ArrayList

class DashboardFragment : MoldContentFragment(), View.OnClickListener {

    companion object {
        fun newInstance(): MoldContentFragment {
            var s = Bundle()
            s.putString("D", "d")
            return Mold.parcelableArgumentNewInstance(DashboardFragment::class.java, s)
        }
    }

    private var doubleBackToExitPressedOnce: Long = 0
    private var mMonthText: Array<String>? = null
    private var mCurrentSelectYear: Int? = null
    private var mCurrentSelectMonth: Int? = null
    private var mCurrentSelectDay: Int? = null
    private var fabExpanded: Boolean = true
    private var auth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.f_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        addReminder()
        gotoSchedule()
        closeFabMenu()
        hasItemDivider()
        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)
        fab3.setOnClickListener(this)
        fabSetting.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab1 -> {
                Mold.replaceContent(activity, MemoContentFragment.newInstance())
                closeFabMenu()
            }
            R.id.fab2 -> {
                dialog = ProgressDialog.show(context, "Registrate", "Wait", true, true)
                dialog!!.create()
                auth = FirebaseAuth.getInstance()
                auth!!.createUserWithEmailAndPassword("warlocked@iclod.com", "123456789").addOnCompleteListener(activity!!,
                        OnCompleteListener<AuthResult> {
                            if (it.isSuccessful) {
                                ToastUtils.showShortToast(context, "Registreted")
                            } else {
                                ToastUtils.showShortToast(context, "NOt worked")
                            }
                            dialog!!.dismiss()
                        })
            }
            R.id.fab3 -> {
                Mold.openContent(RecordActiv::class.java)  //            Mold.openContent(RecordFile::class.java)
                closeFabMenu()
                Toast.makeText(context, "Floating Action Button", Toast.LENGTH_SHORT).show()
            }
            R.id.fabSetting -> {
                if (fabExpanded) {
                    closeFabMenu()
                } else {
                    openFabMenu()
                }
            }
        }
    }

    fun initUI() {
        mMonthText = resources.getStringArray(R.array.calendar_month)

        var cl = Calendar.getInstance()
        mCurrentSelectYear = cl.get(Calendar.YEAR)
        mCurrentSelectMonth = cl.get(Calendar.MONTH)
        mCurrentSelectDay = cl.get(Calendar.DAY_OF_MONTH)
    }

    fun hasItemDivider(): Boolean {
        return false
    }

    fun setCurrentSelectDate(year: Int, month: Int, day: Int) {
        mCurrentSelectDay = day
        mCurrentSelectMonth = month
        mCurrentSelectYear = year
    }

    fun gotoSchedule() {
        var tr = childFragmentManager.beginTransaction()
        tr.setTransition(FragmentTransaction.TRANSIT_NONE)
        var mScheduleFragment: BaseFragment? = null
        if (mScheduleFragment == null) {
            mScheduleFragment = ScheduleFragment.getInstance()
            tr.add(R.id.flMainContainer, mScheduleFragment)
        }

        var eventSetFragment: BaseFragment? = null
        if (eventSetFragment != null)
            tr.hide(eventSetFragment)
        tr.show(mScheduleFragment)
        tr.commit()
    }

    fun addReminder() {
        var adapter = RecyclerReminder(context!!, ArrayList<String>(), object : RecyclerReminder.ItemClickListener {
            override fun onItemClick(position: String) {
                Toast.makeText(context, position, Toast.LENGTH_SHORT).show()
            }
        })

        ed_reminder.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!TextUtils.isEmpty(ed_reminder.text)) {
                    adapter.addItem(ed_reminder.text.toString())
                    ed_reminder.setText("")
                    return@OnKeyListener true
                }
            }
            return@OnKeyListener false
        })

        iv_reminder.setOnClickListener {
            if (!TextUtils.isEmpty(ed_reminder.text)) {
                adapter.addItem(ed_reminder.text.toString())
                ed_reminder.setText("")
            }
        }

        rc_reminder.layoutManager = LinearLayoutManager(context)
        rc_reminder.adapter = adapter
    }

    fun closeFabMenu() {
        stateFab(false)
        fabSetting.setImageResource(R.drawable.ic_add)
    }

    fun openFabMenu() {
        stateFab(true)
        fabSetting.setImageResource(R.drawable.ic_multiply)
    }

    fun stateFab(state: Boolean) {
        var v = if (state) View.VISIBLE else View.INVISIBLE
        fab1.visibility = v
        fab2.visibility = v
        fab3.visibility = v
        fabExpanded = state
    }

    override fun onBackPressed(): Boolean {
        if (doubleBackToExitPressedOnce + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return false
        } else ToastUtils.showLongToast(context, "Press once again to exit!")
        doubleBackToExitPressedOnce = System.currentTimeMillis()
        return true
    }
}