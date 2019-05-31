package nz.ac.canterbury.seng440.kokakolocator.view

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_view_recordings.*
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import java.io.FileInputStream

class ViewRecordingsActivity : AppCompatActivity() {

    private lateinit var viewAdapter: RecyclerView.Adapter<RecordingViewHolder>

    private lateinit var viewManager: RecyclerView.LayoutManager

    private var recordingsList: MutableList<Recording> = mutableListOf()

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recordings)

        viewManager = LinearLayoutManager(this)
        viewAdapter = RecordingsAdapter(recordingsList, this, this::recordItemClicked)

        recordingsRecycler.adapter = viewAdapter
        recordingsRecycler.layoutManager = viewManager

        getItemsFromDb()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bar_recordings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.sort_arrows -> {
                reverseItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun getItemsFromDb() {
        Thread {
            recordingsList.addAll(database().recordingDao().getAll())
            runOnUiThread {
                viewAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun reverseItems() {
        recordingsList.reverse()
        viewAdapter.notifyDataSetChanged()
    }

    private fun recordItemClicked(record: Recording) {
        FileInputStream(record.fileName).use {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(it.fd)
                prepare()
                start()
            }
        }
    }

}
