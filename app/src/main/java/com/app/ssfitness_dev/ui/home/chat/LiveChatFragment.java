package com.app.ssfitness_dev.ui.home.chat;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.ui.home.chat.FriendsFragment.FriendsViewHolder;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveChatFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    //options for firebase recycler
    FirebaseRecyclerOptions<Conversation> options;


    public LiveChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        mMainView =  inflater.inflate(R.layout.fragment_live_chat, container, false);
        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(mCurrent_user_id);

        //KEEP SYNCED
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages")
        .child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        options = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(conversationQuery,Conversation.class).build();

        FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ConversationViewHolder holder, int position, @NonNull Conversation model) {
                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                       /* String userName = dataSnapshot.child("userName").getValue().toString();
                        String userProfile = dataSnapshot.child("photoUrl").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }


                        holder.setName(userName);
                        if(userProfile!=null){
                            holder.setPhoto(userProfile, getContext());
                        }
*/
                        holder.setMessage(data, model.isSeen());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.live_chat_single_item, parent, false);

                return new ConversationViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mConvList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getFragmentManager() != null) {

            getFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

}

class ConversationViewHolder extends RecyclerView.ViewHolder{

    View mView;

    public ConversationViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setMessage(String message, boolean isSeen){

        TextView userMessageView = mView.findViewById(R.id.user_single_message);
        userMessageView.setText(message);

        if(!isSeen){
            userMessageView.setTypeface(userMessageView.getTypeface(), Typeface.BOLD);
        }
        else
        {
            userMessageView.setTypeface(userMessageView.getTypeface(), Typeface.NORMAL);
        }
    }

    public void setName(String name){
        TextView userNameView = mView.findViewById(R.id.user_single_name);
        userNameView.setText(name);
    }

    public void setPhoto(String userProfileUrl, Context applicationContext){
        ImageView userProfileView = mView.findViewById(R.id.user_single_profile);

        if(applicationContext!=null){
            Glide.with(mView.getContext()).load(userProfileUrl).into(userProfileView);
        }

    }

    public void setUserOnline(String online_status){

        ImageView userOnlineView = mView.findViewById(R.id.user_single_online_status);
        if(online_status.equals("true")){
            userOnlineView.setVisibility(View.VISIBLE);
        }
        else {
            userOnlineView.setVisibility(View.INVISIBLE);
        }
    }






}
