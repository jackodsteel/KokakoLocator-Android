package nz.ac.canterbury.seng440.kokakolocator.view

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_view_recordings.*
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import android.widget.AdapterView.OnItemClickListener
import android.util.DisplayMetrics
import android.view.Display
import android.widget.Toast
import android.media.AudioManager
import android.view.Menu
import android.view.MenuItem
import java.io.FileInputStream
import java.io.IOException


class ViewRecordingsActivity : AppCompatActivity() {
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var recordingsList: MutableList<Recording> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recordings)

        Thread{
            recordingsList.clear()
            recordingsList.addAll(database().recordingDao().getAll())
            runOnUiThread { viewAdapter.notifyDataSetChanged() }
        }.start()



        viewManager = LinearLayoutManager(this)
        viewAdapter = RecordingsAdapter(recordingsList, this,{record:Recording -> recordItemClicked(record)})


        recordings_recycler.adapter = viewAdapter
        recordings_recycler.layoutManager = viewManager

    }

    fun sort(){
        recordingsList.reverse()
        viewAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bar_recordings, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.sort_arrows -> {
            sort()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }



    private fun recordItemClicked(record : Recording) {
        val mediaPlayer = MediaPlayer()
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(record.fileName)
            mediaPlayer.setDataSource(fis.getFD())
            mediaPlayer.prepare()
            mediaPlayer.start()
            sort()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (ignore: IOException) {
                }

            }

        }

    }

}
