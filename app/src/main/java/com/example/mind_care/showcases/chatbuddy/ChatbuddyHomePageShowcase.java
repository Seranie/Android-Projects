package com.example.mind_care.showcases.chatbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mind_care.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class ChatbuddyHomePageShowcase extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chatbuddy_home_page_showcase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Add tap target code here
        TapTargetSequence sequence = new TapTargetSequence(getActivity())
                .targets(
                        TapTarget.forView(
                            view.findViewById(R.id.chatbuddy_home_page_showcase_options),
                            getResources().getString(R.string.chatbuddy_showcase_options_title),
                            getResources().getString(R.string.chatbuddy_showcase_options_description)
                        ).transparentTarget(true),
                        TapTarget.forView(
                                view.findViewById(R.id.chatbuddy_home_page_showcase_fab),
                                getResources().getString(R.string.chatbuddy_showcase_fab_title),
                                getResources().getString(R.string.chatbuddy_showcase_fab_description)
                        ).transparentTarget(true)
                ).continueOnCancel(true)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        getActivity().finishAfterTransition();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        sequence.start();
    }
}