package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latestmsg_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
companion object{
    var currentUser:User?=null
    val tag="latestmsgs"
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_msg.adapter=adapter
        recyclerview_latest_msg.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //set item click listener on adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(tag,"123")
            val intent=Intent(this,ChatLogActivity::class.java)
            val row=item as latestMsgRow

            intent.putExtra("User_key",row.ChatPartnerUser)
            startActivity(intent)
        }
        fetcurrentUser()
        verifyUserLogin()
       // setupDummyRows()

        listenForlatestMessage()
    }
val latestMessagesMap=HashMap<String,ChatLogActivity.ChatMessage>()
    private fun listenForlatestMessage() {
        val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage=snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?:return

                latestMessagesMap[snapshot.key!!]=chatmessage
                refreshRecyclerView()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage=snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?:return

                latestMessagesMap[snapshot.key!!]=chatmessage
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun refreshRecyclerView() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(latestMsgRow(it))
        }
    }

    class latestMsgRow(val chatMessage:ChatLogActivity.ChatMessage):Item<GroupieViewHolder>(){
        var ChatPartnerUser:User?=null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
viewHolder.itemView.textView2_message.text=chatMessage.text
       val chatPartner:String
        if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
            chatPartner=chatMessage.toId
        } else{
            chatPartner=chatMessage.fromId
        }
        val ref=FirebaseDatabase.getInstance().getReference("/users/$chatPartner")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ChatPartnerUser=snapshot.getValue(User::class.java)
                viewHolder.itemView.textView_username.text=ChatPartnerUser?.username
                val target=viewHolder.itemView.imageView_latestmsg_row
                Picasso.get().load(ChatPartnerUser?.profileimgurl).into(target)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun getLayout(): Int {
        return  R.layout.latestmsg_row
    }

}
    val adapter=GroupAdapter<GroupieViewHolder>()
//    private fun setupDummyRows() {
//
//
//        adapter.add(latestMsgRow())
//        adapter.add(latestMsgRow())
//        adapter.add(latestMsgRow())
//
//    }

    private fun fetcurrentUser() {
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(snapshot: DataSnapshot) {
             currentUser=snapshot.getValue(User::class.java)
                Log.d("latestmsgs","currentuser${currentUser?.profileimgurl}")
            }

        })
    }

    private fun verifyUserLogin() {
        //TODO("Not yet implemented")
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent=Intent(this,RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
       R.id.menu_sign_out -> {
           FirebaseAuth.getInstance().signOut()
           val intent = Intent(this, RegisterActivity::class.java)
           intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
           startActivity(intent)
       }
            R.id.new_Message->{
val intent=Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}
