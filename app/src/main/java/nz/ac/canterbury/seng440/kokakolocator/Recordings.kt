package nz.ac.canterbury.seng440.kokakolocator

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Recordings : Activity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recordings)

        var dataSet = arrayOf("Test Co-ords")




        viewManager = LinearLayoutManager(this)
        viewAdapter = RecordingsAdapter(dataSet)

        //recyclerView = findViewById<RecyclerView>(R.id.recor)

    }
}