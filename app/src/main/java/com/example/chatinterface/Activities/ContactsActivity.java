package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatinterface.R;
import com.example.chatinterface.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView myContactList;
    private DatabaseReference contactsref, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        myContactList = findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsref, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {


            @NonNull
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                String userIds=getRef(position).getKey();
                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            if (dataSnapshot.child("UserState").hasChild("state")) {

                                String state = dataSnapshot.child("UserState").child("state").getValue().toString();
                                String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                                String time = dataSnapshot.child("UserState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);



                                } else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);


                                }

                            } else {

                                holder.onlineIcon.setVisibility(View.INVISIBLE);


                            }




                            if(dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);



                            }
                            else{
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);

                            }


                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }


            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ContactsViewHolder viewHolder= new ContactsViewHolder(view);
                return viewHolder;
            }


        };
        myContactList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            onlineIcon=itemView.findViewById(R.id.user_online_status);

        }
    }
}
