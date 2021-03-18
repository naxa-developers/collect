/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.instrumented.dao;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ApplicationResetter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
/**
 * This class contains tests for {@link FormsDao}
 */
public class FormsDaoTest {

    private FormsDao formsDao;
    private final StoragePathProvider storagePathProvider = new StoragePathProvider();

    // sample forms
    private Form biggestNOfSetForm;
    private Form birdsForm;
    private Form miramareForm;
    private Form geoTaggerV2Form;
    private Form widgetsForm;
    private Form sampleForm;
    private Form birds2Form;

    @Before
    public void setUp() throws IOException {
        formsDao = new FormsDao();
        resetAppState();
        setUpSampleForms();
    }

    @Test
    public void getAllFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor();
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(0));
        assertEquals(birdsForm, forms.get(1));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(widgetsForm, forms.get(4));
        assertEquals(sampleForm, forms.get(5));
        assertEquals(birds2Form, forms.get(6));
    }

    @Test
    public void getFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor(null, null, null, null);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(0));
        assertEquals(birdsForm, forms.get(1));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(widgetsForm, forms.get(4));
        assertEquals(sampleForm, forms.get(5));
        assertEquals(birds2Form, forms.get(6));

        String sortOrder = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";

        cursor = formsDao.getFormsCursor(null, null, null, sortOrder);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(6));
        assertEquals(birdsForm, forms.get(5));
        assertEquals(birds2Form, forms.get(4));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(sampleForm, forms.get(1));
        assertEquals(widgetsForm, forms.get(0));

        String selection = FormsColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = {"Miramare"};

        cursor = formsDao.getFormsCursor(null, selection, selectionArgs, null);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(miramareForm, forms.get(0));
    }

    private void setUpSampleForms() throws IOException {
        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Biggest N of Set.xml").createNewFile());
        biggestNOfSetForm = new Form.Builder()
                .displayName("Biggest N of Set")
                .jrFormId("N_Biggest")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487773315435L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Biggest N of Set-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Biggest N of Set.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/ccce6015dd1b8f935f5f3058e81eeb43.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(biggestNOfSetForm));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds.xml").createNewFile());
        birdsForm = new Form.Builder()
                .displayName("Birds")
                .jrFormId("Birds")
                .jrVersion("3")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782404899L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/4cd980d50f884362afba842cbff3a798.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(birdsForm));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Miramare.xml").createNewFile());
        miramareForm = new Form.Builder()
                .displayName("Miramare")
                .jrFormId("Miramare")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782545945L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Miramare-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Miramare.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/e733627cdbf220929bf9c4899cb983ea.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(miramareForm));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Geo Tagger v2.xml").createNewFile());
        geoTaggerV2Form = new Form.Builder()
                .displayName("Geo Tagger v2")
                .jrFormId("geo_tagger_v2")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782428992L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Geo Tagger v2-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Geo Tagger v2.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/1d5e9109298c8ef02bc523b17d7c0451.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(geoTaggerV2Form));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Widgets.xml").createNewFile());
        widgetsForm = new Form.Builder()
                .displayName("Widgets")
                .jrFormId("Widgets")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782554846L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Widgets-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Widgets.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(widgetsForm));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/sample.xml").createNewFile());
        sampleForm = new Form.Builder()
                .displayName("sample")
                .jrFormId("sample")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782555840L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/sample-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/sample.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/4f495fddd1f2544f65444ea83d25f425.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(sampleForm));

        assertTrue(new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds_4.xml").createNewFile());
        birds2Form = new Form.Builder()
                .displayName("Birds")
                .jrFormId("Birds")
                .jrVersion("4")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1512390303610L)
                .formMediaPath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds_4-media"))
                .formFilePath(new StoragePathProvider().getRelativeFormPath(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + "/Birds_4.xml"))
                .jrCacheFilePath(new StoragePathProvider().getRelativeCachePath(storagePathProvider.getOdkRootDirPath() + "/.cache/4cd980d50f884362afba842cbff3a775.formdef"))
                .build();

        Collect.getInstance().getContentResolver().insert(FormsColumns.CONTENT_URI, getValuesFromFormObject(birds2Form));
    }

    @After
    public void tearDown() {
        resetAppState();
    }

    private void resetAppState() {
        List<Integer> resetActions = Arrays.asList(
                ApplicationResetter.ResetAction.RESET_PREFERENCES, ApplicationResetter.ResetAction.RESET_INSTANCES,
                ApplicationResetter.ResetAction.RESET_FORMS, ApplicationResetter.ResetAction.RESET_LAYERS,
                ApplicationResetter.ResetAction.RESET_CACHE, ApplicationResetter.ResetAction.RESET_OSM_DROID
        );

        List<Integer> failedResetActions = new ApplicationResetter().reset(resetActions);
        assertEquals(0, failedResetActions.size());
    }

    private static ContentValues getValuesFromFormObject(Form form) {
        ContentValues values = new ContentValues();
        values.put(FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsColumns.FORM_FILE_PATH, form.getFormFilePath());
        values.put(FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsColumns.DATE, form.getDate());
        values.put(FormsColumns.JRCACHE_FILE_PATH, form.getJrCacheFilePath());
        values.put(FormsColumns.FORM_MEDIA_PATH, form.getFormMediaPath());
        values.put(FormsColumns.LANGUAGE, form.getLanguage());
        values.put(FormsColumns.AUTO_SEND, form.getAutoSend());
        values.put(FormsColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }
}
