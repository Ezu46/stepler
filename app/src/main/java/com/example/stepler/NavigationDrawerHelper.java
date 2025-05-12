package com.example.stepler;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class NavigationDrawerHelper implements NavigationView.OnNavigationItemSelectedListener {
    private final DrawerLayout drawer;
    private final AppCompatActivity activity;
    private final NavigationView navigationView;
    private final TextView tvHeaderName;
    private final TextView tvHeaderEmail;
    private NavigationListener navigationListener;

    public interface NavigationListener {
        boolean onNavigationItemSelected(MenuItem item);
    }

    public NavigationDrawerHelper(AppCompatActivity activity,
                                  int drawerLayoutId,
                                  int toolbarId,
                                  int navViewId) {
        this.activity = activity;

        // 1) Toolbar
        Toolbar toolbar = activity.findViewById(toolbarId);
        activity.setSupportActionBar(toolbar);

        // 2) Drawer и NavigationView
        drawer = activity.findViewById(drawerLayoutId);
        navigationView = activity.findViewById(navViewId);
        navigationView.setNavigationItemSelectedListener(this);

        // 3) Если header не задан в XML через app:headerLayout, инфлейтим его вручную:
        if (navigationView.getHeaderCount() == 0) {
            navigationView.inflateHeaderView(R.layout.nav_header);
        }

        // 4) Берём view из header и находим TextView
        View headerView = navigationView.getHeaderView(0);
        tvHeaderName  = headerView.findViewById(R.id.tvHeaderName);
        tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // 5) Настраиваем "гамбургер"
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (navigationListener != null) {
            return navigationListener.onNavigationItemSelected(item);
        }
        return false;
    }

    /** Закрыть drawer */
    public void closeDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    /** Проверка, открыт ли drawer */
    public boolean isDrawerOpen() {
        return drawer.isDrawerOpen(GravityCompat.START);
    }

    /** Обновить шапку — ставим проверку на null для безопасности */
    public void updateHeader(String name, String email) {
        if (tvHeaderName  != null) tvHeaderName.setText(name);
        if (tvHeaderEmail != null) tvHeaderEmail.setText(email);
    }

    /** При back-пресс закрыть drawer или уехать из activity */
    public void handleBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            activity.onBackPressed();
        }
    }
}