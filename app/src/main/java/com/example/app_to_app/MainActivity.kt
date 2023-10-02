package com.example.app_to_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import com.example.app_to_app.ui.theme.ApptoappTheme
import com.nexmo.client.NexmoCall
import com.nexmo.client.NexmoCallEventListener
import com.nexmo.client.NexmoCallMemberStatus
import com.nexmo.client.NexmoClient
import com.nexmo.client.NexmoMediaActionState
import com.nexmo.client.NexmoMember
import com.nexmo.client.request_listener.NexmoApiError
import com.nexmo.client.request_listener.NexmoConnectionListener
import com.nexmo.client.request_listener.NexmoRequestListener
import java.lang.Error

class MainActivity : ComponentActivity() {

    private lateinit var connectionStatusTextView: TextView
    private lateinit var waitingForIncomingCallTextView: TextView
    private lateinit var loginAsAlice: Button
    private lateinit var loginAsBob: Button
    private lateinit var startCallButton: Button
    private lateinit var answerCallButton: Button
    private lateinit var rejectCallButton: Button
    private lateinit var endCallButton: Button

    private lateinit var client: NexmoClient
    private var otherUser: String = ""

    private var onGoingCall: NexmoCall? = null

    private lateinit var callListener: NexmoCallEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val callsPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(this, callsPermissions, 123)

        connectionStatusTextView = findViewById(R.id.connectionStatusTextView)
        waitingForIncomingCallTextView = findViewById(R.id.waitingForIncomingCallTextView)
        loginAsAlice = findViewById(R.id.loginAsAlice)
        loginAsBob = findViewById(R.id.loginAsBob)
        startCallButton = findViewById(R.id.startCallButton)
        answerCallButton = findViewById(R.id.answerCallButton)
        rejectCallButton = findViewById(R.id.rejectCallButton)
        endCallButton = findViewById(R.id.endCallButton)

        startCallButton.setOnClickListener { startCall() }

        loginAsAlice.setOnClickListener { loginAsAlice() }
        loginAsBob.setOnClickListener { loginAsBob () }

        answerCallButton.setOnClickListener { answerCall() }
        rejectCallButton.setOnClickListener { rejectCall() }
        endCallButton.setOnClickListener { endCall() }

        client = NexmoClient.Builder().build(this)
        client.setConnectionListener { connectionStatus, _ ->
            runOnUiThread { connectionStatusTextView.text = connectionStatus.toString() }
            if (connectionStatus == NexmoConnectionListener.ConnectionStatus.CONNECTED){
                runOnUiThread{
                    hideUI()
                    connectionStatusTextView.visibility = View.VISIBLE
                    startCallButton.visibility = View.VISIBLE
                    waitingForIncomingCallTextView.visibility = View.VISIBLE
                }
                return@setConnectionListener
            }
        }

