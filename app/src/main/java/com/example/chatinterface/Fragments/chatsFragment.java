package com.example.chatinterface.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatinterface.Activities.ChatActivity;
import com.example.chatinterface.model.Contacts;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatinterface.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.PrivilegedAction;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView private_chat_list;
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;


    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);




        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        private_chat_list = PrivateChatsView.findViewById(R.id.chats_list);
        private_chat_list.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);

        private_chat_list.addItemDecoration(dividerItemDecoration);




        return PrivateChatsView;
    }

    @Override
    public void onStart() {

        super.onStart();

        //progressBar.setVisibility(View.VISIBLE);
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override


            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String user_Ids = getRef(position).getKey();
                final String[] ret_Img = {"default_image"};

                usersRef.child(user_Ids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                               // progressBar.setVisibility(View.INVISIBLE);

                                ret_Img[0] = dataSnapshot.child("image").getValue().toString();
                                Log.d("img",ret_Img[0]);
                                Picasso.get().load(ret_Img[0]).into(holder.profileImage);


                            }

                            final String retName = dataSnapshot.child("name").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(retName);


                            if (dataSnapshot.child("UserState").hasChild("state")) {
                                String state = dataSnapshot.child("UserState").child("state").getValue().toString();
                                String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                                String time = dataSnapshot.child("UserState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.userStatus.setText("online");


                                }

                                else if (state.equals("offline")) {
                                    holder.userStatus.setText(" Last seen: " + date + " " + time);


                                }

                            } else {

                                holder.userStatus.setText("offline");


                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", user_Ids);
                                    chatIntent.putExtra("visit_user_name", retName);
                                    chatIntent.putExtra("visit_user_image",ret_Img[0]);
                                    Log.d("img",ret_Img[0]);

                                    startActivity(chatIntent);


                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };


        private_chat_list.setAdapter(adapter);
        adapter.startListening();


    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);


        }
    }
}
