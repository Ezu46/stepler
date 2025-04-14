package com.example.stepler;// NavigationDrawerHelper.java
import android.app.Activity;
import android.content.Context;
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
    private DrawerLayout drawer;
    private AppCompatActivity activity;
    private NavigationView navigationView;
    private NavigationListener navigationListener;
    private TextView tvHeaderName;
    private TextView tvHeaderEmail;

    public interface NavigationListener {
        boolean onNavigationItemSelected(MenuItem item);
    }

    public NavigationDrawerHelper(AppCompatActivity activity,
                                  int drawerLayoutId,
                                  int toolbarId,
                                  int navViewId) {
        this.activity = activity;

        // Инициализация элементов
        Toolbar toolbar = activity.findViewById(toolbarId);
        activity.setSupportActionBar(toolbar); // Теперь метод доступен

        drawer = activity.findViewById(drawerLayoutId);
        navigationView = activity.findViewById(navViewId);
        navigationView.setNavigationItemSelectedListener(this);

        // Настройка переключателя
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Инициализация элементов хедера
        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
            tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
        }
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

    public void updateHeader(String name, String email) {
        tvHeaderName.setText(name);
        tvHeaderEmail.setText(email);
    }

    public void handleBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            activity.onBackPressed();
        }
    }
}