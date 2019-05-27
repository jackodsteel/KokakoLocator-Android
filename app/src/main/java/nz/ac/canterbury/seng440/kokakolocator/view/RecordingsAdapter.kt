package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recordings_row.view.*
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
class RecordingsAdapter(val Dataset: List<Recording>,val context:Context) :
    RecyclerView.Adapter<ViewHolder>(){


    override fun getItemCount(): Int {
        return Dataset.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recordings_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recordingsTitle?.text = Dataset[position].toString()
    }






}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view){
    val recordingsTitle = view.recordingsTitle


}

