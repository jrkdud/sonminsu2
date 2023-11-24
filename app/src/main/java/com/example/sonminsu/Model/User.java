package com.example.sonminsu.Model;

public class User {
    private String id;
    private String username;
    private String fullname;
    private String imageurl;
    private String bio;

    private String password;
    private String password2;
    private String email;


    public User(String id, String username, String fullname, String imageurl, String bio, String password, String password2, String email){
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.imageurl = imageurl;
        this.bio = bio;
        this.password = password;
        this.password2 = password2;
        this.email = email;
    }
    public User(){

    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getFullname(){
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageurl(){
        return imageurl;
    }

    public void setImageurl(String imageurl){
        this.imageurl = imageurl;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio){
        this.bio = bio;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
