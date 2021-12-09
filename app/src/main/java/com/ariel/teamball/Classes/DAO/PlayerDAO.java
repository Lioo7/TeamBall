package com.ariel.teamball.Classes.DAO;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ariel.teamball.Classes.Player;
import com.ariel.teamball.EditProfile;
import com.ariel.teamball.MyProfile;
import com.ariel.teamball.SportsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// Data Access Object class that synchronizes the Player objects with the data base
public class PlayerDAO {

    public static final String TAG = "TAG";
    private static FirebaseAuth fAuth;
    private static FirebaseFirestore fStore;
    private static Context context;
    private static StorageReference storageReference;
    private static FirebaseUser user;

    public PlayerDAO(Context context){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = fAuth.getCurrentUser();

        this.context = context;
    }

    public static FirebaseUser getUser() {
        return user;
    }

    public static void setUser(FirebaseUser user) {
        PlayerDAO.user = user;
    }

    //Add room to list of private rooms user
    public static void addRoom(String category,String roomName){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        DatabaseReference usersRef = ref.child("userRooms/" + playerID());
        Map<String, Object> groups = new HashMap<>();
        groups.put(category,roomName);

        usersRef.updateChildren(groups);
    }

    public static String playerID(){
        return fAuth.getCurrentUser().getUid();
    }

    public static DocumentReference getCollection(String nameCollection, String playerID){
        return fStore.collection(nameCollection).document(playerID);
    }

    public static StorageReference getStorage(String path){
        return storageReference.child(path);
    }

    public static void playerSignOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public static boolean playerConnected(){
        return fAuth.getCurrentUser() != null;
    }

    public static void playerRegister(Player p, ProgressBar pb){

        //Register the user in firebase
        fAuth.createUserWithEmailAndPassword(p.getEmail(),p.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser fuser = fAuth.getCurrentUser();

                    //Send verification link
                    fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Verification Email Has Been Sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"onFailure: Email not sent " + e.getMessage());
                        }
                    });

                    Toast.makeText(context,"User Created", Toast.LENGTH_SHORT).show();


                    String userID = fuser.getUid();
                    //Create collection
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    //Store player in the collection
                    documentReference.set(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG,"onSuccess: user Profile is created for "+userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"onFailure: "+e.toString());
                        }
                    });

                    //Go to a SportsMenu page
                    Intent myIntent = new Intent(context, SportsMenu.class);
                    context.startActivity(myIntent);

                    pb.setVisibility(View.GONE);

                }else{
                    Toast.makeText(context,"Error! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void playerLogin(String email,String password,ProgressBar pb){

        //Authenticate the user
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context,"Logged in Successfully", Toast.LENGTH_SHORT).show();

                    Intent myIntent = new Intent(context, SportsMenu.class);
                    context.startActivity(myIntent);

                    pb.setVisibility(View.GONE);

                }else{
                    Toast.makeText(context,"Error! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void resetPassword(String mail){

        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Reset Link Sent to Your Email",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error! Reset Link Is Not Sent " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updatePassword(String newPassword){

        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context,"Password Reset Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Password Reset Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void editPlayerDetails(String email, String fullName, String phone){
        user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                DocumentReference docRef = fStore.collection("users").document(user.getUid());
                Map<String,Object> edited = new HashMap<>();
                edited.put("email",email);
                edited.put("fullName",fullName);
                edited.put("phone", phone);
                docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Profile Updated", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context,MyProfile.class));

                    }
                });
                Toast.makeText(context,"Email is changed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Upload image to firebase storage
    public static void uploadImage(Uri imageUri,ImageView profileImageView){
        StorageReference fileRef = getStorage("users/"+playerID()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImageView);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private DatabaseReference databaseReference;
//
//    public PlayerDAO() {
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        databaseReference = db.getReference(Player.class.getSimpleName());
//    }
//
//
//    public void add(Player _player) {
//        databaseReference.push().setValue(_player);
//    }
//
//    public void update(String key, HashMap<String, Object> hashMap) {
//        databaseReference.child(key).updateChildren(hashMap);
//    }
//
//    public void remove(String key) {
//        databaseReference.child(key).removeValue();
//    }


//    @Override
//    public Set<Player> getAllUsers() {
//        return null;
//    }
//
//    @Override
//    public Player getUserByUserEmailAndPassword() {
//        return null;
//    }
//
//    @Override
//    public boolean insertUser() {
//        return false;
//    }
//
//    @Override
//    public boolean updateUser() {
//        return false;
//    }
//
//    @Override
//    public boolean deleteUser() {
//        return false;
//    }
}
