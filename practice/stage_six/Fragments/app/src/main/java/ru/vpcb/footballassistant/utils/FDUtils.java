package ru.vpcb.footballassistant.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.vpcb.footballassistant.R;
import ru.vpcb.footballassistant.data.FDCompetition;
import ru.vpcb.footballassistant.data.FDFixture;
import ru.vpcb.footballassistant.data.FDFixtures;
import ru.vpcb.footballassistant.data.FDLink;
import ru.vpcb.footballassistant.data.FDPlayer;
import ru.vpcb.footballassistant.data.FDPlayers;
import ru.vpcb.footballassistant.data.FDStanding;
import ru.vpcb.footballassistant.data.FDStandingGroup;
import ru.vpcb.footballassistant.data.FDTable;
import ru.vpcb.footballassistant.data.FDTeam;
import ru.vpcb.footballassistant.data.FDTeams;
import ru.vpcb.footballassistant.data.IFDRetrofitAPI;
import ru.vpcb.footballassistant.data.PostProcessingEnabler;
import ru.vpcb.footballassistant.dbase.FDContract;
import ru.vpcb.footballassistant.dbase.FDDbHelper;
import timber.log.Timber;

import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_COMPETITIONS;
import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_CP_FIXTURES;
import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_CP_TEAMS;
import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_FIXTURES;
import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_TABLES;
import static ru.vpcb.footballassistant.dbase.FDContract.MATCH_PARAMETERS_TEAMS;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_INT_VALUE;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_STRING;
import static ru.vpcb.footballassistant.utils.Config.FORMAT_MATCH_SCORE;
import static ru.vpcb.footballassistant.utils.Config.FORMAT_MATCH_TIME_WIDGET;
import static ru.vpcb.footballassistant.utils.Config.DATE_FULL_PATTERN;
import static ru.vpcb.footballassistant.utils.Config.EXCEPTION_CODE_7;
import static ru.vpcb.footballassistant.utils.Config.PATTERN_DATE_SQLITE;
import static ru.vpcb.footballassistant.utils.Config.PATTERN_MATCH_DATE;
import static ru.vpcb.footballassistant.utils.Config.PATTERN_MATCH_DATE_WIDGET;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_LONG_DASH;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_MATCH_SCORE;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_MATCH_TIME;
import static ru.vpcb.footballassistant.utils.Config.FD_BASE_URI;
import static ru.vpcb.footballassistant.utils.Config.FD_API_KEY;
import static ru.vpcb.footballassistant.utils.Config.LOAD_DB_DELAY;
import static ru.vpcb.footballassistant.utils.Config.EXCEPTION_CODE_1;
import static ru.vpcb.footballassistant.utils.Config.EXCEPTION_CODE_4;
import static ru.vpcb.footballassistant.utils.Config.EXCEPTION_CODE_5;
import static ru.vpcb.footballassistant.utils.Config.EXCEPTION_CODE_6;
import static ru.vpcb.footballassistant.utils.Config.PATTERN_MATCH_TIME;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_A;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_B;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_C;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_D;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_E;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_F;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_G;
import static ru.vpcb.footballassistant.utils.Config.TABLE_GROUP_H;
import static ru.vpcb.footballassistant.utils.Config.UPDATE_SERVICE_PROGRESS;
import static ru.vpcb.footballassistant.utils.FootballUtils.formatString;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 28-Jan-18
 * Email: vadim.v.voronov@gmail.com
 */

public class FDUtils {
    //    private static final SimpleDateFormat formatDate =
//            new SimpleDateFormat(DATE_FULL_PATTERN, Locale.ENGLISH);
    private static final SimpleDateFormat formatMatchDateWidget =
            new SimpleDateFormat(PATTERN_MATCH_DATE_WIDGET, Locale.ENGLISH);
    private static final SimpleDateFormat formatMatchDate =
            new SimpleDateFormat(PATTERN_MATCH_DATE, Locale.ENGLISH);
    private static final SimpleDateFormat formatMatchTime =
            new SimpleDateFormat(PATTERN_MATCH_TIME, Locale.ENGLISH);

    private static final SimpleDateFormat formatSQLiteDate =
            new SimpleDateFormat(PATTERN_DATE_SQLITE, Locale.ENGLISH);


