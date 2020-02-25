package com.brian.androidslots

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brian.androidslots.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.message_list
import kotlinx.android.synthetic.main.item_message.view.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login_button.setOnClickListener {
            startActivity(LoginAuth.newIntent(AuthMode.Login, this))
        }

        register_button.setOnClickListener {
            startActivity(LoginAuth.newIntent(AuthMode.Register, this))
        }
    }






}

sealed class AuthMode() : Parcelable {
    @Parcelize
    object Login : AuthMode()

    @Parcelize
    object Register : AuthMode()
}




class ChatActivity: AppCompatActivity() {
    private lateinit var messagesDB: DatabaseReference
    // create an instance level messages collection
    var messages: MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        messagesDB = FirebaseDatabase.getInstance().getReference("Messages")
        // add the event listener to receive data from Firebase

        //PROBLEM CODE RELATED child of this!!!!
        messagesDB.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messages = mutableListOf()
                dataSnapshot.children.forEach {
                    val message = it.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                update()
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read values: handle error
            }
        })
        //PROBLEM CODE End HERE!!!!


        message_list.layoutManager = LinearLayoutManager(this)
    }

    // update method to be called when UI needs to be refreshed
    private fun update(){
        message_list.adapter = MessagesAdapter(messages, this)
    }

    private fun saveMessage(sender: String, messageBody: String) {
        val key = messagesDB.push().key
        key ?: return

        val message = Message(sender, messageBody)

        messagesDB.child(key).setValue(message)
    }
}


private class MessagesAdapter(private val messages: List<Message>, val context: Context): RecyclerView.Adapter<MessagesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        return MessagesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    //    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
//        val message = messages[position]
//
//        holder.itemView.sender_label.text = message.sender
//        holder.itemView.message_body_label.text = message.messageBody
//    }
    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val message = messages[position]

        holder.itemView.sender_label.text = message.sender
        holder.itemView.message_body_label.text = message.messageBody

        if (FirebaseAuth.getInstance().currentUser?.email == message.sender) {
//            holder.itemView.sender_image.setImageResource(R.drawable.smile)
            holder.itemView.message_container.setBackgroundResource(R.drawable.rounded_background1)
        } else {
//            holder.itemView.sender_image.setImageResource(R.drawable.stars)
            holder.itemView.message_container.setBackgroundResource(R.drawable.rounded_background2)
        }
    }
}
