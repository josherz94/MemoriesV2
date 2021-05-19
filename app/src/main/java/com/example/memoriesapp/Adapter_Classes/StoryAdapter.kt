package com.example.memoriesapp.Adapter_Classes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.CreateStoryActivity
import com.example.memoriesapp.Model_Classes.Story
import com.example.memoriesapp.Model_Classes.User
import com.example.memoriesapp.R
import com.example.memoriesapp.StoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// Story Adapter to set RecyclerView data for Notifications
class StoryAdapter (private val mContext: Context,
                    private val mStory: List<Story>)
                    : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    // Inflates the layout on creation
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // viewType: 0 means its the current user's story. on click will inflate add story item layout
        if(viewType == 0) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item_layout, parent, false)
            return ViewHolder(view)
        } else {
            // viewType >0 will inflate story item layout so the user can view the story
            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item_layout, parent, false)
            return ViewHolder(view)
        }
    }

    // return the story list item count
    override fun getItemCount(): Int {
        return mStory.size
    }

    // Reflects Stories held by the viewholder to our recyclerview
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]

        userInfo(holder, story.getUserId(), position)

        // if adapter position is >0, another user's story
        if(holder.adapterPosition !== 0) {
            viewedStory(holder, story.getUserId())
        }
        // if adapter is 0, User's own story
        if(holder.adapterPosition === 0) {
            userStories(holder.addStoryText!!, holder.storyPlusBtn!!, false)
        }

        // on click listener for itemView. if 0, add story, if >0 view that specific story
        holder.itemView.setOnClickListener {
            if(holder.adapterPosition === 0) {
                userStories(holder.addStoryText!!, holder.storyPlusBtn!!, true)
            } else {
                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userid", story.getUserId())
                mContext.startActivity(intent)
            }
        }
    }

    // initialize our views for the viewholder
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        // viewHolder for story items
        var storyImage: CircleImageView? = null
        var storyImageSeen: CircleImageView? = null
        var storyUserName: TextView? = null

        // viewHolder for add story items
        var storyPlusBtn: ImageView? = null
        var addStoryText: TextView? = null

        init {
            // initialize story items
            storyImage = itemView.findViewById(R.id.story_image)
            storyImageSeen = itemView.findViewById(R.id.story_image_seen)
            storyUserName = itemView.findViewById(R.id.story_username)

            // initialize add story items
            storyPlusBtn = itemView.findViewById(R.id.story_add)
            addStoryText = itemView.findViewById(R.id.add_story_text)
        }
    }

    // itemType 0: add story for current user.
    // itemType >0: for viewing other stories
    override fun getItemViewType(position: Int): Int {
        if(position == 0) {
            return 0
        } else {
            return 1
        }
    }

    // get user info from firebase and set that info for story list
    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance()
            .reference
            .child("Users")
            .child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(viewHolder.storyImage)

                    if(position != 0) {
                        Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(viewHolder.storyImageSeen)
                        viewHolder.storyUserName?.text = user.getUsername()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // get current user's story from firebase. will add all stories to a list via a counter
    private fun userStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // keep track of the number of current user's story
                var counter = 0

                val timeCurrent = System.currentTimeMillis()

                for(snap in snapshot.children) {
                    val story = snap.getValue(Story::class.java)
                    if(timeCurrent>story!!.getTimeStart() && timeCurrent<story!!.getTimeEnd()) {
                        counter++
                    }
                }
                if(click) {
                    if(counter > 0) {
                        // alert dialog gives option to view own story or add story
                        val alertDialog = AlertDialog.Builder(mContext).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story"){

                            dialogInterface, which ->

                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface!!.dismiss()
                        }

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") {
                            dialogInterface, which ->

                            val intent = Intent(mContext, CreateStoryActivity::class.java)
                            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface!!.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(mContext, CreateStoryActivity::class.java)
                        intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)
                    }
                } else {
                    // change dialog if user has one or more stories to My Story
                    if(counter > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    } else {
                        // If user has no story, change text to add story
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // gets story ref from firebase and checks on data changed for views
    private fun viewedStory(viewHolder: ViewHolder, userId: String) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)

        storyRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0
                for(snap in snapshot.children) {
                    // if view is 1 day old do not display else increment counter for that specific story and make it viewable
                    if(!snap.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists()
                        && System.currentTimeMillis() < snap.getValue(Story::class.java)!!.getTimeEnd()) {
                        counter++
                        if(counter>0) {
                            viewHolder.storyImage!!.visibility = View.VISIBLE
                            viewHolder.storyImageSeen!!.visibility = View.GONE
                        } else {
                            viewHolder.storyImage!!.visibility = View.GONE
                            viewHolder.storyImageSeen!!.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}