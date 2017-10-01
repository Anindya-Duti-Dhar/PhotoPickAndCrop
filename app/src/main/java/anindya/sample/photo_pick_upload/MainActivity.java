package anindya.sample.photo_pick_upload;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;

public class MainActivity extends AppCompatActivity {
    SpaceTabLayout tabLayout;
    String TAG = getClass().getName();
    ViewPager mViewPager;
    FloatingActionButton fab;

    private DrawerLayout drawerLayout;
    TextView mNavHeaderTitle;

    //Defining Variables
    Toolbar mToolbar;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        // set a custom tint color for all system bars
        tintManager.setTintColor(Color.parseColor("#D50000"));

        // Set up the toolbar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.chats_toolbar));

        //add the fragments you want to display in a List
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentA());
        fragmentList.add(new FragmentB());
        fragmentList.add(new FragmentC());

        final CoordinatorLayout ParentLayout = (CoordinatorLayout) findViewById(R.id.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (SpaceTabLayout) findViewById(R.id.spaceTabLayout);

        tabLayout.initialize(mViewPager, getSupportFragmentManager(),
                fragmentList, savedInstanceState);

        tabLayout.setTabOneOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getCurrentPosition() == 0) {
                    Log.d(TAG, "TabOnclick " + " Contacts");
                }
            }
        });

        tabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getCurrentPosition() == 0) {
                    Log.d(TAG, "Onclick " + " Contacts");
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mToolbar.setTitle(getString(R.string.contacts_toolbar));
                }
                if (position == 1) {
                    mToolbar.setTitle(getString(R.string.chats_toolbar));
                }
                if (position == 2) {
                    mToolbar.setTitle(getString(R.string.profile_toolbar));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // init navigation drawer
        initNavigationDrawer();

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                if (searchView.isSearchOpen()) {
                    searchView.closeSearch();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }

    // initialize navigation drawer
    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    //Replacing the main content with home
                    case R.id.home:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Replacing the main content with Profile
                    case R.id.friends:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Replacing the main content with Profile
                    case R.id.profile:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Replacing the main content with Profile
                    case R.id.settings:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Replacing the main content with Profile
                    case R.id.help:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Replacing the main content with Profile
                    case R.id.logout:
                        Toast.makeText(getApplicationContext(), getString(R.string.click_action), Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;

                    //Rest of the case just show toast
                    default:
                        Toast.makeText(getApplicationContext(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        return true;

                }
                return true;
            }
        });

        View header = navigationView.getHeaderView(0);
        mNavHeaderTitle = (TextView)header.findViewById(R.id.user_name);
        mNavHeaderTitle.setText("Amelia Silvia");
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolar_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        }
        //close drawer it's open
        else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

}
