package com.example.tutorial_menu.showcases;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tutorial_menu.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class RemindersHomePageShowCase extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reminders_home_page_showcase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TapTargetSequence sequence = new TapTargetSequence(getActivity())
                .targets(
                        TapTarget.forView(
                                view.findViewById(R.id.reminders_home_page_showcase_options),
                                "This is the options menu where you can sort contacts.",
                                "Okay"
                        ).transparentTarget(true),
                        TapTarget.forView(
                                view.findViewById(R.id.reminders_home_page_showcase_fab),
                                "Click this to start a new reminder",
                                "Okay"
                        ).transparentTarget(true)
                )
                .continueOnCancel(true)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        getParentFragmentManager().beginTransaction().replace(R.id.showcase_fragment_container, new ChatbuddyHomePageShowcase()).commit();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        ;
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        ;
                    }
                });

        sequence.start();


    }
}