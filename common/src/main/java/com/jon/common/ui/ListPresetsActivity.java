package com.jon.common.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.jon.common.R;
import com.jon.common.presets.OutputPreset;
import com.jon.common.presets.PresetRepository;
import com.jon.common.utils.Protocol;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ListPresetsActivity
        extends ServiceBoundActivity
        implements ListPresetsAdapter.ItemClickListener {

    private final Map<Protocol, PresetRecyclerInfo> recyclerViewMap = new HashMap<Protocol, PresetRecyclerInfo>() {{
        put(Protocol.SSL, new PresetRecyclerInfo(R.id.listPresetsSslRecyclerView, R.id.sslNoneFound));
        put(Protocol.TCP, new PresetRecyclerInfo(R.id.listPresetsTcpRecyclerView, R.id.tcpNoneFound));
        put(Protocol.UDP, new PresetRecyclerInfo(R.id.listPresetsUdpRecyclerView, R.id.udpNoneFound));
    }};

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PresetRepository repository = PresetRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_presets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.toolbarHeaderListPresets);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        for (Map.Entry<Protocol, PresetRecyclerInfo> entry : recyclerViewMap.entrySet()) {
            final Protocol protocol = entry.getKey();
            final PresetRecyclerInfo info = entry.getValue();
            final RecyclerView recyclerView = findViewById(info.recyclerViewId);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(presets -> {
                        info.adapter = new ListPresetsAdapter(this, presets);
                        info.adapter.setClickListener(this);
                        recyclerView.setAdapter(info.adapter);
                        final int visibility = presets.isEmpty() ? View.VISIBLE : View.GONE;
                        findViewById(info.emptyMessageId).setVisibility(visibility);
                    }));
        }

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditPresetActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_presets_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int resId = item.getItemId();
        if (resId == android.R.id.home) {
            onBackPressed();
        } else if (resId == R.id.delete_all) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Presets")
                    .setMessage("Clear all listed presets? The built-in defaults will still remain.")
                    .setPositiveButton(android.R.string.ok, (dialog, buttonId) -> {
                        repository.deleteDatabase();
                        refresh();
                    }).setNegativeButton(android.R.string.cancel, (dialog, buttonId) -> dialog.dismiss())
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickEditItem(OutputPreset preset) {
        /* Build an intent containing all the values we'll need to populate the EditPreset screen */
        Intent intent = new Intent(this, EditPresetActivity.class);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL, preset.protocol.get());
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_ALIAS, preset.alias);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_ADDRESS, preset.address);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PORT, preset.port);
        if (preset.protocol == Protocol.SSL) {
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES, new String(preset.clientCert));
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD, preset.clientCertPassword);
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES, new String(preset.trustStore));
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD, preset.trustStorePassword);
        }
        startActivity(intent);
    }

    @Override
    public void onClickDeleteItem(OutputPreset preset) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Preset")
                .setMessage("Are you sure you want to delete " + preset.alias + "?")
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    repository.deletePreset(preset);
                    refresh();
                })
                .show();
    }

    private void refresh() {
        for (Map.Entry<Protocol, PresetRecyclerInfo> entry : recyclerViewMap.entrySet()) {
            final Protocol protocol = entry.getKey();
            final PresetRecyclerInfo info = entry.getValue();
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(presets -> {
                        info.adapter.updatePresets(presets);
                        final int visibility = presets.isEmpty() ? View.VISIBLE : View.GONE;
                        findViewById(info.emptyMessageId).setVisibility(visibility);
                    }));
        }
    }

    private static class PresetRecyclerInfo {
        int recyclerViewId, emptyMessageId;
        ListPresetsAdapter adapter;
        PresetRecyclerInfo(@IdRes int recyclerViewId, @IdRes int emptyMessageId) {
            this.recyclerViewId = recyclerViewId;
            this.emptyMessageId = emptyMessageId;
        }
    }
}
