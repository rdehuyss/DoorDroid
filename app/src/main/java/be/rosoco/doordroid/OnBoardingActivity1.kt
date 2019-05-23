package be.rosoco.doordroid


import android.content.Intent
import android.net.sip.SipManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick


class OnBoardingActivity1 : AppCompatActivity() {

    @BindView(R.id.textView2)
    lateinit var textView: TextView

    @BindView(R.id.button_next)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_1)
        ButterKnife.bind(this)

        if(!isSIPSupported()) {
            textView.setText(R.string.onboarding_intro_no_sip)
            button.visibility = View.INVISIBLE
        }

    }

    @OnClick(R.id.button_next)
    fun next() {
        startActivity(Intent(this, OnBoardingActivity2::class.java))
    }

    fun isSIPSupported(): Boolean {
        return SipManager.isVoipSupported(this) && SipManager.isApiSupported(this)
    }
}