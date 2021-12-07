package com.lars.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_write_data.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var intentFiltersArray: Array<IntentFilter>? = null

    private lateinit var textViewResult: TextView

    private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.activity_main)
        textViewResult = findViewById(R.id.textViewResult)

        try {

            btnwrite.setOnClickListener {
                val intent = Intent(this, WriteData::class.java)
                startActivity(intent)
            }
            //nfc process start
            pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            try {
                ndef.addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
            intentFiltersArray = arrayOf(ndef)
            if (nfcAdapter == null) {
                val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                builder.setMessage("This device doesn't support NFC.")
                builder.setPositiveButton("Cancel", null)
                val myDialog = builder.create()
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()

            } else if (!nfcAdapter!!.isEnabled) {
                val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                builder.setTitle("NFC Disabled")
                builder.setMessage("Plesae Enable NFC")

                builder.setPositiveButton("Settings") { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                builder.setNegativeButton("Cancel", null)
                val myDialog = builder.create()
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()
            }
        }
        catch (ex:Exception)
        {
            Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)


        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {
                try {
                    val inNdefMessage = this[0] as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    //if there are many records, you can call inNdefRecords[1] as array
                    val ndefRecord_0 = inNdefRecords[0]
                    val inMessage = String(ndefRecord_0.payload)
                    val Message = inMessage.drop(3);
                    textViewResult.setText(Message.toString())


                } catch (ex: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "There are no Machine and Shop information found!, please click write data to write those!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }

    }

    override fun onPause() {
        if (this.isFinishing) {
            nfcAdapter?.disableForegroundDispatch(this)
        }
        super.onPause()
    }
}
