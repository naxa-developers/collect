package org.odk.collect.android.audio;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.AudioRecordingControllerFragmentBinding;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;
import org.odk.collect.strings.format.LengthFormatterKt;

import javax.inject.Inject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AudioRecordingControllerFragment extends Fragment {

    @Inject
    AudioRecorderViewModelFactory audioRecorderViewModelFactory;

    public AudioRecordingControllerFragmentBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AudioRecordingControllerFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final AudioRecorderViewModel viewModel = new ViewModelProvider(requireActivity(), audioRecorderViewModelFactory).get(AudioRecorderViewModel.class);

        viewModel.getCurrentSession().observe(getViewLifecycleOwner(), session -> {
            if (session == null) {
                binding.getRoot().setVisibility(GONE);
            } else if (session.getFailedToStart()) {
                binding.getRoot().setVisibility(GONE);
                new MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.start_recording_failed)
                        .setPositiveButton(R.string.ok, (dialog, which) -> viewModel.cleanUp())
                        .show();
            } else if (session.getFile() == null) {
                binding.getRoot().setVisibility(VISIBLE);

                binding.timeCode.setText(LengthFormatterKt.formatLength(session.getDuration()));

                if (session.getPaused()) {
                    binding.pauseRecording.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_mic_24));
                    binding.pauseRecording.setContentDescription(getString(R.string.resume_recording));
                    binding.pauseRecording.setOnClickListener(v -> viewModel.resume());

                    binding.recordingStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause_24dp));
                } else {
                    binding.pauseRecording.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause_24dp));
                    binding.pauseRecording.setContentDescription(getString(R.string.pause_recording));
                    binding.pauseRecording.setOnClickListener(v -> viewModel.pause());

                    binding.recordingStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_mic_24));
                }

                // Pause not available before API 24
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                    binding.pauseRecording.setVisibility(GONE);
                }
            } else {
                binding.getRoot().setVisibility(GONE);
            }
        });

        binding.stopRecording.setOnClickListener(v -> viewModel.stop());
    }
}
