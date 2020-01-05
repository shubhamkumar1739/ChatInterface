package com.example.chatinterface.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatinterface.R;

import com.example.chatinterface.model.RequestTypeModel;
import com.example.chatinterface.model.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private static final String TAG = "request_fragment";
    private View RequestFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference chatReqref, UserRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String requestUserName;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        RequestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        UserRef = databaseReference.child("Users");
        chatReqref = databaseReference.child("Chat Requests");
        contactsRef = databaseReference.child("Contacts");


        myRequestsList = RequestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestFragmentView;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");

        FirebaseRecyclerOptions<RequestTypeModel> options = new FirebaseRecyclerOptions.Builder<RequestTypeModel>()
                .setQuery(chatReqref.child(currentUserId), RequestTypeModel.class).build();


        FirebaseRecyclerAdapter<RequestTypeModel, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestTypeModel, RequestViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull RequestTypeModel model) {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);


                final String list_user_id = getRef(position).getKey();
                Log.d(TAG, "userID: "+ list_user_id);

                // Code To Find Request Type (Received)...
                ValueEventListener eventListener = new ValueEventListener() {


                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // (Key-I) For Getting Key Info Inside Chat Requests Node...
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            // For Getting Key Info inside above Key (Key-I)...
                            for(DataSnapshot dSnapshot : ds.getChildren()) {
                                // Create A Model With Keys As In Firebase Database...
                                RequestTypeModel requestType = dSnapshot.getValue(RequestTypeModel.class);
                                if (requestType != null) {
                                    Log.d("TAG", requestType.getRequest_type());
                                    String request_type = requestType.getRequest_type();
                                    Log.d(TAG, "Request Type: "+request_type);
                                    // If Request Type Is "Received"...
                                    if ("received".equals(request_type)) {
                                        // ----------------------------------------
                                        // Code To Find Users With Received Requests...
                                        ValueEventListener listener = new ValueEventListener() {


                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                // For Getting Keys Info Inside Users Node....
                                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    String key = snapshot.getKey();

                                                    // Check If Received User UID = Key From List...
                                                    if (list_user_id != null && list_user_id.equals(key)) {

                                                        // Get Info Of Only Received Request Keys...
                                                        DatabaseReference userReference = UserRef.child(key);
                                                        ValueEventListener valueEventListener = new ValueEventListener() {


                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                                                                if (usersModel != null){
                                                                    String user_name = usersModel.getName();
                                                                    String userStatus = usersModel.getStatus();
                                                                    String userImg = usersModel.getImage();



                                                                    holder.userName.setText(user_name);
                                                                    holder.userStatus.setText(userStatus);
                                                                    Picasso.get().load(userImg).placeholder(R.drawable.profile_image).into(holder.profileImage); //adding image here

                                                                }else {
                                                                    Log.d(TAG, "Users Model Empty!");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        };
                                                        userReference.addValueEventListener(valueEventListener);
                                                        break;
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.d(TAG, "onCancelled: "+databaseError.toString());
                                            }
                                        };
                                        UserRef.addValueEventListener(listener);
                                        // --------------------------------------------------
                                        break;
                                    }
                                }else {
                                    Log.d(TAG, "Request Type Model Null!");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: "+databaseError.toString());
                    }
                };

                // Adding Listener to "ChatRequestReference"...
                chatReqref.addListenerForSingleValueEvent(eventListener);

//                ------------------------------------------------
                // Accept Or Decline Request Code...
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[] = new CharSequence[]{
                                "Accept",
                                "Cancel"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(requestUserName + "Chat Request");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    contactsRef.child(currentUserId).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                contactsRef.child(list_user_id).child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            chatReqref.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        chatReqref.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"New Contact saved",Toast.LENGTH_SHORT).show();



                                                                                }

                                                                            }
                                                                        });

                                                                    }

                                                                }
                                                            });


                                                        }

                                                    }
                                                });


                                            }

                                        }
                                    });

                                }
                                if (i == 1) {

                                    chatReqref.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatReqref.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();




                                                        }

                                                    }
                                                });

                                            }

                                        }
                                    });



                                }

                            }
                        });

                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new RequestViewHolder(view);
            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);


        }
    }

}
