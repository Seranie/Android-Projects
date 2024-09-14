package com.example.mind_care.guide.guide_showcase;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mind_care.R;
import com.example.mind_care.showcases.ShowcaseChangeViewModel;
import com.example.mind_care.guide.TabPositionViewModel;

import java.util.List;

public class GuideShowcaseAdapter extends RecyclerView.Adapter<GuideShowcaseAdapter.GuideShowcaseViewHolder> {
    //Adapter for guide showcase's recycler view of cards.
    private final List<GuideShowcaseCard> cardList;
    private final TabPositionViewModel tabPositionViewModel;
    private final ShowcaseChangeViewModel showcaseViewModel;
    private SoundPool soundPool;
    private int soundId;

    private final int MAX_STREAMS = 5;
    private final float LEFT_VOLUME = 1.0f;
    private final float RIGHT_VOLUME = 1.0f;
    private final int PRIORITY = 0;
    private final int LOOP = 0;
    private final float RATE = 1.0f;

    public GuideShowcaseAdapter(List<GuideShowcaseCard> cardList, TabPositionViewModel tabPositionViewModel, ShowcaseChangeViewModel showcaseViewModel, Context context){
        this.cardList = cardList;
        this.tabPositionViewModel = tabPositionViewModel;
        this.showcaseViewModel = showcaseViewModel;
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                ).setMaxStreams(MAX_STREAMS)
                .build();
        soundId = soundPool.load(context, R.raw.show_me_click, 1);
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
                    soundPool.play(soundId, LEFT_VOLUME, RIGHT_VOLUME, PRIORITY, LOOP, RATE);
                    showcaseViewModel.setShowcaseChangeId(adapterPosition);
                    break;
                case "learn_more_button":
                    //button switches tab from showcase to docs, and flashes the corresponding docs header to prompt click
                    tabPositionViewModel.setTabPositionLiveData(adapterPosition);
                    break;
            }
        }
    }
}