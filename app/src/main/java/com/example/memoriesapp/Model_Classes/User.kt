package com.example.memoriesapp.Model_Classes

class User {
    private var username: String = ""
    private var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    private var uid: String = ""

    constructor(username: String, fullname: String, bio: String, image: String, uid: String) {
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
    }

    constructor()

    fun getUsername(): String { return username }
    fun setUsername(username: String) { this.username = username}

    fun getFullName(): String { return fullname }
    fun setFullName(fullname: String) { this.fullname = fullname}

    fun getBio(): String { return bio }
    fun setBio(bio: String) { this.bio = bio}

    fun getImage(): String { return image }
    fun setImage(image: String) { this.image = image}

    fun getUid(): String { return uid }
    fun setUid(uid: String) { this.uid = uid }
}