    // stings
    public static int formatId(String href) throws NumberFormatException {
        try {
            return Integer.valueOf(href.substring(href.lastIndexOf("/") + 1));
        } catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
            return EMPTY_INT_VALUE;
        }
    }


    public static String formatIntToString(int value) {
        if (value < 0) return EMPTY_LONG_DASH;

        return String.valueOf(value);
    }


    public static Calendar getCalendarFromString(String s) {
        Date date = formatDateFromSQLite(s);
        if (date == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }


    public static String formatMatchTimeWidget(String s) {
        if (s == null || s.isEmpty()) return EMPTY_MATCH_TIME;

        Calendar c = getCalendarFromString(s);
        if (c == null) return EMPTY_MATCH_TIME;

        return String.format(Locale.ENGLISH, FORMAT_MATCH_TIME_WIDGET,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public static String formatMatchDateWidget(String s) {
        if (s == null || s.isEmpty()) return EMPTY_LONG_DASH;
        Date date = formatDateFromSQLite(s);
        if (date == null) return EMPTY_LONG_DASH;
        return formatMatchDateWidget.format(date);
    }

    public static String formatMatchDate(String s) {
        if (s == null || s.isEmpty()) return EMPTY_LONG_DASH;
        Date date = formatDateFromSQLite(s);
        if (date == null) return EMPTY_LONG_DASH;
        return formatMatchDate.format(date);
    }

    public static String formatMatchTime(String s) {
        if (s == null || s.isEmpty()) return EMPTY_LONG_DASH;
        Date date = formatDateFromSQLite(s);
        if (date == null) return EMPTY_LONG_DASH;
        return formatMatchTime.format(date);
    }

    public static String formatMatchScore(int home, int away) {
        if (home < 0 || away < 0) return EMPTY_MATCH_SCORE;
        return String.format(Locale.ENGLISH, FORMAT_MATCH_SCORE, home, away);
    }

    // move to pattern SQLIte
    public static String formatDateToSQLite(String s) {
        if (s == null) return EMPTY_STRING;
        return s.replace("T", " ").replace("Z", "");
    }


    // int

    // date and time

    public static Comparator<FDFixture> cFx = new Comparator<FDFixture>() {
        @Override
        public int compare(FDFixture o1, FDFixture o2) {
            if (o1 == null || o2 == null ||
                    o1.getDate() == null || o2.getDate() == null)
                throw new IllegalArgumentException();

            Date dateO1 = FDUtils.formatDateFromSQLite(o1.getDate());
            Date dateO2 = FDUtils.formatDateFromSQLite(o2.getDate());
            if (dateO1 == null || dateO2 == null) throw new IllegalArgumentException();

            return dateO1.compareTo(dateO2);
        }
    };

    public static void setZeroTime(Calendar c) {
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    }


    public static void setDay(Calendar c, Date date) {
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    }


    public static Date formatDateFromSQLite(String s) {
        try {
            return formatSQLiteDate.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatDateToSQLite(Date date) {
        try {
            return formatSQLiteDate.format(date);
        } catch (NullPointerException e) {
            return null;
        }
    }


    // shared preferences
    public static boolean getPrefBool(Context context, int keyId, int valueId) {
        Resources res = context.getResources();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean value = sp.getBoolean(res.getString(keyId), res.getBoolean(valueId));
        return value;
    }

    public static int getPrefInt(Context context, int keyId, int defaultId) {
        Resources res = context.getResources();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int value = sp.getInt(res.getString(keyId), res.getInteger(defaultId));
        return value;
    }

    public static void setRefreshTime(Context context) {
        Resources res = context.getResources();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(res.getString(R.string.pref_data_update_time_key), FDUtils.currentTimeMinutes());
        editor.apply();
    }

    public static boolean isFootballDataRefreshed(Context context) {
        int time = getPrefInt(context,
                R.string.pref_data_update_time_key,
                R.integer.pref_data_update_time_default);
        int delay = getPrefInt(context,
                R.string.pref_data_delay_time_key,
                R.integer.pref_data_delay_time_default);

        return FDUtils.currentTimeMinutes() - time < delay;
    }

    public static boolean isNewsDataRefreshed(Context context) {
        int time = getPrefInt(context,
                R.string.pref_news_update_time_key,
                R.integer.pref_news_update_time_default);
        int delay = getPrefInt(context,
                R.string.pref_news_delay_time_key,
                R.integer.pref_news_delay_time_default);

        return FDUtils.currentTimeMinutes() - time < delay;
    }

    // data
//    private static boolean isRefreshed(Context context, Date lastRefresh) {
//        if (lastRefresh == null) return false;
//
//        long delay = TimeUnit.MINUTES.toMillis(
//                getPrefInt(context, R.string.pref_data_delay_time_key, R.integer.pref_data_delay_time_default));
//        long currentTime = Calendar.getInstance().getTimeInMillis();
//        long refreshTime = lastRefresh.getTime();
//
//        return currentTime - refreshTime < delay;
//    }


    // database
    public static Uri buildTableNameUri(String tableName) {
        return FDContract.BASE_CONTENT_URI.buildUpon().appendPath(tableName).build();
    }

    public static Uri buildItemIdUri(String tableName, long id) {
        return FDContract.BASE_CONTENT_URI.buildUpon().appendPath(tableName).appendPath(Long.toString(id)).build();
    }

    public static Uri buildItemIdUri(String tableName, long id, long id2) {
        return FDContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(tableName)
                .appendPath(Long.toString(id))
                .appendPath(Long.toString(id2))
                .build();
    }

    public static int currentTimeMinutes() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTimeInMillis());
    }

    private static Date currentTimeMinutesToDate() {
        return minutesToDate(currentTimeMinutes());
    }

    private static int dateToMinutes(Date date) {
        if (date == null) return 0;
        return (int) TimeUnit.MILLISECONDS.toMinutes(date.getTime());
    }

    public static Date minutesToDate(int time) {
        return new Date(TimeUnit.MINUTES.toMillis(time));
    }

    private static String formatLoad(Context context, int code, FDCompetition competition, String message) {
        int id = -1;
        if (competition != null) id = competition.getId();
        return context.getString(R.string.load_database, code, id, message);
    }

    private static String formatLoad(Context context, int code, FDTeam team, String message) {
        int id = -1;
        if (team == null) id = team.getId();
        return context.getString(R.string.load_database, code, id, message);
    }

    private static String formatLoad(Context context, int code, int id, String message) {
        return context.getString(R.string.load_database, code, id, message);
    }


    private static String formatWrite(Context context, int code, int id, String message) {
        return context.getString(R.string.write_database, code, id, message);
    }


    // data
    private static boolean setListTeams(FDCompetition competition,
                                        Map<Integer, List<Integer>> mapKeys,
                                        Map<Integer, FDTeam> mapTeams) {
        if (competition == null || competition.getId() <= 0 ||
                mapKeys == null || mapKeys.isEmpty() ||
                mapTeams == null || mapTeams.isEmpty()) return false;

        List<Integer> listKeys = mapKeys.get(competition.getId());
//        if (listKeys == null) return false;
        if (listKeys == null) return true;   // map does not have keys by source

        List<FDTeam> list = new ArrayList<>();
        for (Integer key : listKeys) {
            FDTeam team = mapTeams.get(key);
            if (team == null || team.getId() != key) continue;
            list.add(team);
        }
//        if (list == null || list.isEmpty()) return false;
        competition.setTeams(list);
        return true;
    }

    private static boolean setListFixtures(FDCompetition competition,
                                           Map<Integer, List<Integer>> mapKeys,
                                           Map<Integer, FDFixture> mapFixtures) {
        if (competition == null || competition.getId() <= 0 ||
                mapKeys == null || mapKeys.isEmpty() ||
                mapFixtures == null || mapFixtures.isEmpty()) return false;


        List<Integer> listKeys = mapKeys.get(competition.getId());
//        if (listKeys == null) return false;
        if (listKeys == null) return true;  // map does not have keys by source
        List<FDFixture> list = new ArrayList<>();
        for (Integer key : listKeys) {
            FDFixture fixture = mapFixtures.get(key);
            if (fixture == null || fixture.getId() != key) continue;
            list.add(fixture);
        }
//        if (list == null || list.isEmpty()) return false;
        competition.setFixtures(list);
        return true;
    }


    // competitions
    public static void getCompetitionTeams(Context context,
                                           Map<Integer, FDCompetition> competitions,
                                           Map<Integer, List<Integer>> mapKeys,
                                           Map<Integer, FDTeam> mapTeams) {

        for (FDCompetition competition : competitions.values()) {
            setListTeams(competition, mapKeys, mapTeams);
        }
    }

    public static void getCompetitionFixtures(Context context,
                                              Map<Integer, FDCompetition> competitions,
                                              Map<Integer, List<Integer>> mapKeys,
                                              Map<Integer, FDFixture> mapFixtures) {

        for (FDCompetition competition : competitions.values()) {
            setListFixtures(competition, mapKeys, mapFixtures);

        }
    }


    // read
    // competitions
    public static Map<Integer, FDCompetition> readCompetitions(Context context) {
        Uri uri = FDContract.CpEntry.CONTENT_URI;                               // вся таблица
//        String sortOrder = FDContract.CpEntry.COLUMN_COMPETITION_ID + " ASC";   // sort by id
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_COMPETITIONS].getSortOrder();

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, FDCompetition> map = readCompetitions(cursor);
        cursor.close();      // disables notifications
        return map;
    }

    // competition
    private static FDCompetition readCompetition(Cursor cursor) {
        int id = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_COMPETITION_ID);
        if (id <= 0) return null;

        String caption = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_CAPTION);
        String league = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_LEAGUE);
        String year = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_YEAR);
        int currentMatchDay = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_CURRENT_MATCHDAY);
        int numberOfMatchDays = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_MATCHDAYS);
        int numberOfTeams = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_TEAMS);
        int numberOfGames = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_GAMES);
        String lastUpdated = cursor.getString(FDDbHelper.ICpEntry.COLUMN_LAST_UPDATE);
