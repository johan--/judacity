package ru.vpcb.contentprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.vpcb.contentprovider.data.FDCompetition;
import ru.vpcb.contentprovider.data.FDFixture;
import ru.vpcb.contentprovider.data.FDFixtures;
import ru.vpcb.contentprovider.data.FDPlayers;
import ru.vpcb.contentprovider.data.FDTable;
import ru.vpcb.contentprovider.data.FDTeam;
import ru.vpcb.contentprovider.data.FDTeams;
import ru.vpcb.contentprovider.data.IRetrofitAPI;

import ru.vpcb.contentprovider.dbase.FDContract;
import ru.vpcb.contentprovider.dbase.FDLoader;
import ru.vpcb.contentprovider.services.UpdateService;
import ru.vpcb.contentprovider.utils.FDUtils;
import timber.log.Timber;


import static ru.vpcb.contentprovider.utils.Constants.FD_BASE_URI;
import static ru.vpcb.contentprovider.utils.Constants.FD_TIME_FUTUTRE;
import static ru.vpcb.contentprovider.utils.Constants.FD_TIME_PAST;
import static ru.vpcb.contentprovider.utils.FootballUtils.isOnline;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The flag is true it Timber.Tree is exists
     */
    public static boolean mIsTimber;


    private ProgressBar mProgressBar;
    private FloatingActionButton mFab;
    private FloatingActionButton mFab2;

    private IRetrofitAPI mRetrofitAPI;
    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;
    private MessageReceiver mMessageReceiver;

    private Map<Integer, FDCompetition> map = new HashMap<>();
    private Map<Integer, List<Integer>> mapTeamKeys = new HashMap<>();
    private Map<Integer, FDTeam> mapTeams = new HashMap<>();
    private Map<Integer, List<Integer>> mapFixtureKeys = new HashMap<>();
    private Map<Integer, FDFixture> mapFixtures = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // log
        if (!mIsTimber) {
            Timber.plant(new Timber.DebugTree());
            mIsTimber = true;
        }


        mFab = findViewById(R.id.fab);
        mFab2 = findViewById(R.id.fab2);
        mProgressBar = findViewById(R.id.progress_bar);


        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                Intent intent = new Intent(MainActivity.this, BottomActivity.class);
//                startActivity(intent);


                FDCompetition competition = new FDCompetition(524, "MyCompetition", "New League",
                        "2017", 1, 12, 0, 24,
                        Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
                try {
                    FDUtils.writeCompetition(MainActivity.this, competition, false);
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


            }
        });


        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                startRetrofitLoader();
