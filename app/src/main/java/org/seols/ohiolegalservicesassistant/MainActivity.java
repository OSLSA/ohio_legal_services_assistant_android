package org.seols.ohiolegalservicesassistant;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchDialogFragment.OnUpdateSearchListener {

    public static String PACKAGE_NAME;
    private MenuItem search;
    private String bookName;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PACKAGE_NAME = getApplicationContext().getPackageName();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        setLogoClick(navigationView);
        navigationView.setNavigationItemSelectedListener(this);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setFragment(new HomeFragment(), "home", getResources().getString(R.string.app_name), null);

    }

    private void setLogoClick(NavigationView navigationView) {
        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(headerListener);
    }

    public void recordAnalytics(Bundle bundle) {
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        Log.d("Analytics Logged: ", bundle.toString());
    }

    private View.OnClickListener headerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setFragment(new HomeFragment(), "home", getResources().getString(R.string.app_name), null);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        search = menu.findItem(R.id.search);
        search.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.search) {
            String bookTitle = getBookTitle();
            showSearchDialog(bookTitle);
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private String getBookTitle() {
        String[] tags = getResources().getStringArray(R.array.rules_tags);
        int position = -1;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].equals(bookName)) position = i;
        }
        String[] titles = getResources().getStringArray(R.array.Rules);
        return titles[position];
    }

    private void showSearchDialog(String title) {
        FragmentManager fm = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString("title", title);
        SearchDialogFragment dialog = new SearchDialogFragment();
        dialog.setArguments(args);
        dialog.show(fm, "SearchDialog");
    }

    @Override
    public void onSearchSubmit(String dialogSearch) {
        // handle cancel pushed on dialog
        if (dialogSearch.equals("-1")) return;
        // search rule book for term
        Bundle args = new Bundle();
        // TODO these all have to be dynamic, in for just debugging at the moment
        args.putString("bookName", bookName);
        args.putString("searchTerm", dialogSearch);
        setFragment(new RulesSearchFragment(), "SEARCH", "Search: " + dialogSearch, args);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_forms) {
            setFragment(new FormsListFragment(), "FORMS", "Forms", null);
        } else if (id == R.id.nav_calculators) {
            setFragment(new CalculatorsFragment(), "CALCULATORS", "Calculators", null);
        } else if (id == R.id.nav_about) {
            setFragment(new AboutFragment(), "ABOUT", "About", null);
        } else if (id == R.id.nav_resources) {
            setFragment(new LocalResourcesFragment(), "RESOURCES", "Local Resources", null);
        } else if (id == R.id.nav_rules) {
            setFragment(new RulesAllTitlesFragment(), "RULES", "Rules", null);
        } else if (id == R.id.nav_settings) {
            setFragment(new SettingsFragment(), "SETTING", "Settings", null);
        } else if (id == R.id.pika_mobile) {
            String url = PreferenceManager.getDefaultSharedPreferences(this).getString("pikaURL", "");
            if (url.equals("")) {
                Toast toast = Toast.makeText(this, "You have to set the address for your mobile version of Pika in the settings", Toast.LENGTH_LONG);
                toast.show();
            } else {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    // TODO set back button navigation tool

    /**
     * Sets the main content area to the chosen fragment
     * @param name name of the fragment
     * @param tag log tag
     * @param title Title to set the activity bar to
     * @param args Arguments to be passed to new fragment, can be null
     */
    public void setFragment(Fragment name, String tag, String title, Bundle args) {
        getSupportActionBar().setTitle(title);
        Log.d("Tag: ", tag);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle argsFinal = new Bundle();
        if (args != null) {
            if (!args.getString("bookName", "none").equals("none")) bookName = args.getString("bookName");
            argsFinal = args;
        }

        argsFinal.putString("title", title);
        name.setArguments(argsFinal);


        if (search != null) {
            if (tag.equals("RULE TOC") || tag.equals("RULE DETAIL") || tag.equals("SEARCH")) {
                search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                search.setVisible(true);
            } else {
                search.setVisible(false);
            }
        }

        ft.replace(R.id.nav_content_frame, name, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }
}