// TODO Check Date Format
        FDCompetition competition = new FDCompetition(id, caption, league,
                year, currentMatchDay, numberOfMatchDays, numberOfTeams,
                numberOfGames, lastUpdated);

        return competition;
    }

    public static FDCompetition readCompetition(Context context, int id) {
        if (id <= 0) return null;
        Uri uri = FDContract.CpEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();                               // вся таблица
//        String sortOrder = FDContract.CpEntry.COLUMN_COMPETITION_ID + " ASC";   // sort by id
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_COMPETITIONS].getSortOrder();

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        FDCompetition competition = readCompetition(cursor);
        cursor.close();
        return competition;
    }


    // competition_teams
    public static Map<Integer, List<Integer>> readCompetitionTeams(Context context) {
        Uri uri = FDContract.CpTmEntry.CONTENT_URI;                                     // all table
//        String sortOrder = FDContract.CpTmEntry.COLUMN_COMPETITION_ID + " ASC";          // sort by id
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_CP_TEAMS].getSortOrder();


        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, List<Integer>> map = readCompetitionTeams(cursor);
        cursor.close();     // disables notifications
        return map;
    }

    // teams
    public static Map<Integer, FDTeam> readTeams(Context context) {
        Uri uri = FDContract.TmEntry.CONTENT_URI;                               // вся таблица
//        String sortOrder = FDContract.TmEntry.COLUMN_TEAM_ID + " ASC";   // sort by id
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_TEAMS].getSortOrder();


        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, FDTeam> map = readTeams(cursor);
        cursor.close();     // disables notifications
        return map;
    }


    // fixtures
    // competition_fixture
    public static Map<Integer, List<Integer>> readCompetitionFixtures(Context context) {
        Uri uri = FDContract.CpFxEntry.CONTENT_URI;                                     // all table
//        String sortOrder = FDContract.CpFxEntry.COLUMN_COMPETITION_ID + " ASC";          // sort by id
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_CP_FIXTURES].getSortOrder();


        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, List<Integer>> map = readCompetitionFixtures(cursor);
        cursor.close();     // disables notifications
        return map;
    }

    // fixture
    public static Map<Integer, FDFixture> readFixtures(Context context) {
        Uri uri = FDContract.FxEntry.CONTENT_URI;                               // вся таблица
//        String sortOrder = FDContract.CpEntry.COLUMN_LAST_UPDATE + " ASC";
//        String sortOrder = FDContract.FxEntry.COLUMN_FIXTURE_ID + " ASC";   // sort by id
//        String sortOrder = "date("+FDContract.FxEntry.COLUMN_FIXTURE_ID+")" + " DESC LIMIT 1";    // sort by date
//        String sortOrder = "datetime("+FDContract.FxEntry.COLUMN_FIXTURE_ID+")" + " DESC LIMIT 1"; // sort by date and time 1 record last
//        String sortOrder = "strftime(%s,"+FDContract.FxEntry.COLUMN_FIXTURE_ID+")" + " DESC";      // sort by date and time all records backward
//        String sortOrder = "datetime(" + FDContract.FxEntry.COLUMN_FIXTURE_DATE + ")" + " ASC"; // sort by date and time
        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_FIXTURES].getSortOrder();

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, FDFixture> map = readFixtures(cursor);
        cursor.close();     // disables notifications
        return map;
    }

    // fixture
    private static FDFixture readFixture(Cursor cursor) {
        int id = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_FIXTURE_ID);
        if (id <= 0) return null;

        int competitionId = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_COMPETITION_ID);
        int homeTeamId = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_TEAM_HOME_ID);
        int awayTeamId = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_TEAM_AWAY_ID);
        String date = cursor.getString(FDDbHelper.IFxEntry.COLUMN_FIXTURE_DATE);
        String status = cursor.getString(FDDbHelper.IFxEntry.COLUMN_FIXTURE_STATUS);
        int matchday = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_FIXTURE_MATCHDAY);
        String homeTeamName = cursor.getString(FDDbHelper.IFxEntry.COLUMN_FIXTURE_TEAM_HOME);
        String awayTeamName = cursor.getString(FDDbHelper.IFxEntry.COLUMN_FIXTURE_TEAM_AWAY);

        int goalsHomeTeam = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_FIXTURE_GOALS_HOME);
        int goalsAwayTeam = cursor.getInt(FDDbHelper.IFxEntry.COLUMN_FIXTURE_GOALS_AWAY);
        double homeWin = cursor.getDouble(FDDbHelper.IFxEntry.COLUMN_FIXTURE_ODDS_WIN);
        double draw = cursor.getDouble(FDDbHelper.IFxEntry.COLUMN_FIXTURE_ODDS_DRAW);
        double awayWin = cursor.getDouble(FDDbHelper.IFxEntry.COLUMN_FIXTURE_ODDS_AWAY);

        FDFixture fixture = new FDFixture(id, competitionId, homeTeamId, awayTeamId,
                date, status, matchday, homeTeamName, awayTeamName,
                goalsHomeTeam, goalsAwayTeam, homeWin, draw, awayWin);

        return fixture;
    }


    public static FDFixture readFixture(Context context, int id) {
        if (id <= 0) return null;
        Uri uri = FDContract.FxEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();                               // вся таблица

        String sortOrder = FDContract.MATCH_PARAMETERS[MATCH_PARAMETERS_FIXTURES].getSortOrder();

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                sortOrder
        );
        if (cursor == null || cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        FDFixture fixture = readFixture(cursor);
        cursor.close();     // disables notifications
        return fixture;
    }


    // read database
    public static void readDatabase(
            Context context, Map<Integer, FDCompetition> map,
            Map<Integer, List<Integer>> mapTeamKeys, Map<Integer, FDTeam> mapTeams,
            Map<Integer, List<Integer>> mapFixtureKeys, Map<Integer, FDFixture> mapFixtures) {


        Map<Integer, FDCompetition> readMap = readCompetitions(context);
        Map<Integer, List<Integer>> readMapTeamKeys = readCompetitionTeams(context);
        Map<Integer, FDTeam> readMapTeams = readTeams(context);
        Map<Integer, List<Integer>> readMapFixtureKeys = readCompetitionFixtures(context);
        Map<Integer, FDFixture> readMapFixtures = readFixtures(context);

        if (readMap != null && readMap.size() > 0) {
            map.clear();
            map.putAll(readMap);
        }

        if (readMapTeamKeys != null && readMapTeamKeys.size() > 0) {
            mapTeamKeys.clear();
            mapTeamKeys.putAll(readMapTeamKeys);
        }

        if (readMapTeams != null && readMapTeams.size() > 0) {
            mapTeams.clear();
            mapTeams.putAll(readMapTeams);
        }

        if (readMapFixtureKeys != null && readMapFixtureKeys.size() > 0) {
            mapFixtureKeys.clear();
            mapFixtureKeys.putAll(readMapFixtureKeys);
        }

        if (readMapFixtures != null && readMapFixtures.size() > 0) {
            mapFixtures.clear();
            mapFixtures.putAll(readMapFixtures);
        }
    }

    // read cursors