//                startRetrofitLoaderC();  //ok
//                startRetrofitLoaderCT();  // ok
//                startRetrofitLoaderCF(); // ok
//                startRetrofitLoaderCFM(); // ok
//                startRetrofitLoaderCFT(); // ok
//                startRetrofitLoaderLT(); // ok
//                startRetrofitLoaderT(); // ok
//                startRetrofitLoaderF(); // ok
//                startRetrofitLoaderFT(); // ok
//                startRetrofitLoaderP();
                refresh(getString(R.string.action_update));

            }
        });

        setupReceiver();

        getSupportLoaderManager().initLoader(FDContract.CpEntry.LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(FDContract.CpTmEntry.LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(FDContract.CpFxEntry.LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(FDContract.TmEntry.LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(FDContract.FxEntry.LOADER_ID, null, this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.prefs.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    /**
     * Shows ProgressBar View object
     */
    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hides ProgressBar and ErrorMessage Views
     */
    private void showResult() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }


    private String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.ENGLISH, "%4d", calendar.get(Calendar.YEAR));
    }

    private void startRetrofitLoaderC() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();

// setup data
        String season = getCurrentYear();
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getCompetitions(season).enqueue(new Callback<List<FDCompetition>>() {
            @Override
            public void onResponse(@Nullable Call<List<FDCompetition>> call, @NonNull Response<List<FDCompetition>> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                List<FDCompetition> competitions = response.body();

// test!!! записать в ContentProvider
//                RecipeUtils.bulkInsertBackground(mContext.getContentResolver(), getSupportLoaderManager(), list, mLoaderDb);
// test!!!  сохранить  timestamp
//                saveReLoadTimePreference();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<List<FDCompetition>> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }

    private void startRetrofitLoaderCT() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();

// setup data
        int id = 450;
        String competition = String.format(Locale.ENGLISH, "%d", id);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTeams(competition).enqueue(new Callback<FDTeams>() {
            @Override
            public void onResponse(@Nullable Call<FDTeams> call, @NonNull Response<FDTeams> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDTeams teams = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDTeams> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }


    private void startRetrofitLoaderCF() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 446;
        String competition = String.format(Locale.ENGLISH, "%d", id);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getFixtures(competition).enqueue(new Callback<FDFixtures>() {
            @Override
            public void onResponse(@Nullable Call<FDFixtures> call, @NonNull Response<FDFixtures> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDFixtures fixtures = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDFixtures> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }


    private void startRetrofitLoaderCFM() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 446;
        String competition = String.format(Locale.ENGLISH, "%d", id);
        int matchDay = 25;
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getFixturesMatch(competition, matchDay).enqueue(new Callback<FDFixtures>() {
            @Override
            public void onResponse(@Nullable Call<FDFixtures> call, @NonNull Response<FDFixtures> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDFixtures fixtures = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDFixtures> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }

    private void startRetrofitLoaderCFT() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 446;
        String competition = String.format(Locale.ENGLISH, "%d", id);
        int nDays = 50;
        String nTime = FD_TIME_PAST;

        String timeFrame = String.format(Locale.ENGLISH, "%s%d", nTime, nDays);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getFixturesTime(competition, timeFrame).enqueue(new Callback<FDFixtures>() {
            @Override
            public void onResponse(@Nullable Call<FDFixtures> call, @NonNull Response<FDFixtures> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDFixtures fixtures = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDFixtures> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }

    private void startRetrofitLoaderLT() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 446;
        String competition = String.format(Locale.ENGLISH, "%d", id);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTable(competition).enqueue(new Callback<FDTable>() {
            @Override
            public void onResponse(@Nullable Call<FDTable> call, @NonNull Response<FDTable> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDTable table = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDTable> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }


    private void startRetrofitLoaderT() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 524;
        String team = String.format(Locale.ENGLISH, "%d", id);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTeam(team).enqueue(new Callback<FDTeam>() {
            @Override
            public void onResponse(@Nullable Call<FDTeam> call, @NonNull Response<FDTeam> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDTeam team = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDTeam> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }


    private void startRetrofitLoaderF() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 524;
        String team = String.format(Locale.ENGLISH, "%d", id);
// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTeamFixtures(team).enqueue(new Callback<FDFixtures>() {
            @Override
            public void onResponse(@Nullable Call<FDFixtures> call, @NonNull Response<FDFixtures> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDFixtures fixtures = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDFixtures> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }

    private int counter = 0;

    private void startRetrofitLoaderFT() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 524;
        String team = String.format(Locale.ENGLISH, "%d", id);
        int nDays = 5;
        String nTime = FD_TIME_FUTUTRE;
        String season = getCurrentYear();
        String timeFrame = String.format(Locale.ENGLISH, "%s%d", nTime, nDays);

// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }
                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTeamFixtures(team, timeFrame, season).enqueue(new Callback<FDFixtures>() {
            @Override
            public void onResponse(@Nullable Call<FDFixtures> call, @NonNull Response<FDFixtures> response) {
                if (response.body() == null) {
                    showResult();
                    Timber.d(counter++ + ": " + response);
                    return;
                }
                FDFixtures fixtures = response.body();

                Timber.d(counter++ + ": " + fixtures);
                showResult();

            }

            @Override
            public void onFailure(@Nullable Call<FDFixtures> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }

    private void startRetrofitLoaderP() {
        if (!isOnline(this)) {
            return;
        }
        showProgress();
// setup data
        int id = 338;
        final String team = String.format(Locale.ENGLISH, "%d", id);

// setup okHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }

                }).build();
// setup Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(IRetrofitAPI.class);
        mRetrofitAPI.getTeamPlayers(team).enqueue(new Callback<FDPlayers>() {
            @Override
            public void onResponse(@Nullable Call<FDPlayers> call, @NonNull Response<FDPlayers> response) {
                if (response.body() == null) {
                    showResult();
                    return;
                }
                FDPlayers players = response.body();
                showResult();
            }

            @Override
            public void onFailure(@Nullable Call<FDPlayers> call, @NonNull Throwable t) {
                Timber.d(t.getMessage());
                showResult();
            }
        });
    }


    private void refresh(String action) {
        Intent intent = new Intent(action, null, this, UpdateService.class);
        startService(intent);
    }

    private void setupReceiver() {
        mMessageReceiver = new MessageReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_update_started));
        intentFilter.addAction(getString(R.string.broadcast_update_finished));
        intentFilter.addAction(getString(R.string.broadcast_no_network));
        registerReceiver(mMessageReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return FDLoader.getInstance(this, id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader == null || loader.getId() <= 0 || data == null || data.getCount() == 0) return;

        switch (loader.getId()) {
            case FDContract.CpEntry.LOADER_ID:
                map = FDUtils.readCompetitions(data);
                break;

            case FDContract.CpTmEntry.LOADER_ID:
                mapTeamKeys = FDUtils.readCompetitionTeams(data);
                break;

            case FDContract.TmEntry.LOADER_ID:
                mapTeams = FDUtils.readTeams(data);
                break;

            case FDContract.CpFxEntry.LOADER_ID:
                mapFixtureKeys = FDUtils.readCompetitionFixtures(data);
                break;

            case FDContract.FxEntry.LOADER_ID:
                mapFixtures = FDUtils.readFixtures(data);
                break;

            case FDContract.TbEntry.LOADER_ID:
                break;

            case FDContract.PlEntry.LOADER_ID:
                break;

            default:
                throw new IllegalArgumentException("Unknown id: " + loader.getId());
        }
        boolean isUpdated = FDUtils.loadCompetitions(map,mapTeamKeys,mapTeams,mapFixtureKeys,mapFixtures);
        if(isUpdated) {
// test!!!
            Timber.d("RecyclerView or ViewPager adapter notification update: "+map.size());
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // an Intent broadcast.
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals(context.getString(R.string.broadcast_update_started))) {
                    Toast.makeText(context, "Broadcast message: update started", Toast.LENGTH_SHORT).show();

                } else if (action.equals(context.getString(R.string.broadcast_update_finished))) {
                    Toast.makeText(context, "Broadcast message: update finished", Toast.LENGTH_SHORT).show();
                } else if (action.equals(context.getString(R.string.broadcast_no_network))) {
                    Toast.makeText(context, "Broadcast message: no network", Toast.LENGTH_SHORT).show();
                } else {
                    throw new UnsupportedOperationException("Not yet implemented");
                }

            }

        }
    }


}
