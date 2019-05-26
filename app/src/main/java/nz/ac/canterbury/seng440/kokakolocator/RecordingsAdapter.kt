package nz.ac.canterbury.seng440.kokakolocator

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordingsAdapter(private val Dataset: Array<String>) :
    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {
    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recordings_row, parent, false) as TextView

        return ViewHolder(textView)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = Dataset[position]
    }

    override fun getItemCount(): Int {
        return Dataset.size
    }

}