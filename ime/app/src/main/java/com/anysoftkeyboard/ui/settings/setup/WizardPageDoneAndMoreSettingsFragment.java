package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.ui.settings.KeyboardAddOnBrowserFragment;
import com.anysoftkeyboard.ui.settings.KeyboardThemeSelectorFragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class WizardPageDoneAndMoreSettingsFragment extends WizardPageBaseFragment
        implements View.OnClickListener {

    private DemoAnyKeyboardView mDemoAnyKeyboardView;

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_additional_settings_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_languages_action).setOnClickListener(this);
        view.findViewById(R.id.go_to_theme_action).setOnClickListener(this);
        view.findViewById(R.id.go_to_all_settings_action).setOnClickListener(this);

        mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);

        // DONE: I am restoring data from file here
        Log.v("FahadQaziTest", "restoring settings");
        setDefaultSettings();
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return false; // this step is never done! You can always configure more :)
    }

    @Override
    public void onClick(View v) {
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        switch (v.getId()) {
            case R.id.go_to_languages_action:
                activity.addFragmentToUi(
                        new KeyboardAddOnBrowserFragment(),
                        TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                break;
            case R.id.go_to_theme_action:
                activity.addFragmentToUi(
                        new KeyboardThemeSelectorFragment(),
                        TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                break;
            case R.id.go_to_all_settings_action:
                startActivity(new Intent(getContext(), MainSettingsActivity.class));
                // not returning to this Activity any longer.
                activity.finish();
                break;
            default:
                throw new IllegalArgumentException(
                        "Failed to handle "
                                + v.getId()
                                + " in WizardPageDoneAndMoreSettingsFragment");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AnyKeyboard defaultKeyboard =
                AnyApplication.getKeyboardFactory(getContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens());
        mDemoAnyKeyboardView.setKeyboard(defaultKeyboard, null, null);

        SetupSupport.popupViewAnimationWithIds(
                getView(),
                R.id.go_to_languages_action,
                0,
                R.id.go_to_theme_action,
                0,
                R.id.go_to_all_settings_action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDemoAnyKeyboardView.onViewNotRequired();
    }


    // DONE: restoring default settings
    private void setDefaultSettings() {
        InputStream inputStream = null;
        try {
            List<GlobalPrefsBackup.ProviderDetails> supportedProviders;
            Boolean[] checked;

            supportedProviders = GlobalPrefsBackup.getAllPrefsProviders(getContext());
            final CharSequence[] providersTitles = new CharSequence[supportedProviders.size()];
            final boolean[] initialChecked = new boolean[supportedProviders.size()];
            checked = new Boolean[supportedProviders.size()];

            for (int providerIndex = 0; providerIndex < supportedProviders.size(); providerIndex++) {
                // starting with everything checked
                checked[providerIndex] = initialChecked[providerIndex] = true;
                providersTitles[providerIndex] =
                        getText(supportedProviders.get(providerIndex).providerTitle);
            }


            inputStream = getContext().getAssets().open("Liz-AnySoftKeyboardPrefs.xml");
            File file = createFileFromInputStream(inputStream);
            GlobalPrefsBackup.updateCustomFilename(file);
            Observable<GlobalPrefsBackup.ProviderDetails> result = GlobalPrefsBackup.restore(new Pair<>(supportedProviders, checked));
            Disposable d = result.subscribe(providerDetails -> {
                Log.v("FahadQaziTest", "restore result: " + providerDetails + ", filename: " + file.getName());
            }, e -> {
                Log.v("FahadQaziTest", "error: " + e);
            });

            Log.v("FahadQaziTest", d.toString());

        } catch (IOException e) {
            Log.e("FahadQaziTest", e.getMessage());
        }
    }

    private File createFileFromInputStream(InputStream inputStream) {

        try{
            File f = new File(getContext().getCacheDir() + "/Liz-AnySoftKeyboardPrefs.xml");
            OutputStream outputStream = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
        }

        return null;
    }
}