// read
// competitions
    public static Map<Integer, FDCompetition> readCompetitions(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;

        Map<Integer, FDCompetition> map = new HashMap<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_COMPETITION_ID);
            if (id <= 0) continue;

            String caption = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_CAPTION);
            String league = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_LEAGUE);
            String year = cursor.getString(FDDbHelper.ICpEntry.COLUMN_COMPETITION_YEAR);
            int currentMatchDay = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_CURRENT_MATCHDAY);
            int numberOfMatchDays = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_MATCHDAYS);
            int numberOfTeams = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_TEAMS);
            int numberOfGames = cursor.getInt(FDDbHelper.ICpEntry.COLUMN_NUMBER_GAMES);
            String lastUpdated = cursor.getString(FDDbHelper.ICpEntry.COLUMN_LAST_UPDATE);
// TODO Check Date Format
            FDCompetition competition = new FDCompetition(id, caption, league,
                    year, currentMatchDay, numberOfMatchDays, numberOfTeams,
                    numberOfGames, lastUpdated);

            map.put(competition.getId(), competition);
        }
//        cursor.close();  // notification support
        return map;
    }

    // competition_teams
    public static Map<Integer, List<Integer>> readCompetitionTeams(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, List<Integer>> map = new HashMap<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(FDDbHelper.ICpTmEntry.COLUMN_COMPETITION_ID);
            int id2 = cursor.getInt(FDDbHelper.ICpTmEntry.COLUMN_TEAM_ID);
            if (id <= 0 || id2 <= 0) continue;

            List<Integer> list = map.get(id);
            if (list == null) list = new ArrayList<>();
            list.add(id2);
            map.put(id, list);
        }
//        cursor.close(); // notification support
        return map;
    }

    // teams
    public static Map<Integer, FDTeam> readTeams(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, FDTeam> map = new HashMap<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(FDDbHelper.ITmEntry.COLUMN_TEAM_ID);
            if (id <= 0) continue;

            String name = cursor.getString(FDDbHelper.ITmEntry.COLUMN_TEAM_NAME);
            String code = cursor.getString(FDDbHelper.ITmEntry.COLUMN_TEAM_CODE);
            String shortName = cursor.getString(FDDbHelper.ITmEntry.COLUMN_TEAM_SHORT_NAME);
            String squadMarketValue = cursor.getString(FDDbHelper.ITmEntry.COLUMN_TEAM_MARKET_VALUE);
            String crestURL = cursor.getString(FDDbHelper.ITmEntry.COLUMN_TEAM_CREST_URI);


            FDTeam team = new FDTeam(id, name, code, shortName, squadMarketValue, crestURL);
            map.put(team.getId(), team);
        }
//        cursor.close();
        return map;
    }


    // fixtures
    // competition_fixture
    public static Map<Integer, List<Integer>> readCompetitionFixtures(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, List<Integer>> map = new HashMap<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(FDDbHelper.ICpFxEntry.COLUMN_COMPETITION_ID);
            int id2 = cursor.getInt(FDDbHelper.ICpFxEntry.COLUMN_FIXTURE_ID);
            if (id <= 0 || id2 <= 0) continue;

            List<Integer> list = map.get(id);
            if (list == null) list = new ArrayList<>();
            list.add(id2);
            map.put(id, list);
        }
