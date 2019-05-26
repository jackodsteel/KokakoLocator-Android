package nz.ac.canterbury.seng440.kokakolocator.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nz.ac.canterbury.seng440.kokakolocator.R

class ViewRecordingsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recordings)


        var recordingsList = arrayOf("Test")

        viewManager = LinearLayoutManager(this)
        viewAdapter = RecordingsAdapter(recordingsList)

        //TODO this is broken but did it ever work?
        recyclerView = findViewById<RecyclerView>(R.id.recordingsRecyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            adapter = viewAdapter
        }

    }
}
