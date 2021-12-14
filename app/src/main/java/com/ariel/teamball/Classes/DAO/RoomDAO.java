package com.ariel.teamball.Classes.DAO;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ariel.teamball.Classes.Room;
import com.ariel.teamball.MyRooms;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Data Access Object class that synchronizes the Room objects with the database
public class RoomDAO {

//    public static final String TAG = "TAG";
//    private static FirebaseAuth fAuth;
//    private static FirebaseFirestore fStore;
//    private static Context context;
//    private static DatabaseReference reference;
    private static String adminID;

    public RoomDAO(Context context){

//        fAuth = FirebaseAuth.getInstance();
//        fStore = FirebaseFirestore.getInstance();
//
//        this.context = context;
    }

    public RoomDAO() {

    }

    public static String getAdminOfRoom(String category,String roomName){
        DatabaseReference roomRef = getPathReference("Rooms/"+category+"/"+roomName);

        // Attach a listener to read the data at our rooms reference
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Room room = dataSnapshot.getValue(Room.class);
                adminID = room.getAdmin();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return adminID;
    }

    public static void createRoom(String category, Room newRoom){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        DatabaseReference roomsRef = ref.child("Rooms").child(category);
        Map<String, Object> room = new HashMap<>();
        room.put(newRoom.getName(),newRoom);

        roomsRef.updateChildren(room);
    }

    public static DatabaseReference getPathReference(String path){
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static void newUserInRoom(String category, String _room){
        //Access to the list of rooms category
        DatabaseReference reference = getPathReference("Rooms/" + category + "/" + _room);

        //Put all the rooms of the category to list from the firebase
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Room room = dataSnapshot.getValue(Room.class);
                room.setCurrentInRoom(room.getCurrentInRoom()+1);
                createRoom(category,room);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    public static boolean isAdminOfRoom(String playerID,String category,String room_name){
//
//        DatabaseReference roomRef = getPathReference("Rooms/"+category+"/"+room_name);
//
//        // Attach a listener to read the data at our rooms reference
//        roomRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Room room = dataSnapshot.getValue(Room.class);
//                adminID = room.getAdmin();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        if(playerID == adminID){
//            return true;
//        }
//
//        return false;
//    }
}
