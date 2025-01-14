package com.example.tutorial_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class GuideShowcaseAdapter extends RecyclerView.Adapter<GuideShowcaseAdapter.GuideShowcaseViewHolder> {
    //Adapter for guide showcase's recycler view of cards.
    private final List<GuideShowcaseCard> cardList;
    private final TabPositionViewModel tabPositionViewModel;

    public GuideShowcaseAdapter(List<GuideShowcaseCard> cardList, TabPositionViewModel tabPositionViewModel){
        this.cardList = cardList;
        this.tabPositionViewModel = tabPositionViewModel;
    }

    @NonNull
    @Override
    public GuideShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guide_showcase_card, parent, false);
        return new GuideShowcaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideShowcaseViewHolder holder, int position) {
        //gets the card at x position and updates the view with the card details
        GuideShowcaseCard card = cardList.get(position);
        holder.mImageView.setImageResource(card.getCardImage());
        holder.mCardTitle.setText(card.getCardTitle());
        holder.mCardSubtitle.setText(card.getCardSubtitle());
        holder.mCardDescription.setText(card.getCardDescription());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public class GuideShowcaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Holder retrieves and holds references to the card layout's UI elements.
        ImageView mImageView;
        TextView mCardTitle;
        TextView mCardSubtitle;
        TextView mCardDescription;
        //TODO link up with navigation
        Button mShowMe;
        Button mLearnMore;

        public GuideShowcaseViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.showcase_card_image);
            mCardTitle = itemView.findViewById(R.id.showcase_card_title);
            mCardSubtitle = itemView.findViewById(R.id.showcase_card_subtitle);
            mCardDescription = itemView.findViewById(R.id.showcase_card_description);
            mShowMe = itemView.findViewById(R.id.showcase_card_show_me_button);
            mLearnMore = itemView.findViewById(R.id.showcase_card_learn_more);
            //Add click handlers for navigation from showcase to docs
            mShowMe.setOnClickListener(this);
            mLearnMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //Clicking changes destination from showcase to docs
            int adapterPosition = getAdapterPosition();

            switch ((String) view.getTag()){
                case "show_me_button":
                    break;
                case "learn_more_button":
                    //navigate to docs and send bundle with position of the view, to correspond to which docs to open.
                    Bundle bundle = new Bundle();
                    //TODO add in adapterposition
                    bundle.putInt("cardNumber", adapterPosition);
                    //TODO CHANGE THIS SHIT
                    tabPositionViewModel.setSelectedTab(0);
                    break;
            }
        }
    }
}
