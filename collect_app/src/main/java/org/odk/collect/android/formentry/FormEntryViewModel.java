package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.form.api.FormEntryController;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.utilities.Clock;

import static org.odk.collect.android.analytics.AnalyticsEvents.ADD_REPEAT;
import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;

public class FormEntryViewModel extends ViewModel implements RequiresFormController {

    private final Analytics analytics;
    private final Clock clock;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    @Nullable
    private FormController formController;

    @Nullable
    private FormIndex jumpBackIndex;

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Analytics analytics, Clock clock) {
        this.analytics = analytics;
        this.clock = clock;
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;
    }

    @Nullable
    public FormIndex getCurrentIndex() {
        if (formController != null) {
            return formController.getFormIndex();
        } else {
            return null;
        }
    }

    public LiveData<String> getError() {
        return error;
    }

    @SuppressWarnings("WeakerAccess")
    public void promptForNewRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = formController.getFormIndex();
        jumpToNewRepeat();
    }

    public void jumpToNewRepeat() {
        formController.jumpToNewRepeatPrompt();
    }

    public void addRepeat(boolean fromPrompt) {
        if (formController == null) {
            return;
        }

        if (jumpBackIndex != null) {
            jumpBackIndex = null;
            analytics.logEvent(ADD_REPEAT, "Inline", formController.getCurrentFormIdentifierHash());
        } else if (fromPrompt) {
            analytics.logEvent(ADD_REPEAT, "Prompt", formController.getCurrentFormIdentifierHash());
        } else {
            analytics.logEvent(ADD_REPEAT, "Hierarchy", formController.getCurrentFormIdentifierHash());
        }

        try {
            formController.newRepeat();
        } catch (RuntimeException e) {
            error.setValue(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (!formController.indexIsInFieldList()) {
            try {
                formController.stepToNextScreenEvent();
            } catch (JavaRosaException e) {
                error.setValue(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        }
    }

    public void cancelRepeatPrompt() {
        if (formController == null) {
            return;
        }

        if (jumpBackIndex != null) {
            analytics.logEvent(ADD_REPEAT, "InlineDecline", formController.getCurrentFormIdentifierHash());

            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
        } else {
            try {
                this.formController.stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(exception.getCause().getMessage());
            }
        }
    }

    public void errorDisplayed() {
        error.setValue(null);
    }

    public boolean canAddRepeat() {
        if (formController != null && formController.indexContainsRepeatableGroup()) {
            FormDef formDef = formController.getFormDef();
            FormIndex repeatGroupIndex = getRepeatGroupIndex(formController.getFormIndex(), formDef);
            return !((GroupDef) formDef.getChild(repeatGroupIndex)).noAddRemove;
        } else {
            return false;
        }
    }

    public void moveForward() {
        try {
            formController.stepToNextScreenEvent();
        } catch (JavaRosaException e) {
            error.setValue(e.getCause().getMessage());
            return;
        }

        formController.getAuditEventLogger().flush(); // Close events waiting for an end time
    }

    public void moveBackward() {
        try {
            int event = formController.stepToPreviousScreenEvent();

            // If we are the beginning of the form we need to move back to the first actual screen
            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                formController.stepToNextScreenEvent();
            }
        } catch (JavaRosaException e) {
            error.setValue(e.getCause().getMessage());
            return;
        }

        formController.getAuditEventLogger().flush(); // Close events waiting for an end time
    }

    public void openHierarchy() {
        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true, clock.getCurrentTime());
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Analytics analytics;

        public Factory(Analytics analytics) {
            this.analytics = analytics;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(analytics, System::currentTimeMillis);
        }
    }
}