//        cursor.close(); // notification support
        return map;
    }

    // fixture
    public static Map<Integer, FDFixture> readFixtures(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        Map<Integer, FDFixture> map = new LinkedHashMap<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            FDFixture fixture = readFixture(cursor);
            if (fixture == null) continue;
            map.put(fixture.getId(), fixture);
        }
        return map;
    }


    // write
    // competition
    public static ArrayList<ContentProviderOperation> writeCompetition(
            FDCompetition competition, boolean forceDelete) {

        if (competition == null || competition.getId() <= 0) return null;

        Uri uri = buildItemIdUri(FDContract.CpEntry.TABLE_NAME, competition.getId());


        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (forceDelete) {
            operations.add(ContentProviderOperation.newDelete(uri).build());  // delete one record from Competitions table
        }

        ContentValues values = new ContentValues();
// TODO Check Date
        String lastUpdated = formatDateToSQLite(competition.getLastUpdated());

        values.put(FDContract.CpEntry.COLUMN_COMPETITION_ID, competition.getId());                  // int
        values.put(FDContract.CpEntry.COLUMN_COMPETITION_CAPTION, competition.getCaption());        // string
        values.put(FDContract.CpEntry.COLUMN_COMPETITION_LEAGUE, competition.getLeague());          // string
        values.put(FDContract.CpEntry.COLUMN_COMPETITION_YEAR, competition.getYear());              // string
        values.put(FDContract.CpEntry.COLUMN_CURRENT_MATCHDAY, competition.getCurrentMatchDay());   // int
        values.put(FDContract.CpEntry.COLUMN_NUMBER_MATCHDAYS, competition.getNumberOfMatchDays()); // int
        values.put(FDContract.CpEntry.COLUMN_NUMBER_TEAMS, competition.getNumberOfTeams());         // int
        values.put(FDContract.CpEntry.COLUMN_NUMBER_GAMES, competition.getNumberOfGames());         // int
        values.put(FDContract.CpEntry.COLUMN_LAST_UPDATE, lastUpdated);                             // string SQLite format
        operations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());

        return operations;
    }

    // competition
    public static ContentProviderResult[] writeCompetition(Context context, FDCompetition
            competition, boolean forceDelete)
            throws OperationApplicationException, RemoteException {
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, writeCompetition(competition, forceDelete));
    }

    // competitions
    public static ContentProviderResult[] writeCompetitions(
            Context context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;
        Uri uri = FDContract.CpEntry.CONTENT_URI;
        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();
        if (forceDelete) {
            listOperations.add(ContentProviderOperation.newDelete(uri).build());    // delete all records from Competitions table
        }

        for (FDCompetition competition : map.values()) {
            List<ContentProviderOperation> operations = writeCompetition(competition, false);
            if (operations == null) continue;
            listOperations.addAll(operations);
        }

        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }

    // competition_team
    public static ArrayList<ContentProviderOperation> writeCompetitionTeams(FDCompetition competition) {

        int refreshTime = (int) (TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTime().getTime()));
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        // must be deleted because of ids are not keys
        Uri uri = buildItemIdUri(FDContract.CpTmEntry.TABLE_NAME, competition.getId());
        operations.add(ContentProviderOperation.newDelete(uri).build());  // delete all records for Competition from table


        for (FDTeam team : competition.getTeams()) {
            if (team == null || team.getId() <= 0) continue;
            uri = buildItemIdUri(FDContract.CpTmEntry.TABLE_NAME, competition.getId(), team.getId());

            ContentValues values = new ContentValues();
            values.put(FDContract.CpTmEntry.COLUMN_COMPETITION_ID, competition.getId());            // int
            values.put(FDContract.CpTmEntry.COLUMN_TEAM_ID, team.getId());                          // int
            operations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        }
        return operations;
    }

    public static ContentProviderResult[] writeCompetitionTeams(Context context, FDCompetition
            competition,
                                                                boolean forceDelete)
            throws OperationApplicationException, RemoteException {
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, writeCompetitionTeams(competition));
    }

    public static ContentProviderResult[] writeCompetitionTeams(
            Context context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;

        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();

        if (forceDelete) {
            Uri uri = FDContract.CpTmEntry.CONTENT_URI;
            listOperations.add(ContentProviderOperation.newDelete(uri).build());
        }

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0 ||
                    competition.getTeams() == null) continue;
            List<ContentProviderOperation> operations = writeCompetitionTeams(competition);
            if (operations == null) continue;
            listOperations.addAll(operations);

        }
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }

    // teams
    // team
    public static ArrayList<ContentProviderOperation> writeTeam(FDTeam team, boolean forceDelete) {

        if (team == null || team.getId() <= 0) return null;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        Uri uri = buildItemIdUri(FDContract.TmEntry.TABLE_NAME, team.getId());
        int refreshTime = (int) (TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTime().getTime()));

        if (forceDelete) { // force clear Teams table
            operations.add(ContentProviderOperation.newDelete(uri).build());
        }

        ContentValues values = new ContentValues();
        values.put(FDContract.TmEntry.COLUMN_TEAM_ID, team.getId());                                // int
        values.put(FDContract.TmEntry.COLUMN_TEAM_NAME, team.getName());                            // string
        values.put(FDContract.TmEntry.COLUMN_TEAM_CODE, team.getCode());                            // string
        values.put(FDContract.TmEntry.COLUMN_TEAM_SHORT_NAME, team.getShortName());                 // string
        values.put(FDContract.TmEntry.COLUMN_TEAM_MARKET_VALUE, team.getSquadMarketValue());        // string
        values.put(FDContract.TmEntry.COLUMN_TEAM_CREST_URI, team.getCrestURL());                   // string

        operations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        return operations;
    }

    // team
    public static ContentProviderResult[] writeTeam(Context context, FDTeam team,
                                                    boolean forceDelete)
            throws OperationApplicationException, RemoteException {
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, writeTeam(team, forceDelete));
    }

    // teams
    public static ContentProviderResult[] writeTeams(Context
                                                             context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;


        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();

        if (forceDelete) {                                          // force clear Teams table
            Uri uri = FDContract.TmEntry.CONTENT_URI;
            listOperations.add(ContentProviderOperation.newDelete(uri).build());
        }

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0) continue;
            List<FDTeam> teams = competition.getTeams();
            if (teams == null || teams.size() == 0) continue;
            for (FDTeam team : teams) {
                List<ContentProviderOperation> operations = writeTeam(team, false);
                if (operations == null) continue;
                listOperations.addAll(operations);
            }
        }
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }

    // competition fixture
    public static ArrayList<ContentProviderOperation> writeCompetitionFixtures(
            FDCompetition competition) {


        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        // all fixtures of given competition  must be deleted because ids are not key
        Uri uri = buildItemIdUri(FDContract.CpFxEntry.TABLE_NAME, competition.getId());
        operations.add(ContentProviderOperation.newDelete(uri).build());  // delete all records for Competition from table


        for (FDFixture fixture : competition.getFixtures()) {
            if (fixture == null || fixture.getId() <= 0) continue;
            uri = buildItemIdUri(FDContract.CpFxEntry.TABLE_NAME, competition.getId(), fixture.getId());

            ContentValues values = new ContentValues();
            values.put(FDContract.CpFxEntry.COLUMN_COMPETITION_ID, competition.getId());            // int
            values.put(FDContract.CpFxEntry.COLUMN_FIXTURE_ID, fixture.getId());                    // int
            operations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        }
        return operations;
    }

    public static ContentProviderResult[] writeCompetitionFixtures(
            Context context, FDCompetition competition, boolean forceDelete)
            throws OperationApplicationException, RemoteException {
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, writeCompetitionTeams(competition));
    }

    public static ContentProviderResult[] writeCompetitionFixtures(
            Context context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;

        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();

        if (forceDelete) {
            Uri uri = FDContract.CpFxEntry.CONTENT_URI;
            listOperations.add(ContentProviderOperation.newDelete(uri).build());
        }

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0 || competition.getFixtures() == null)
                continue;
            List<ContentProviderOperation> operations = writeCompetitionFixtures(competition);
            if (operations == null) continue;
            listOperations.addAll(operations);

        }
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }

    // fixture
    public static ArrayList<ContentProviderOperation> writeFixture(FDFixture fixture, boolean forceDelete) {

        if (fixture == null || fixture.getId() <= 0) return null;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        Uri uri = buildItemIdUri(FDContract.FxEntry.TABLE_NAME, fixture.getId());
        if (forceDelete) { // force clear Teams table
            operations.add(ContentProviderOperation.newDelete(uri).build());
        }
        ContentValues values = new ContentValues();
// TODO Check SQLite Date Format
        String fixtureDate = formatDateToSQLite(fixture.getDate());

        values.put(FDContract.FxEntry.COLUMN_FIXTURE_ID, fixture.getId());                          // int
        values.put(FDContract.FxEntry.COLUMN_COMPETITION_ID, fixture.getCompetitionId());           // int
        values.put(FDContract.FxEntry.COLUMN_TEAM_HOME_ID, fixture.getHomeTeamId());                // int
        values.put(FDContract.FxEntry.COLUMN_TEAM_AWAY_ID, fixture.getAwayTeamId());                // int
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_DATE, fixtureDate);                            // string date SQLite format
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_STATUS, fixture.getStatus());                  // string
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_MATCHDAY, fixture.getMatchDay());              // string
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_TEAM_HOME, fixture.getHomeTeamName());         // string
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_TEAM_AWAY, fixture.getAwayTeamName());         // string
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_GOALS_HOME, fixture.getGoalsHome());           // int
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_GOALS_AWAY, fixture.getGoalsAway());           // int
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_ODDS_WIN, fixture.getHomeWin());               // double
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_ODDS_DRAW, fixture.getDraw());                 // double
        values.put(FDContract.FxEntry.COLUMN_FIXTURE_ODDS_AWAY, fixture.getAwayWin());              // double
        operations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        return operations;
    }

    // fixture
    public static ContentProviderResult[] writeFixture(Context context, FDFixture fixture,
                                                       boolean forceDelete)
            throws OperationApplicationException, RemoteException {
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, writeFixture(fixture, forceDelete));
    }

    // teams
    public static ContentProviderResult[] writeFixtures(
            Context context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;

        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();

        if (forceDelete) {                                          // force clear Teams table
            Uri uri = FDContract.FxEntry.CONTENT_URI;
            listOperations.add(ContentProviderOperation.newDelete(uri).build());
        }

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0) continue;
            List<FDFixture> fixtures = competition.getFixtures();
            if (fixtures == null || fixtures.size() == 0) continue;
            for (FDFixture fixture : fixtures) {
                List<ContentProviderOperation> operations = writeFixture(fixture, false);
                if (operations == null) continue;
                listOperations.addAll(operations);
            }
        }
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }

    // write database
    public static void writeDatabase(Context context, Map<Integer, FDCompetition> map, boolean forceDelete)
            throws OperationApplicationException, RemoteException {

        writeCompetitions(context, map, forceDelete);
        writeCompetitionTeams(context, map, forceDelete);
        writeTeams(context, map, forceDelete);
        writeCompetitionFixtures(context, map, forceDelete);
        writeFixtures(context, map, forceDelete);
    }

    // write long term data
    // table
    private static ContentProviderOperation writeTableRecord(
            FDStanding standing, int id, int matchDay, String caption, String group, Uri uri)
            throws OperationApplicationException, RemoteException {

// id = competitionId * 10000 + teamId
        ContentValues values = new ContentValues();
        values.put(FDContract.TbEntry.COLUMN_COMPETITION_ID, id);                                   // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_ID, standing.getId());                            // int
        values.put(FDContract.TbEntry.COLUMN_GROUP_NAME, group);                                     // string
        values.put(FDContract.TbEntry.COLUMN_COMPETITION_MATCHDAY, matchDay);                       // int
        values.put(FDContract.TbEntry.COLUMN_LEAGUE_CAPTION, caption);                              // string
        values.put(FDContract.TbEntry.COLUMN_TEAM_POSITION, standing.getPosition());                // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_NAME, standing.getTeamName());                    // string
        values.put(FDContract.TbEntry.COLUMN_CREST_URI, standing.getCrestURI());                    // string
        values.put(FDContract.TbEntry.COLUMN_TEAM_POINTS, standing.getPoints());                    // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_GOALS, standing.getGoals());                      // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_GOALS_AGAINST, standing.getGoalsAgainst());       // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_GOALS_DIFFERENCE, standing.getGoalDifference());  // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_WINS, standing.getWins());                        // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_DRAWS, standing.getDraws());                      // int
        values.put(FDContract.TbEntry.COLUMN_TEAM_LOSSES, standing.getLosses());                    // int

        return ContentProviderOperation.newInsert(uri).withValues(values).build();
    }

    public static List<ContentProviderOperation> writeTableRecords(
            Context context, List<FDStanding> standings, FDTable table, String group, Uri uri)
            throws OperationApplicationException, RemoteException {

        List<ContentProviderOperation> operations = new ArrayList<>();
        int id = table.getId();
        int matchDay = table.getMatchDay();
        String caption = table.getLeagueCaption();

        if (standings == null) return operations;

        for (FDStanding standing : standings) {
            try {
                standing.setId();
                ContentProviderOperation op = writeTableRecord(standing, id, matchDay, caption, group, uri);
                operations.add(op);

            } catch (NullPointerException | NumberFormatException e) {
                Timber.d(formatWrite(context, EXCEPTION_CODE_7, id, e.getMessage()));
            }
        }

        return operations;
    }

    public static ArrayList<ContentProviderOperation> writeTable(Context context, FDTable table)
            throws OperationApplicationException, RemoteException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if (table == null || table.getId() <= 0) return operations;
        Uri uri = buildItemIdUri(FDContract.TbEntry.TABLE_NAME, table.getId());

