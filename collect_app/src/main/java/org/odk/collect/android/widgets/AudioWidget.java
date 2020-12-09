/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.databinding.AudioWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.AudioFileRequester;
import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.android.widgets.utilities.RecordingRequester;
import org.odk.collect.audioclips.Clip;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.strings.format.LengthFormatterKt.formatLength;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class AudioWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {

    AudioWidgetAnswerBinding binding;

    private final AudioPlayer audioPlayer;
    private final RecordingRequester recordingRequester;
    private final QuestionMediaManager questionMediaManager;
    private final AudioFileRequester audioFileRequester;

    private boolean recordingInProgress;
    private String binaryName;

    public AudioWidget(Context context, QuestionDetails questionDetails, QuestionMediaManager questionMediaManager, AudioPlayer audioPlayer, RecordingRequester recordingRequester, AudioFileRequester audioFileRequester) {
        super(context, questionDetails);
        this.audioPlayer = audioPlayer;

        this.questionMediaManager = questionMediaManager;
        this.recordingRequester = recordingRequester;
        this.audioFileRequester = audioFileRequester;

        binaryName = questionDetails.getPrompt().getAnswerText();

        updateVisibilities();
        updatePlayerMedia();

        recordingRequester.onIsRecordingBlocked(isRecordingBlocked -> {
            binding.captureButton.setEnabled(!isRecordingBlocked);
            binding.chooseButton.setEnabled(!isRecordingBlocked);
        });

        recordingRequester.onRecordingInProgress(getFormEntryPrompt(), session -> {
            recordingInProgress = true;
            updateVisibilities();

            binding.recordingDuration.setText(formatLength(session.first));
            binding.waveform.addAmplitude(session.second);
        });

        recordingRequester.onRecordingFinished(getFormEntryPrompt(), recording -> {
            recordingInProgress = false;

            if (recording != null) {
                setData(recording);
            } else {
                updateVisibilities();
            }
        });
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = AudioWidgetAnswerBinding.inflate(LayoutInflater.from(context));

        binding.captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        binding.captureButton.setOnClickListener(v -> {
            binding.waveform.clear();
            recordingRequester.requestRecording(getFormEntryPrompt());
        });
        binding.chooseButton.setOnClickListener(v -> audioFileRequester.requestFile(getFormEntryPrompt()));

        return binding.getRoot();
    }

    @Override
    public void deleteFile() {
        audioPlayer.stop();
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(), getAudioFile().getAbsolutePath());
        binaryName = null;
    }

    @Override
    public void clearAnswer() {
        deleteFile();
        widgetValueChanged();
        updateVisibilities();
    }

    @Override
    public IAnswerData getAnswer() {
        if (binaryName != null) {
            return new StringData(binaryName);
        } else {
            return null;
        }
    }

    /**
     * @param object file name of media file that will be available in the {@link QuestionMediaManager}
     * @see org.odk.collect.android.activities.FormEntryActivity
     */
    @Override
    public void setData(Object object) {
        // Support being handed a File as well
        if (object instanceof File) {
            object = (String) ((File) object).getName();
        }
        if (object instanceof String) {
            String fileName = (String) object;
            File newAudio = questionMediaManager.getAnswerFile(fileName);

            if (newAudio != null && newAudio.exists()) {
                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), newAudio.getAbsolutePath());

                // when replacing an answer. remove the current media.
                if (binaryName != null && !binaryName.equals(newAudio.getName())) {
                    deleteFile();
                }

                binaryName = newAudio.getName();
                Timber.i("Setting current answer to %s", newAudio.getName());

                updateVisibilities();
                updatePlayerMedia();
                widgetValueChanged();
            } else {
                Timber.e("Inserting Audio file FAILED");
            }
        } else {
            Timber.w("AudioWidget's setBinaryData must receive a File object.");
            return;
        }
    }

    private void updateVisibilities() {
        if (recordingInProgress) {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
            binding.recordingDuration.setVisibility(VISIBLE);
            binding.waveform.setVisibility(VISIBLE);
            binding.audioController.setVisibility(GONE);
        } else if (getAnswer() == null) {
            binding.captureButton.setVisibility(VISIBLE);
            binding.chooseButton.setVisibility(VISIBLE);
            binding.recordingDuration.setVisibility(GONE);
            binding.waveform.setVisibility(GONE);
            binding.audioController.setVisibility(GONE);
        } else {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
            binding.recordingDuration.setVisibility(GONE);
            binding.waveform.setVisibility(GONE);
            binding.audioController.setVisibility(VISIBLE);
        }

        if (questionDetails.isReadOnly()) {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
        }

        if (getFormEntryPrompt().getAppearanceHint() != null && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(WidgetAppearanceUtils.NEW)) {
            binding.chooseButton.setVisibility(GONE);
        }
    }

    private void updatePlayerMedia() {
        if (binaryName != null) {
            Clip clip = new Clip("audio:" + getFormEntryPrompt().getIndex().toString(), getAudioFile().getAbsolutePath());

            audioPlayer.onPlayingChanged(clip.getClipID(), binding.audioController::setPlaying);
            audioPlayer.onPositionChanged(clip.getClipID(), binding.audioController::setPosition);
            binding.audioController.setDuration(getDurationOfFile(clip.getURI()));
            binding.audioController.setListener(new AudioControllerView.Listener() {
                @Override
                public void onPlayClicked() {
                    audioPlayer.play(clip);
                }

                @Override
                public void onPauseClicked() {
                    audioPlayer.pause();
                }

                @Override
                public void onPositionChanged(Integer newPosition) {
                    audioPlayer.setPosition(clip.getClipID(), newPosition);
                }

                @Override
                public void onRemoveClicked() {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.delete_answer_file_question)
                            .setMessage(R.string.answer_file_delete_warning)
                            .setPositiveButton(R.string.delete_answer_file, (dialog, which) -> clearAnswer())
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
            });

        }
    }

    private Integer getDurationOfFile(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString != null ? Integer.parseInt(durationString) : 0;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.captureButton.setOnLongClickListener(l);
        binding.chooseButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.captureButton.cancelLongPress();
        binding.chooseButton.cancelLongPress();
    }

    /**
     * Returns the audio file added to the widget for the current instance
     */
    private File getAudioFile() {
        return questionMediaManager.getAnswerFile(binaryName);
    }
}
