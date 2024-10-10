import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.garbogo.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RequestsAdapter :
    ListAdapter<RequestItem, RequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textWard: TextView = itemView.findViewById(R.id.textWard)
        val textRequest: TextView = itemView.findViewById(R.id.textRequest)
        val buttonMarkPicked: Button = itemView.findViewById(R.id.buttonMarkPicked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.textWard.text = currentItem.ward
        holder.textRequest.text = "Request ID: ${currentItem.requestId}\n" +
                "Name: ${currentItem.name}\n" +
                "House No: ${currentItem.houseNo}\n" +
                "Phone: ${currentItem.phone}\n" +
                "Preferred Date: ${currentItem.datePref}\n" +
                "Type: ${currentItem.type}"

        holder.buttonMarkPicked.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("pickup Request")
            databaseReference.child(currentItem.ward).child(currentItem.requestId).removeValue()
        }
    }
}

class RequestDiffCallback : DiffUtil.ItemCallback<RequestItem>() {
    override fun areItemsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
        return oldItem.requestId == newItem.requestId
    }

    override fun areContentsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
        return oldItem == newItem
    }
}