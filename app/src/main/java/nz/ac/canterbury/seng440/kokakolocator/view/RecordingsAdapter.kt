package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recordings_row.view.*
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
class RecordingsAdapter(val Dataset: List<Recording>,val context:Context,val clickListener:(Recording) -> Unit) :
    RecyclerView.Adapter<ViewHolder>(){




    override fun getItemCount(): Int {
        return Dataset.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recordings_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recordingsTitle?.text = Dataset[position].toString()

        (holder).bind(Dataset[position],clickListener)
    }






}

class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
    val recordingsTitle = itemView.recordingsTitle
    fun bind(record: Recording,clickListener: (Recording) -> Unit){
        itemView.setOnClickListener{clickListener(record)}


    }



}

