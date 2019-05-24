package nz.ac.canterbury.seng440.kokakolocator

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nz.ac.canterbury.seng440.kokakolocator.R.id.microphone
import java.io.IOException
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_CONFIGURATION_MONO
import android.media.AudioRecord



class RecordAudioView:AppCompatActivity(){
    private var output: String? = null
    private var isRecording: Boolean = false

    private var mediaRecorder: MediaRecorder? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_screen)

        output = filesDir.absolutePath + "/recording.mp3"

        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        Log.e("Output",output)

        var isActivated:Boolean = false

        val linearLayout = findViewById(R.id.RecordingLayout) as LinearLayout
        val imageButton = findViewById<ImageButton>(R.id.microphone)
        imageButton.setOnClickListener(View.OnClickListener {
            isActivated = isActivated.not()
            if (isActivated){
                imageButton.setImageResource(R.drawable.microphone)
                stopRecording()
            } else
            {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permissions,0)
                } else {
                    startRecording()
                }
                imageButton.setImageResource(R.drawable.microphone_activated)

            }

        })
    }

    fun startRecording(){
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun stopRecording(){
        if(isRecording){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            isRecording = false
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }

    }

}