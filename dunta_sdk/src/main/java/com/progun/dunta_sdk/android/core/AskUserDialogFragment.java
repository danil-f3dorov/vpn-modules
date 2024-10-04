package com.progun.dunta_sdk.android.core;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.progun.dunta_sdk.R;

public class AskUserDialogFragment extends DialogFragment {

    public final String BUNDLE_KEY = "TITLE";

    private final float DIALOG_HEIGHT_PERCENTAGE = 0.9f;
    private final float DIALOG_WIDTH_PERCENTAGE = 0.9f;

    private final String DEFAULT_TITLE = "Get Instant Free Hint!";

    private OnUserChoiceListener userChoiceListener = null;
    View rootView;

    @ColorInt
    private int accentColorFromUser = 0;
    public final String ACCENT_COLOR_KEY = "accent_color";

    public void setUserChoiceListener(@NonNull OnUserChoiceListener listener) {
//        Log.v(PowerManager.TAG)
        userChoiceListener = listener;
    }

    public void removeUserChoiceListener() {
        userChoiceListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundleColor = getArguments();
        if (bundleColor != null) accentColorFromUser = getArguments().getInt(ACCENT_COLOR_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(R.layout.fragment_proxy_description, container, false);

        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_shape);

        var acceptBtn = (AppCompatButton) rootView.findViewById(R.id.proxyDescriptionAcceptBtn);
        var declineBtn = (AppCompatButton) rootView.findViewById(R.id.proxyDescriptionDeclineBtn);
        var title = (TextView) rootView.findViewById(R.id.proxyDescriptionTitle);
        var policyShortText = (TextView) rootView.findViewById(R.id.proxyDescriptionPolicy);

        var titleBundle = getArguments();
        title.setText(titleBundle != null ? titleBundle.getString(BUNDLE_KEY) : DEFAULT_TITLE);

        var textWithLinkPP = getString(R.string.proxy_description_private_pol_item);
        var spannableString =
                new SpannableString(getString(R.string.proxy_description_policy_text));
        var posLink = getString(R.string.proxy_policy_link);

        var posPan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(posLink)));
            }
        };

        var tosText = getString(R.string.proxy_description_policy_text);

        var tosStartIndex = tosText.indexOf(textWithLinkPP);
        var tosEndIndex = tosText.indexOf(textWithLinkPP) + textWithLinkPP.length();

        spannableString.setSpan(
                posPan,
                tosStartIndex,
                tosEndIndex,
                0
        );
        policyShortText.setText(spannableString);
        policyShortText.setMovementMethod(LinkMovementMethod.getInstance());

        acceptBtn.setOnClickListener(v -> {
            userChoiceListener.onUserPermissionGranted();
            dismiss();
        });

        declineBtn.setOnClickListener(v -> {
            userChoiceListener.onUserPermissionDenied();
            dismiss();
        });

        if (accentColorFromUser != 0) {
            acceptBtn.setBackgroundColor(accentColorFromUser);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        forceSetupDialogSize();
    }

    private void forceSetupDialogSize() {
        var width = (int) (getResources().getDisplayMetrics().widthPixels * DIALOG_WIDTH_PERCENTAGE);
        var height = (int) (getResources().getDisplayMetrics().heightPixels * DIALOG_HEIGHT_PERCENTAGE);
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeUserChoiceListener();
    }

    public interface OnUserChoiceListener {
        void onUserPermissionGranted();

        void onUserPermissionDenied();
    }
}
