package com.example.app_to_app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import com.nexmo.client.NexmoCall
import com.nexmo.client.NexmoCallEventListener
import com.nexmo.client.NexmoCallMemberStatus
import com.nexmo.client.NexmoClient
import com.nexmo.client.NexmoMediaActionState
import com.nexmo.client.NexmoMember
import com.nexmo.client.request_listener.NexmoApiError
import com.nexmo.client.request_listener.NexmoConnectionListener
import com.nexmo.client.request_listener.NexmoRequestListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


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

    fun getStringFromAssets(context: Context, path: String?): String? {
        val buf = StringBuilder()
        val text: InputStream
        try {
            text = context.assets.open(path!!)
            val `in` = BufferedReader(InputStreamReader(text))
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
            }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buf.toString()
    }
    fun Executer(command: String?): String? {
        val output = StringBuffer()
        val p: Process
        try {
            p = Runtime.getRuntime().exec(command)
            p.waitFor()
            val reader =
                BufferedReader(InputStreamReader(p.inputStream))
            var line = ""
            while (reader.readLine().also { line = it } != null) {
                output.append(line + "n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return output.toString()
    }
    private fun loginAsAlice() {
        val com = getStringFromAssets(this, "token.txt")
        val command = Executer(com)
        otherUser = "Bob"
        client.login(command.toString())
    }

    private fun loginAsBob() {
        otherUser = "Alice"
        client.login("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2OTY1ODY5ODMsImp0aSI6Ijc4NDAxZWUwLTY0MzAtMTFlZS1iMDE2LWIzNjhhZWNjOWNkNyIsImFwcGxpY2F0aW9uX2lkIjoiYTliMzhjNzItMDZmZC00ODc5LWIwNmItZWU2NjRjMjY2M2UzIiwic3ViIjoiQm9iIiwiZXhwIjoxNjk2NTg3MDA0OTc0LCJhY2wiOnsicGF0aHMiOnsiLyovdXNlcnMvKioiOnt9LCIvKi9jb252ZXJzYXRpb25zLyoqIjp7fSwiLyovc2Vzc2lvbnMvKioiOnt9LCIvKi9kZXZpY2VzLyoqIjp7fSwiLyovaW1hZ2UvKioiOnt9LCIvKi9tZWRpYS8qKiI6e30sIi8qL2FwcGxpY2F0aW9ucy8qKiI6e30sIi8qL3B1c2gvKioiOnt9LCIvKi9rbm9ja2luZy8qKiI6e30sIi8qL2xlZ3MvKioiOnt9fX19.D6ZoHzlthycmRqXK-I5fjUAPCZ-CTHjtpu8RybZgMh7ebJkFUFRvUkXM5l0f34oBdz1hbZRfocfageIdWaInee6v5Z0UJycLxMd5p2HEhhsFXtuZ-doGKpOkwcT1W-ArZVCbPcQyEmOHIHek5aqSRN5bKr-1a9vKf6HDw1obNOC89xM8AynnzC1Sowm57ZlSnD0DcbjwE3g9uqUHOj4FcvxfToq-KKzzEcGLYfWNUyaUTWMv8bNFtViXdgdFs1PWs7fqRQEtagReO4r9wJfwMOlxMZcdDzBBBv05lmUHijgwco02-hwYaJCfMbfviWot7buwa9w1p8uxARo46vFdLw")
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