//          // force clear table for competition
//            Uri uri = buildItemIdUri(FDContract.TbEntry.TABLE_NAME, table.getId());
//            operations.add(ContentProviderOperation.newDelete(uri).build());

        List<FDStanding> standings = table.getStanding();
        FDStandingGroup standingGroup = table.getStandings();

        if (standings != null) {
            operations.addAll(writeTableRecords(context, standings, table, EMPTY_STRING, uri));
        }

        if (standingGroup != null) {
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_A, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_B, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_C, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_D, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_E, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_F, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_G, uri));
            operations.addAll(writeTableRecords(context, standingGroup.getGroupA(), table, TABLE_GROUP_H, uri));
        }

        return operations;
    }

    // tables
    public static ContentProviderResult[] writeTables(Context context, Map<Integer, FDCompetition> map)
            throws OperationApplicationException, RemoteException {

        if (map == null || map.size() == 0) return null;
        ArrayList<ContentProviderOperation> listOperations = new ArrayList<>();

        Uri uri = FDContract.TbEntry.CONTENT_URI;
        listOperations.add(ContentProviderOperation.newDelete(uri).build());  // clear because _ID autoincrement

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0) continue;
            FDTable table = competition.getTable();

            List<ContentProviderOperation> operations = writeTable(context, table);
            if (operations == null) continue;
            listOperations.addAll(operations);

        }
        return context.getContentResolver().applyBatch(FDContract.CONTENT_AUTHORITY, listOperations);
    }