        client.addIncomingCallListener {
            onGoingCall = it

            runOnUiThread{
                hideUI()
                answerCallButton.visibility = View.VISIBLE
                rejectCallButton.visibility = View.VISIBLE
            }
        }
//        setContent {
//            ApptoappTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }
        callListener = object : NexmoCallEventListener{
            override fun onMemberStatusUpdated(
                newState: NexmoCallMemberStatus?,
                member: NexmoMember?
            ){
                if (newState == NexmoCallMemberStatus.COMPLETED ||
                        newState == NexmoCallMemberStatus.CANCELLED){
                    onGoingCall = null
                    runOnUiThread{
                        hideUI()
                        startCallButton.visibility = View.VISIBLE
                        waitingForIncomingCallTextView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onMuteChanged(newState: NexmoMediaActionState?, member: NexmoMember?) {
                TODO("Not yet implemented")
            }

            override fun onEarmuffChanged(newState: NexmoMediaActionState?, member: NexmoMember?) {
                TODO("Not yet implemented")
            }

            override fun onDTMF(dtmf: String?, member: NexmoMember?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun hideUI() {
        val content = findViewById<LinearLayout>(R.id.content)
        content.forEach { it.visibility = View.GONE}
    }

    private fun loginAsAlice() {
        otherUser = "Bob"
        client.login("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2OTYwNTk5NjUsImp0aSI6IjY5MjJmYmEwLTVmNjUtMTFlZS1hMGQ1LTVkZDZkN2IwNWY2MSIsImFwcGxpY2F0aW9uX2lkIjoiYTliMzhjNzItMDZmZC00ODc5LWIwNmItZWU2NjRjMjY2M2UzIiwic3ViIjoiQWxpY2UiLCJleHAiOjE2OTYwNTk5ODcxMzAsImFjbCI6eyJwYXRocyI6eyIvKi91c2Vycy8qKiI6e30sIi8qL2NvbnZlcnNhdGlvbnMvKioiOnt9LCIvKi9zZXNzaW9ucy8qKiI6e30sIi8qL2RldmljZXMvKioiOnt9LCIvKi9pbWFnZS8qKiI6e30sIi8qL21lZGlhLyoqIjp7fSwiLyovYXBwbGljYXRpb25zLyoqIjp7fSwiLyovcHVzaC8qKiI6e30sIi8qL2tub2NraW5nLyoqIjp7fSwiLyovbGVncy8qKiI6e319fX0.I2vK8vBt_am7b_ETypJ0f3m0XeAOUVE15A_cydJTA0eeete9cJg9eWAW0dfUpOS-A0IVUqBkyd8CmG2CyrQmRm8EpgMyFc0EXYsGnQPJd9vieH0d85TNs08swXkj4q_OEeservmr2F0w92sTTNkxi40ROk_66jg_nC3NVi_XNVFVIVjhgpelDHE_4Y4OFYP6Fy-apxJ61v0Jx6_UaLoXpnFm0erQG7gTx0yv6Gobj9DIkffDwhn_3L1bdTmkNFHVPWhoS2HWhPMHVHuCYQcqfMYBgKd94cvsZdotMmWxRIK1AHl-1Agna94hzuovclauRax9eECULBhZllgWXd-QuQ")
    }

    private fun loginAsBob() {
        otherUser = "Alice"
        client.login("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2OTYwNTk5NzcsImp0aSI6IjcwMDM4YWMwLTVmNjUtMTFlZS1hNWJhLTI3ZjA5Njk2ZjAxNCIsImFwcGxpY2F0aW9uX2lkIjoiYTliMzhjNzItMDZmZC00ODc5LWIwNmItZWU2NjRjMjY2M2UzIiwic3ViIjoiQm9iIiwiZXhwIjoxNjk2MDU5OTk4NjY4LCJhY2wiOnsicGF0aHMiOnsiLyovdXNlcnMvKioiOnt9LCIvKi9jb252ZXJzYXRpb25zLyoqIjp7fSwiLyovc2Vzc2lvbnMvKioiOnt9LCIvKi9kZXZpY2VzLyoqIjp7fSwiLyovaW1hZ2UvKioiOnt9LCIvKi9tZWRpYS8qKiI6e30sIi8qL2FwcGxpY2F0aW9ucy8qKiI6e30sIi8qL3B1c2gvKioiOnt9LCIvKi9rbm9ja2luZy8qKiI6e30sIi8qL2xlZ3MvKioiOnt9fX19.R-IEmBGUr7x7IrSJ933CQNdaJEItl97Y-kTIOPD1iPuS91ojbmHKuSpNdovMGwSbPRX7-HgKcpVDvwezq1AbD-Y2SuXfmAY9G7gPAhSr6WkuV04KUg1PUqCjqV15_4dXNOthw_20KKftEZfdd94ks8OKSqg5EV684TDFVyKrRYAmqfM5ZsbLw6w0PIwhAuArND7Scgk6G4xmRnFtEmoHVPwVQDVfkW98ickhaFCyi1HkUOy0JTdY-F62QhWuAFRn_zJC4LiBwqUsHWSKAKYOJ0y7yOQ6WEn2VFefBqcKRo47wzRqOp8cWIa7tBN-ugjXe6IZI__3LEwqWiBnPR6VfQ")
    }

    @SuppressLint("MissingPermission")
    private fun answerCall() {
        onGoingCall?.answer(object : NexmoRequestListener<NexmoCall>{
            override fun onError(error: NexmoApiError){
                TODO("Not yet implemented")
            }

            override fun onSuccess(result: NexmoCall?) {
                onGoingCall?.addCallEventListener(callListener)
                runOnUiThread{
                    hideUI()
                    endCallButton.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun rejectCall(){
        onGoingCall?.hangup(object : NexmoRequestListener<NexmoCall> {
            override fun onError(error: NexmoApiError){
                TODO("Not yet implemented")
            }

            override fun onSuccess(result : NexmoCall?) {
                runOnUiThread {
                    hideUI()
                    startCallButton.visibility = View.VISIBLE
                    waitingForIncomingCallTextView.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun endCall() {
        onGoingCall?.hangup(object : NexmoRequestListener<NexmoCall> {
            override fun onError(error: NexmoApiError) {
                TODO("Not yet implemented")
            }

            override fun onSuccess(result: NexmoCall?) {
                runOnUiThread {
                    hideUI()
                    startCallButton.visibility = View.VISIBLE
                    waitingForIncomingCallTextView.visibility = View.VISIBLE
                }
            }
        })
        onGoingCall = null
    }

    @SuppressLint("MissingPermission")
    fun startCall(){
        client.serverCall(otherUser, null, object : NexmoRequestListener<NexmoCall>{
            override fun onError(error: NexmoApiError){
                TODO("Not yet implemented")
            }

            override fun onSuccess(result: NexmoCall?){
                runOnUiThread{
                    hideUI()
                    endCallButton.visibility = View.VISIBLE
                }

                onGoingCall = result
                onGoingCall?.addCallEventListener(callListener)
            }
        })
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello ",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ApptoappTheme {
//        Greeting("Android")
//    }
//}