package com.example.garbogo

import RequestItem
import RequestsAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ActivityRequests : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var requestsAdapter: RequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        requestsAdapter = RequestsAdapter()
        recyclerView.adapter = requestsAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("pickup Request")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val requestsList = mutableListOf<RequestItem>()

                for (wardSnapshot in dataSnapshot.children) {
                    val ward = wardSnapshot.key ?: continue
                    for (requestSnapshot in wardSnapshot.children) {
                        val requestId = requestSnapshot.key ?: continue
                        val name = requestSnapshot.child("Name").getValue(String::class.java) ?: ""
                        val houseNo = requestSnapshot.child("House_No").getValue(String::class.java) ?: ""
                        val phone = requestSnapshot.child("Phone").getValue(String::class.java) ?: ""
                        val datePref = requestSnapshot.child("Preferred Date").getValue(String::class.java) ?: ""
                        val type = requestSnapshot.child("Type").getValue(String::class.java) ?: ""
                        val requestItem = RequestItem(ward, requestId, name, houseNo, phone, datePref, type)
                        requestsList.add(requestItem)
                    }
                }

                requestsAdapter.submitList(requestsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}