// connection

    /**
     * Returns status of connection to network
     *
     * @param context Context of calling activity
     * @return boolean status of connection, true if connected, false if not
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // network
    private static IFDRetrofitAPI setupRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-Auth-Token", FD_API_KEY)//BuildConfig.FD_API_KEY)
                                .build();
                        return chain.proceed(request);
                    }
                }).build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new PostProcessingEnabler())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(FD_BASE_URI)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(IFDRetrofitAPI.class);
    }

    private static List<FDCompetition> loadListCompetitions()
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getCompetitions().execute().body();
    }

    // competitions
    private List<FDCompetition> loadListCompetitions(String season)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getCompetitions(season).execute().body();
    }

    // teams
    private static FDTeams loadListTeams(String competition)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getTeams(competition).execute().body();
    }

    // fixtures
    private static FDFixtures loadListFixtures(String competition)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getFixtures(competition).execute().body();
    }

    // table
    private static FDTable loadTable(String competition)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getTable(competition).execute().body();
    }

    // players
    private static FDPlayers loadListPlayers(String team)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getTeamPlayers(team).execute().body();
    }

    private static FDFixtures loadListTeamFixtures(String team)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getTeamFixtures(team).execute().body();
    }

    private static FDPlayers loadListTeamPlayers(String team)
            throws NullPointerException, IOException {

        IFDRetrofitAPI retrofitAPI = setupRetrofit();
        return retrofitAPI.getTeamPlayers(team).execute().body();
    }


    // load
    // competitions
    public static Map<Integer, FDCompetition> loadCompetitions(Context context, Map<Integer, FDCompetition> map)
            throws NullPointerException, IOException {

        map = new HashMap<>();
        List<FDCompetition> list = loadListCompetitions();          // NullPointerException, IOException
        for (FDCompetition competition : list) {
            try {
                map.put(competition.getId(), competition);          // NullPointerException, NumberFromatException
                List<FDTeam> teams = loadListTeams(competition);    // NullPointerException, NumberFromatException, IOException
                competition.setTeams(teams);

                List<FDFixture> fixtures = loadListFixtures(competition);  // load
                competition.setFixtures(fixtures);

            } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
                Timber.d(formatLoad(context, EXCEPTION_CODE_1, competition, e.getMessage()));
            }
        }
        return map;
    }

//    private static boolean isCompetitionsRefreshed(Context context, Map<Integer, FDCompetition> map) {
//        if (map == null || map.isEmpty()) return false;
//        for (FDCompetition competition : map.values()) {
//            if (competition == null || competition.getId() <= 0 ||
//                    !isRefreshed(context, competition.getLastRefresh())) return false;
//        }
//        return true; // all data ok and refreshed
//    }

    // all maps read by loaders
// load competitions
//    public static boolean loadDatabaseRefresh(
//            Context context, Map<Integer, FDCompetition> map,
//            Map<Integer, List<Integer>> mapTeamKeys, Map<Integer, FDTeam> mapTeams,
//            Map<Integer, List<Integer>> mapFixtureKeys, Map<Integer, FDFixture> mapFixtures,
//            boolean forceUpdate) throws NullPointerException, IOException {
//
//// progress
//        double step;
//        double progress = 0;
//
//// load teams or skip
//        boolean isUpdated = false;
//        boolean isCompetitionUpdated = false;
//// load map
//        if (forceUpdate || !isCompetitionsRefreshed(context, map)) {
//            isUpdated = true;
//            isCompetitionUpdated = true;
//            map.clear();
//            List<FDCompetition> list = loadListCompetitions();  // NullPointerException, IOException
//            Date lastRefresh = currentTimeMinutesToDate();
//            for (FDCompetition competition : list) {
//                if (competition == null || competition.getId() <= 0) continue;
//                competition.setLastRefresh(lastRefresh);
//                map.put(competition.getId(), competition);
//            }
//        }
//
//        step = UPDATE_SERVICE_PROGRESS * 0.8 / (map.size() + 1); // 1 + t.map.size + f.map.size
//        progress = step;
//        sendProgress(context, (int) progress);  // +1
//
//        for (FDCompetition competition : map.values()) {
//            if (competition == null || competition.getId() <= 0) continue;
//// teams
//            boolean isRefreshed = false;
//            if (!forceUpdate && !isCompetitionUpdated &&                                // if competitions updated load teams
//                    isRefreshed(context, competition.getLastRefresh())) {               // check smart update
//                isRefreshed = true;
//                if (competition.getTeams() == null) {
//                    isRefreshed = setListTeams(competition, mapTeamKeys, mapTeams);     // restore teams from keys
//                }
//            }
//
//            if (!isRefreshed) {
//                try {
//                    isUpdated = true;                                                   // one item changed
//                    List<FDTeam> teams = loadListTeams(competition);                    // load
//                    competition.setTeams(teams);
//                } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
//                    Timber.d(format(context, EXCEPTION_CODE_2, competition, e.getMessage()));
//                }
//            }
//// fixtures
//            isRefreshed = false;
//            if (!forceUpdate && !isCompetitionUpdated &&                                // if competitions updated load fixtures
//                    isRefreshed(context, competition.getLastRefresh())) {               // check smart update
//                isRefreshed = true;
//                if (competition.getFixtures() == null) {
//                    isRefreshed = setListFixtures(competition, mapFixtureKeys, mapFixtures);
//                }
//            }
//
//            if (!isRefreshed) {
//                try {
//                    isUpdated = true;                                                   // one item changed
//                    List<FDFixture> fixtures = loadListFixtures(competition);           // load
//                    competition.setFixtures(fixtures);
//                } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
//                    Timber.d(format(context, EXCEPTION_CODE_3, competition, e.getMessage()));
//                }
//
//            }
//// progress
//            progress += step;
//            sendProgress(context, (int) progress);// t,f
//        }
//        return isUpdated;
//    }


    // all maps read by loaders
// load competitions
    public static boolean loadDatabase(
            Context context, Map<Integer, FDCompetition> map,
            Map<Integer, List<Integer>> mapTeamKeys, Map<Integer, FDTeam> mapTeams,
            Map<Integer, List<Integer>> mapFixtureKeys, Map<Integer, FDFixture> mapFixtures
    ) throws NullPointerException, IOException {

// progress
        double step;
        double progress = 0;

// load map
        map.clear();
        List<FDCompetition> list = loadListCompetitions();  // NullPointerException, IOException

        for (FDCompetition competition : list) {
            if (competition == null || competition.getId() <= 0) continue;
            map.put(competition.getId(), competition);
        }

        step = UPDATE_SERVICE_PROGRESS * 0.8 / (map.size() + 1); // 1 + t.map.size + f.map.size
        progress = step;
        sendProgress(context, (int) progress);  // +1

        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0) continue;
// teams
            try {
                List<FDTeam> teams = loadListTeams(competition);                    // load
                competition.setTeams(teams);
            } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
                Timber.d(formatLoad(context, EXCEPTION_CODE_4, competition, e.getMessage()));
            }
// fixtures
            try {
                List<FDFixture> fixtures = loadListFixtures(competition);           // load
                competition.setFixtures(fixtures);

            } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
                Timber.d(formatLoad(context, EXCEPTION_CODE_5, competition, e.getMessage()));
            }
// progress
            progress += step;
            sendProgress(context, (int) progress);// t,f
        }
        return true;
    }

    // teams
    // list from competition
    private static List<FDTeam> loadListTeams(FDCompetition competition)
            throws NumberFormatException, NullPointerException, IOException, InterruptedException {
        if (competition == null || competition.getId() <= 0) return null;

        String id = formatString(competition.getId());

        FDTeams teams = loadListTeams(id);                   // NullPointerException
//        if (teams == null) {
// test!!!
//            Thread.sleep(100);
//            teams = loadListTeams(id); // second trial
//        }
        List<FDTeam> list = new ArrayList<>();
        for (FDTeam team : teams.getTeams()) {
            if (team == null) continue;
            list.add(team);
        }
        return list;
    }

    // list from competition
    private static List<FDTeam> getListTeams(
            Context context, FDCompetition competition, boolean forceUpdate)
            throws NumberFormatException, NullPointerException, IOException {
        if (competition == null || competition.getId() <= 0) return null;
// smart update check
        List<FDTeam> list = competition.getTeams();
        if (!forceUpdate && list != null) {  // check smart update
            return list;
        }
// no teams
        String id = formatString(competition.getId());
        FDTeams teams = loadListTeams(id);      // NullPointerException
        list = new ArrayList<>();
        for (FDTeam team : teams.getTeams()) {
            try {
                team.setId();
            } catch (NullPointerException | NumberFormatException e) {
                continue;
            }
            list.add(team);
        }
        return list;
    }

    // map from competition
    private static Map<Integer, FDTeam> getTeams(
            Context context, FDCompetition competition, boolean forceUpdate)
            throws NumberFormatException, NullPointerException, IOException {
        Map<Integer, FDTeam> map = new HashMap<>();
        List<FDTeam> teams = getListTeams(context, competition, forceUpdate);
        competition.setTeams(teams);                        // NullPointerException
        for (FDTeam team : teams) {
            map.put(team.getId(), team);
        }
        return map;
    }

    // map from competitions
    public static Map<Integer, FDTeam> getTeams(
            Context context, Map<Integer, FDCompetition> competitions, boolean forceUpdate) {
        if (competitions == null) return null;

        Map<Integer, FDTeam> map = new HashMap<>();
        for (FDCompetition competition : competitions.values()) {
            try {
                map.putAll(getTeams(context, competition, forceUpdate));
            } catch (NumberFormatException | NullPointerException | IOException e) {
                Timber.d(context.getString(R.string.get_competitions_teams_null) + e.getMessage());
            }
        }
        return map;
    }


    // list from competition
    private static List<FDFixture> loadListFixtures(FDCompetition competition)
            throws NumberFormatException, NullPointerException, IOException, InterruptedException {
        if (competition == null || competition.getId() <= 0) return null;

        int competitionId = competition.getId();
        String id = formatString(competitionId);
        String caption = competition.getCaption();

        FDFixtures fixtures = loadListFixtures(id);      // NullPointerException

// TODO check for repetitions of load
        if (fixtures == null) {
// test!!!
//            Thread.sleep(100);
//            fixtures = loadListFixtures(id); // second trial
        }
        List<FDFixture> list = new ArrayList<>();
        for (FDFixture fixture : fixtures.getFixtures()) {
            try {
                fixture.setId();
            } catch (NullPointerException | NumberFormatException e) {
                continue;
            }
            list.add(fixture);
        }
        return list;
    }

    // load competitions
    public static boolean loadCompetitions(
            Map<Integer, FDCompetition> map,
            Map<Integer, List<Integer>> mapTeamKeys, Map<Integer, FDTeam> mapTeams,
            Map<Integer, List<Integer>> mapFixtureKeys, Map<Integer, FDFixture> mapFixtures) {

        if (map == null || map.size() == 0 ||
                mapTeamKeys == null || mapTeamKeys.isEmpty() ||
                mapTeams == null || mapTeams.isEmpty() ||
                mapFixtureKeys == null || mapFixtureKeys.isEmpty() ||
                mapFixtures == null || mapFixtures.isEmpty()) return false;

// load map
        for (FDCompetition competition : map.values()) {
            if (competition == null || competition.getId() <= 0) continue;
// teams
            if (competition.getTeams() == null) {
                setListTeams(competition, mapTeamKeys, mapTeams);    // restore teams from keys
            }
// fixtures
            if (competition.getFixtures() == null) {
                setListFixtures(competition, mapFixtureKeys, mapFixtures);
            }
        }
        return true;
    }


    // load fixtures from team
    private static List<FDFixture> loadListTeamFixtures(int teamId)
            throws NumberFormatException, NullPointerException, IOException, InterruptedException {
        if (teamId <= 0) return null;

        String id = formatString(teamId);
        long lastRefresh = Calendar.getInstance().getTimeInMillis();
        FDFixtures fixtures = loadListTeamFixtures(id);      // NullPointerException
        List<FDFixture> list = new ArrayList<>();
        for (FDFixture fixture : fixtures.getFixtures()) {
            try {
                fixture.setId();
            } catch (NullPointerException | NumberFormatException e) {
                continue;
            }
            list.add(fixture);
        }
        return list;
    }

    public static List<FDFixture> loadListTeamFixtures(Context context, int id) {
        try {
            return loadListTeamFixtures(id);
        } catch (NumberFormatException | NullPointerException | IOException | InterruptedException e) {
            Timber.d(context.getString(R.string.retrofit_response_empty), e.getMessage());
            return null;
        }
    }

    // load players from team
    private static List<FDPlayer> loadListTeamPlayers(int teamId)
            throws NumberFormatException, NullPointerException, IOException, InterruptedException {
        if (teamId <= 0) return null;

        String id = formatString(teamId);

        FDPlayers players = loadListTeamPlayers(id);      // NullPointerException
        List<FDPlayer> list = new ArrayList<>();
        for (FDPlayer player : players.getPlayers()) {
            if (player == null) continue;
            list.add(player);
        }
        return list;
    }

    public static List<FDPlayer> loadListTeamPlayers(Context context, int teamId) {
        try {
            return loadListTeamPlayers(teamId);
        } catch (NumberFormatException | NullPointerException | IOException | InterruptedException e) {
            Timber.d(context.getString(R.string.retrofit_response_empty), e.getMessage());
            return null;
        }
    }


    public static void sendProgress(Context context, int value) {
        if (value < 0) return;
        if (value > UPDATE_SERVICE_PROGRESS) value = UPDATE_SERVICE_PROGRESS;

        context.sendBroadcast(new Intent(context.getString(R.string.broadcast_data_update_progress))
                .putExtra(context.getString(R.string.extra_progress_counter), value));

    }

    // load long term
// load tables
    public static boolean loadTablesLongTerm(Context context, Map<Integer, FDCompetition> map,
                                             double progress, double step)
            throws NullPointerException, IOException {
        if (map == null || map.isEmpty()) return false;


        for (FDCompetition competition : map.values()) {

            if (competition == null || competition.getId() <= 0) continue;
            int competitionId = competition.getId();
            String id = formatString(competitionId);
// tables
            try {
                FDTable table = loadTable(id);
                table.setId();
                competition.setTable(table);
                Thread.sleep(LOAD_DB_DELAY);

            } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
                Timber.d(formatLoad(context, EXCEPTION_CODE_6, competition, e.getMessage()));
            }

// progress
            progress += step;
            sendProgress(context, (int) progress);// t,f
        }
        return true;
    }

    // load players
    public static boolean loadPlayersLongTerm(Context context, Map<Integer, FDTeam> map,
                                              double progress, double step)
            throws NullPointerException, IOException {
        if (map == null || map.isEmpty()) return false;


        for (FDTeam team : map.values()) {
            if (team == null || team.getId() <= 0) continue;
            int teamId = team.getId();
            String id = formatString(teamId);
// tables
            try {
                FDPlayers players = loadListPlayers(id);
                team.setPlayers(players.getPlayers());
                Thread.sleep(LOAD_DB_DELAY);

            } catch (NullPointerException | NumberFormatException | IOException | InterruptedException e) {
                Timber.d(formatLoad(context, EXCEPTION_CODE_6, team, e.getMessage()));
            }
// progress
            progress += step;
            sendProgress(context, (int) progress);// t,f
        }
        return true;
    }


}
