package eu.letmehelpu.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.MessageViewHolder> {

    List<Conversation> conversations;
    private final OnConversationSelectedListener conversationSelectedListener;

    public ConversationsAdapter(OnConversationSelectedListener conversationSelectedListener) {
        this.conversationSelectedListener = conversationSelectedListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.setConversation(conversations.get(position));
    }

    @Override
    public int getItemCount() {
        if(conversations == null) {
            return 0;
        }
        return conversations.size();
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }



    class MessageViewHolder extends RecyclerView.ViewHolder {

        private Conversation conversation;

        public MessageViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conversationSelectedListener.onCoversationSelected(conversation);
                }
            });
        }

        void setConversation(Conversation conversation) {
            this.conversation = conversation;
            ((TextView)itemView).setText("message="+ conversation.lastMessage);
        }
    }

    interface OnConversationSelectedListener {
        void onCoversationSelected(Conversation conversation);
    }
}
