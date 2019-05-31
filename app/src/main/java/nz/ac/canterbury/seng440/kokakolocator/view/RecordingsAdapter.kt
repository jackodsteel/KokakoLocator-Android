package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recordings_row.view.*
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording

typealias OnPlaybackFinishedCallback = () -> Unit

class RecordingsAdapter(
    private val recordings: List<Recording>,
    private val context: Context,
    private val clickListener: (Recording, OnPlaybackFinishedCallback) -> Unit
) : RecyclerView.Adapter<RecordingViewHolder>() {

    override fun getItemCount(): Int = recordings.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        return RecordingViewHolder(LayoutInflater.from(context).inflate(R.layout.recordings_row, parent, false))
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.recordingsTitle.text = recordings[position].toString()

        holder.bindClickListener(recordings[position], clickListener)
    }

}

class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val recordingsTitle: TextView = itemView.recordingsTitle
    private val baseTextColor = recordingsTitle.currentTextColor

    fun bindClickListener(record: Recording, clickListener: (Recording, OnPlaybackFinishedCallback) -> Unit) {
        itemView.setOnClickListener {
            setActiveColor()
            clickListener(record, this::resetColor)
        }
    }

    private fun resetColor() = recordingsTitle.setTextColor(baseTextColor)
    private fun setActiveColor() = recordingsTitle.setTextColor(Color.RED)

}

