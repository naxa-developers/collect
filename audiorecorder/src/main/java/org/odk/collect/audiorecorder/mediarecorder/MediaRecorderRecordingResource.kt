package org.odk.collect.audiorecorder.mediarecorder

import android.media.MediaRecorder
import org.odk.collect.audiorecorder.recorder.RecordingResource

internal abstract class MediaRecorderRecordingResource(private val mediaRecorder: MediaRecorder) : RecordingResource {

    protected abstract fun beforePrepare(mediaRecorder: MediaRecorder)

    override fun setOutputFile(path: String) {
        mediaRecorder.setOutputFile(path)
    }

    override fun prepare() {
        beforePrepare(mediaRecorder)
        mediaRecorder.prepare()
    }

    override fun start() {
        mediaRecorder.start()
    }

    override fun pause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder.pause()
        }
    }

    override fun resume() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder.resume()
        }
    }

    override fun stop() {
        mediaRecorder.stop()
    }

    override fun release() {
        mediaRecorder.release()
    }

    override fun getMaxAmplitude(): Int {
        return mediaRecorder.maxAmplitude
    }
}

internal class AACRecordingResource(mediaRecorder: MediaRecorder, private val kbitRate: Int) : MediaRecorderRecordingResource(mediaRecorder) {

    override fun beforePrepare(mediaRecorder: MediaRecorder) {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioSamplingRate(32000)
        mediaRecorder.setAudioEncodingBitRate(kbitRate * 1000)
    }
}

internal class AMRRecordingResource(mediaRecorder: MediaRecorder) : MediaRecorderRecordingResource(mediaRecorder) {

    override fun beforePrepare(mediaRecorder: MediaRecorder) {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setAudioSamplingRate(8000)
        mediaRecorder.setAudioEncodingBitRate(12200)
    }
}
