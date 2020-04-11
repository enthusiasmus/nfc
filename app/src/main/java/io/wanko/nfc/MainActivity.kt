package io.wanko.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var pendingIntent: PendingIntent;
    private lateinit var intentFiltersArray: Array<IntentFilter>;
    private lateinit var techListsArray: Array<Array<String>>;
    private lateinit var adapter: NfcAdapter;
    private lateinit var vibrator: Vibrator;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        println("Create")

        init();

        readFromIntent(getIntent())
    }

    private fun init() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        adapter = NfcAdapter.getDefaultAdapter(this);
        foreGroundDispatchSystem();
    }

    private fun foreGroundDispatchSystem() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")    /* Handles all MIME based dispatches.
                                 You should specify only the ones that you need. */
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }

        // does foreground only look at ndef discovered?
        intentFiltersArray = arrayOf(ndef)

        // does tech list structure has any side effects?
        techListsArray = arrayOf(arrayOf<String>(NfcF::class.java.name))
    }

    public override fun onStart() {
        println("Start")
        super.onStart()
    }

    public override fun onStop() {
        println("Stop")
        super.onStop()
    }

    public override fun onDestroy() {
        println("Destroy")
        super.onDestroy()
    }

    public override fun onRestart() {
        println("Restart")
        super.onRestart()
    }

    public override fun onPause() {
        println("Pause")
        super.onPause()
        adapter.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        println("Resume")
        super.onResume()
        adapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    private fun readFromIntent(intent: Intent) {
        println("read intent")

        if(
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
        ) {
            Toast.makeText(applicationContext, "NFC Tag gefunden", Toast.LENGTH_SHORT).show()
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

            printNDEFMessages(intent);
            printTag(intent)
        }
    }

    private fun printTag(intent: Intent) {
        println("Tag:")

        val extraTag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        println(extraTag)

        val techList = extraTag?.techList
        val searchedTech = Ndef::class.java.name

        if(!techList.isNullOrEmpty()) {
            for (tech in techList) {
                if (searchedTech == tech) {
                    println(tech)
                    break
                }
            }
        }
    }

    private fun printNDEFMessages(intent: Intent){
        println("NDF Messages:")

        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
            val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }

            for (message in messages) {
                for (record in message.records) {
                    println(" ${record.toString()}")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        println("New intent")

        super.onNewIntent(intent)
        readFromIntent(intent)
    }
}
