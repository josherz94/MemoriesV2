package com.example.memoriesapp.Adapter_Classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Model_Classes.Comment
import com.example.memoriesapp.Model_Classes.User
import com.example.memoriesapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// Adapter for Comments. Attaches Comment data to the RecyclerView
class CommentsAdapter(private val mContext: Context,
                      private val mComment: MutableList<Comment>?)
                      : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    // Inflates the layout and returns the viewHolder for our views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Gets the item count for our mComment List
    override fun getItemCount(): Int {
        return mComment!!.size
    }

    // Reflects Comments held by the viewholder
    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]
        holder.comment.text = comment.getComment()
        getUserInfo(holder.profileImage, holder.userName, comment.getPublisher())
    }

    // Inner class to initialize our views
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var userName: TextView
        var comment: TextView

        // initializer
        init {
            profileImage = itemView.findViewById(R.id.profile_image_comment_page)
            userName = itemView.findViewById(R.id.user_name_comment_page)
            comment = itemView.findViewById(R.id.coment_page_comment)
        }

    }

    // Get the user info from firebase and attach their profile image and username
    private fun getUserInfo(profileImage: CircleImageView, userName: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisher)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user!!.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}