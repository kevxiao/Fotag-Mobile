package com.kevxiao.fotag;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer{

    // PUBLIC

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fotagModel.changeSearchMode("");
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_load) {
            loadAction();
            return true;
        } else if (id == R.id.action_reset) {
            fotagModel.clearImages();
            return true;
        } else if (id == R.id.action_clear) {
            fotagModel.clearSearchAndRating();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable observable, Object data) {
        ArrayList<ImageModel> images = fotagModel.getImages();
        int ratingFilter = fotagModel.getFilterMode();
        String[] searchFilters = fotagModel.getSearchMode().split("\\s+");
        imageModels.clear();
        for(ImageModel model : images) {
            if(!imageModels.contains(model) && model.getRating() >= ratingFilter) {
                if(searchFilters.length == 1 && searchFilters[0].length() == 0) {
                    imageModels.add(model);
                } else {
                    boolean missSearch = false;
                    String path = model.getPath();
                    String name;
                    if(path.lastIndexOf('.') > path.lastIndexOf('/')) {
                        name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                    } else {
                        name = path.substring(path.lastIndexOf('/') + 1);
                    }
                    for(String searchStr : searchFilters) {
                        if(!name.contains(searchStr)) {
                            missSearch = true;
                            break;
                        }
                    }
                    if(!missSearch) {
                        imageModels.add(model);
                    }
                }
            }
        }
        if (ratingFiler.getRating() != fotagModel.getFilterMode()) {
            ratingFiler.setRating(fotagModel.getFilterMode());
        }

        if(listView.getClass() == ListView.class) {
            ((ImageArrayAdapter)((ListView)listView).getAdapter()).notifyDataSetChanged();
        } else if(listView.getClass() == GridView.class) {
            ((ImageArrayAdapter)((GridView)listView).getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImages();
                } else {
                    Toast.makeText(this, R.string.read_ext_perm_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // PROTECTED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ratingFiler = (RatingBar) findViewById(R.id.rating_filter);
        if (ratingFiler != null) {
            ratingFiler.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    fotagModel.changeFilterMode((int)rating);
                }
            });
        }

        if(savedInstanceState != null && savedInstanceState.containsKey("savedModel") && savedInstanceState.getParcelable("savedModel") != null) {
            fotagModel = savedInstanceState.getParcelable("savedModel");
        } else {
            fotagModel = new FotagModel();
        }
        if (fotagModel != null) {
            if(fotagModel.countObservers() > 0) {
                fotagModel.deleteObservers();
            }
            fotagModel.addObserver(this);
        }
        imageModels = new ArrayList<>();

        listView = findViewById(R.id.contentView);
        ImageArrayAdapter adapter = new ImageArrayAdapter(this, R.layout.listview_item, imageModels, fotagModel);
        if (listView != null) {
            if(listView.getClass() == ListView.class) {
                ((ListView)listView).setAdapter(adapter);
            } else if(listView.getClass() == GridView.class) {
                ((GridView)listView).setAdapter(adapter);
            }
        }

        ImageView fsImg = (ImageView)findViewById(R.id.full_screen_img);
        if(fsImg != null) {
            fsImg.setBackground(new ColorDrawable(ContextCompat.getColor(fsImg.getContext(), android.R.color.transparent)));
            fsImg.setImageResource(android.R.color.transparent);
            fsImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setClickable(false);
                    v.setBackground(new ColorDrawable(ContextCompat.getColor(v.getContext(), android.R.color.transparent)));
                    ((ImageView) v).setImageResource(android.R.color.transparent);
                }
            });
            fsImg.setClickable(false);
        }

        handleIntent(getIntent());

        update(fotagModel, null);

        loadAction();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("savedModel", fotagModel);
    }

    // PRIVATE

    private FotagModel fotagModel;
    private ArrayList<ImageModel> imageModels;
    private RatingBar ratingFiler;
    private View listView;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            fotagModel.changeSearchMode(query);
        }
    }

    private void getImages() {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        String path;

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                path = cursor.getString(column_index_data);

                fotagModel.addImage(new ImageModel(path));
            }
            cursor.close();
        }
    }

    private void loadAction() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getImages();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
}
