package com.app.minlan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.minlan.settings.SettingsActivity;
import com.app.minlan.view.AbstractAppView;
import com.app.minlan.view.AppViewFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.app.minlan.apps.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements ReloadCallback {
    public static final String SHARED_APPS_PREFS  = "favourite_apps";
    public static final String SHARED_SETTINGS    = "settings";
    public static final String SETTINGS_DARK_ICONS= "dark_icons";
    public static final String SETTINGS_SHOW_CLOCK= "show_clock";
    public static final String SETTINGS_TEXT_COLOR= "text_color";

    private List<ResolveInfo> mApplicationsInfo;
    private PackageManager mPackageManager;
    private TextInputEditText mInput;
    private int mCurrentSearchId = 0;
    private int mResourceId;

    private AppViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        mPackageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(intent, 0);

        mApplicationsInfo = Collections.unmodifiableList(apps);

        RecyclerView mAppViewsLayout = findViewById(R.id.app_container);
        mAppViewsLayout.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AppViewAdapter(this, str -> mInput.setText(""), this::runOnUiThread);
        mAppViewsLayout.setAdapter(mAdapter);

        addAppsToLayout("", AppStatus.WHICHEVER);

        mInput = findViewById(R.id.app_name_input);

        setListeners();

        new Greeter(this).showHints();
    }

    private void setListeners() {
        ImageButton button = findViewById(R.id.clear_button);
        button.setOnClickListener(v -> mInput.setText(""));
        button.setOnLongClickListener(v -> {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        });

        InputListener il = new InputListener();
        mInput.addTextChangedListener(il);
        mInput.setOnEditorActionListener(il);
    }

    private void configClock() {
        boolean showClock = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(SETTINGS_SHOW_CLOCK, false);
        if(showClock) {
            TextClock clock = findViewById(R.id.clock);
            clock.setVisibility(View.VISIBLE);
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
            clock.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

            clock.setOnClickListener(ignored -> {
                try {
                    Intent clockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                    clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(clockIntent);
                } catch (Exception e) {
                    Log.e("MainActivity", "Could not launch clock app", e);
                }
            });

            clock.setTextColor(getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                    .getInt(SETTINGS_TEXT_COLOR, Color.WHITE));
        } else {
            TextClock clock = findViewById(R.id.clock);
            clock.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int resourceId = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(SETTINGS_DARK_ICONS, false) ? R.drawable.clear_dark : R.drawable.clear;
        if(resourceId != mResourceId) {
            Drawable drawableImage = AppCompatResources.getDrawable(this, resourceId);
            ImageButton button = findViewById(R.id.clear_button);
            button.setImageDrawable(drawableImage);
            mResourceId = resourceId;
        }

        addAppsToLayout(Objects.requireNonNull(mInput.getText()).toString(), AppStatus.WHICHEVER);
        configClock();

        new VersionChecker(this).checkVersionAsynchronously();
    }

    @Override
    public void reload() {
        CharSequence charSeq = mInput.getText();
        if(charSeq != null) {
            String requestedName = charSeq.toString().toLowerCase().replace(" ", "");
            AppStatus appStatus = requestedName.equals("@hidden") ?
                    AppStatus.HIDDEN :
                    AppStatus.WHICHEVER;
            addAppsToLayout(charSeq.toString(), appStatus);
        }
    }

    private void addAppsToLayout(String requestedName, AppStatus status) {
        Log.d("app_manager", "Filtering apps, RequestedName: "+requestedName+", AppStatus: "+status);

        String query = requestedName.toLowerCase().replace(" ", "");
        AppFilter appFilter = new AppFilter(this);
        SharedPreferences preferences = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE);

        int searchId = ++mCurrentSearchId;
        mAdapter.clear(searchId);

        if (status == AppStatus.HIDDEN) {
            new AsyncIterator<>(mApplicationsInfo, app -> {
                String pkg = app.activityInfo.packageName;
                if (appFilter.isHidden(pkg)) {
                    mAdapter.addApp(new AppDisplayItem(app, AppStatus.HIDDEN, true), mPackageManager, searchId);
                }
            }).startProcessing();
        } else {
            // WHICHEVER case: show Favourites then Normals
            // First pass: Favourites
            getLoop(preferences, AppStatus.FAVOURITE, query, appFilter, searchId).startProcessing();

            // Second pass: Normals
            getLoop(preferences, AppStatus.NORMAL, query, appFilter, searchId).startProcessing();
        }
    }

    @NonNull
    private AsyncIterator<ResolveInfo> getLoop(SharedPreferences preferences, AppStatus requiredStatus, String query, AppFilter appFilter, int searchId) {
        return new AsyncIterator<>(mApplicationsInfo, app -> {
            String pkg = app.activityInfo.packageName;
            String appName = app.loadLabel(mPackageManager).toString().toLowerCase().replace(" ", "");
            AppStatus appStatus = Enum.valueOf(AppStatus.class, preferences.getString(pkg, "NORMAL"));

            if (appStatus == requiredStatus && appName.contains(query) && !pkg.equals(getPackageName()) && !appFilter.isHidden(pkg)) {
                mAdapter.addApp(new AppDisplayItem(app, requiredStatus, false), mPackageManager, searchId);
            }
        });
    }

    private final class InputListener implements TextView.OnEditorActionListener, TextWatcher {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager inputMethodManager = getSystemService(InputMethodManager.class);
                inputMethodManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
            }
            return true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String requestedName = s.toString().toLowerCase().replace(" ", "");
            AppStatus appStatus = requestedName.equals("@hidden") ?
                    AppStatus.HIDDEN :
                    AppStatus.WHICHEVER;
            addAppsToLayout(s.toString(), appStatus);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    public static class AppViewAdapter extends RecyclerView.Adapter<AbstractAppViewHolder> {
        private final List<AppDisplayItem> mApps = Collections.synchronizedList(new ArrayList<>());
        private final ReloadCallback mCallback;
        private final Consumer<String> mOnLaunchAppListener;
        private final Consumer<Runnable> mUiThreadRunner;
        private int mActiveSearchId = 0;

        public AppViewAdapter(ReloadCallback callback, Consumer<String> onLaunchAppListener, Consumer<Runnable> uiThreadRunner) {
            this.mCallback = callback;
            this.mOnLaunchAppListener = onLaunchAppListener;
            this.mUiThreadRunner = uiThreadRunner;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void addApp(AppDisplayItem app, PackageManager packageManager, int searchId) {
            if (searchId != mActiveSearchId) return;

            synchronized (mApps) {
                mApps.add(app);
                mApps.sort((a, b) -> {
                    // If the app is favourite, return it.
                    if (a.getStatus() == AppStatus.FAVOURITE && b.getStatus() != AppStatus.FAVOURITE)
                        return -1;
                    if (a.getStatus() != AppStatus.FAVOURITE && b.getStatus() == AppStatus.FAVOURITE)
                        return +1;
                    // Compare strings otherwise
                    return a.getResolveInfo().loadLabel(packageManager).toString()
                            .compareToIgnoreCase(b.getResolveInfo().loadLabel(packageManager).toString());
                });
            }
            mUiThreadRunner.accept(this::notifyDataSetChanged);
        }

        public void clear(int newSearchId) {
            mActiveSearchId = newSearchId;
            mApps.clear();
        }

        @Override
        public int getItemViewType(int position) {
            AppDisplayItem item = mApps.get(position);
            if (item.isSearchingForHidden()) return AppViewFactory.TYPE_HIDDEN;
            if (item.getStatus() == AppStatus.FAVOURITE) return AppViewFactory.TYPE_FAVOURITE;
            return AppViewFactory.TYPE_NORMAL;
        }

        @NonNull
        @Override
        public AbstractAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            AbstractAppView view = AppViewFactory.createAppView(parent.getContext(), mCallback, mOnLaunchAppListener, viewType);
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new AbstractAppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AbstractAppViewHolder holder, int position) {
            AbstractAppView aav = (AbstractAppView)holder.itemView;
            aav.bind(mApps.get(position).getResolveInfo());
        }

        @Override
        public int getItemCount() {
            return mApps.size();
        }
    }

    public static class AbstractAppViewHolder extends RecyclerView.ViewHolder {
        public AbstractAppViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
