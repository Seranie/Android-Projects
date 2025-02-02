package com.example.tutorial_menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GuideDocsAdapter extends RecyclerView.Adapter<GuideDocsAdapter.GuideDocsViewHolder> {
    private final List<GuideDocsCard> cardList;
    private final int cardNumber;

    public GuideDocsAdapter(List<GuideDocsCard> cardList, int cardNumber){
        this.cardList = cardList;
        this.cardNumber = cardNumber;
    }


    @Override
    public int getItemViewType(int position){
        return cardList.get(position).getGuideDocsCardType();
    }

    private View inflateView(ViewGroup parent, int layout){
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @NonNull
    @Override
    public GuideDocsAdapter.GuideDocsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 2:
                view = inflateView(parent, R.layout.guide_docs_card_contacts);
                break;
            case 3:
                view = inflateView(parent, R.layout.guide_docs_card_fences);
                break;
            case 4:
                view = inflateView(parent, R.layout.guide_docs_card_chatbuddy);
                break;
            case 1:
            default:
                view = inflateView(parent, R.layout.guide_docs_card_reminders);
                break;
        }
        return new GuideDocsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideDocsAdapter.GuideDocsViewHolder holder, int position) {
        //Blank as card data is already present in individual layout files.
    }

    @Override
    public int getItemCount() {return cardList.size();}

    public class GuideDocsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageButton mDropDownButton;
        LinearLayout mDescriptionLayout;
        LinearLayout mTitleLayout;
        int mDescriptionLayoutHeight;
        private boolean isAnimating = false;

        public GuideDocsViewHolder(@NonNull View itemView) {
            super(itemView);
            mDropDownButton = itemView.findViewById(R.id.guide_docs_button);
            mDescriptionLayout = itemView.findViewById(R.id.guide_docs_description_layout);
            mTitleLayout = itemView.findViewById(R.id.guide_docs_title_layout);

            //Sets onclick animation when clicking on the card title.
            mTitleLayout.setOnClickListener(this);
            mDescriptionLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                //Obtains description layout's height
                @Override
                public void onGlobalLayout() {
                    mDescriptionLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mDescriptionLayoutHeight = mDescriptionLayout.getHeight();
                    mDescriptionLayout.getLayoutParams().height = 0;
                    mDescriptionLayout.requestLayout();
                }
            });
        }

        @Override
        public void onClick(View view) {
            //Display/hide text description of the tool and flip the drop arrow
            if (isAnimating){
                //if animation is playing ignore any additional clicks
                return;
            }

            boolean descriptionIsVisible;

            descriptionIsVisible = mDescriptionLayout.getHeight() == mDescriptionLayoutHeight;

            if (descriptionIsVisible) {
                collapseView();
            } else {
                expandView();
            }

            mDropDownButton.setImageResource(descriptionIsVisible?R.drawable.arrow_drop_down:R.drawable.arrow_drop_up);
        }

        private void expandView(){
            ValueAnimator animator = slideAnimator(0, mDescriptionLayoutHeight);
            //add listener to ignore additional clicks during animation
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                }
            });
            animator.start();
        }

        private void collapseView(){
            ValueAnimator animator = slideAnimator(mDescriptionLayoutHeight, 0);
            //add listener to ignore additional clicks during animation
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                }
            });
            animator.start();
        }

        private ValueAnimator slideAnimator(int start, int end){
            //ValueAnimator that animates between 0 and height of the expandable layout section
            ValueAnimator animator = ValueAnimator.ofInt(start, end).setDuration(400);

            animator.addUpdateListener(valueAnimator ->{
                mDescriptionLayout.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                mDescriptionLayout.requestLayout();
            });
            return animator;
        }
    }
}
