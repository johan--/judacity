package ru.vpcb.contentprovider.dbase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ru.vpcb.contentprovider.dbase.FDContract.DATABASE_NAME;
import static ru.vpcb.contentprovider.dbase.FDContract.DATABASE_VERSION;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 23-Oct-17
 * Email: vadim.v.voronov@gmail.com
 */

/**
 * RecipeDbHelper class for RecipeItem Database Content Provider
 */
public class FDDbHelper extends SQLiteOpenHelper {

    FDDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates Database of RecipeItems
     *
     * @param db SQLiteDatabse database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_COMPETITIONS = "CREATE TABLE " + FDContract.CpEntry.TABLE_NAME + " (" +
//                FDContract.CpEntry._ID + " INTEGER PRIMARY KEY, " +
                FDContract.CpEntry.COLUMN_COMPETITION_ID + " INTEGER PRIMARY KEY, " +       // int
                FDContract.CpEntry.COLUMN_COMPETITION_CAPTION + " TEXT NOT NULL, " +                    // string
                FDContract.CpEntry.COLUMN_COMPETITION_LEAGUE + " TEXT NOT NULL, " +         // string
                FDContract.CpEntry.COLUMN_COMPETITION_YEAR + " TEXT NOT NULL, " +           // string
                FDContract.CpEntry.COLUMN_CURRENT_MATCHDAY + " INTEGER NOT NULL, " +        // int
                FDContract.CpEntry.COLUMN_NUMBER_MATCHDAYS + " INTEGER NOT NULL, " +        // int
                FDContract.CpEntry.COLUMN_NUMBER_TEAMS + " INTEGER NOT NULL, " +            // int
                FDContract.CpEntry.COLUMN_NUMBER_GAMES + " INTEGER NOT NULL, " +            // int
                FDContract.CpEntry.COLUMN_LAST_UPDATE + " INTEGER NOT NULL, " +             // int from date
                FDContract.CpEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";             // int from date

        final String CREATE_TABLE_COMPETITION_TEAMS = "CREATE TABLE " + FDContract.CpTmEntry.TABLE_NAME + " (" +
                FDContract.CpTmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +                 // int
                FDContract.CpTmEntry.COLUMN_COMPETITION_ID + " INTEGER NOT NULL, " +                // int
                FDContract.CpTmEntry.COLUMN_TEAM_ID + " INTEGER NOT NULL, " +                       // int
                FDContract.CpTmEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";                   // int from date

        final String CREATE_TABLE_COMPETITION_FIXTURES = "CREATE TABLE " + FDContract.CpFxEntry.TABLE_NAME + " (" +
                FDContract.CpFxEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FDContract.CpFxEntry.COLUMN_COMPETITION_ID + " INTEGER NOT NULL, " + // int
                FDContract.CpFxEntry.COLUMN_FIXTURE_ID + " INTEGER NOT NULL, " +        // int
                FDContract.CpFxEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";       // int from date


        final String CREATE_TABLE_TEAMS = "CREATE TABLE " + FDContract.TmEntry.TABLE_NAME + " (" +
//                FDContract.TmEntry._ID + " INTEGER PRIMARY KEY, " +
                FDContract.TmEntry.COLUMN_TEAM_ID + " INTEGER PRIMARY KEY, " +          // int
                FDContract.TmEntry.COLUMN_TEAM_NAME + " TEXT NOT NULL, " +              // string
                FDContract.TmEntry.COLUMN_TEAM_CODE + " TEXT, " +                       // string
                FDContract.TmEntry.COLUMN_TEAM_SHORT_NAME + " TEXT, " +                 // string
                FDContract.TmEntry.COLUMN_TEAM_MARKET_VALUE + " TEXT, " +               // string
                FDContract.TmEntry.COLUMN_TEAM_CREST_URI + " TEXT, " +                  // string
                FDContract.TmEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";         // int from date

        final String CREATE_TABLE_FIXTURES = "CREATE TABLE " + FDContract.FxEntry.TABLE_NAME + " (" +
//                FDContract.FxEntry._ID + " INTEGER PRIMARY KEY, " +
                FDContract.FxEntry.COLUMN_FIXTURE_ID + " INTEGER PRIMARY KEY, " +       // int
                FDContract.FxEntry.COLUMN_FIXTURE_DATE + " INTEGER NOT NULL, " +   // int from date
                FDContract.FxEntry.COLUMN_FIXTURE_STATUS + " TEXT NOT NULL, " +         // string
                FDContract.FxEntry.COLUMN_FIXTURE_MATCHDAY + " INTEGER NOT NULL, " +    // int
                FDContract.FxEntry.COLUMN_FIXTURE_TEAM_HOME + " TEXT NOT NULL, " +      // string
                FDContract.FxEntry.COLUMN_FIXTURE_TEAM_AWAY + " TEXT NOT NULL, " +      // string
                FDContract.FxEntry.COLUMN_FIXTURE_GOALS_HOME + " INTEGER NOT NULL, " +  // int
                FDContract.FxEntry.COLUMN_FIXTURE_GOALS_AWAY + " INTEGER NOT NULL, " +  // int
                FDContract.FxEntry.COLUMN_FIXTURE_ODDS_WIN + " REAL, " +                // real
                FDContract.FxEntry.COLUMN_FIXTURE_ODDS_DRAW + " REAL, " +               // real
                FDContract.FxEntry.COLUMN_FIXTURE_ODDS_AWAY + " REAL, " +               // real
                FDContract.FxEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";         // int from date


        final String CREATE_TABLE_TABLES = "CREATE TABLE " + FDContract.TbEntry.TABLE_NAME + " (" +
//                FDContract.TbEntry._ID + " INTEGER PRIMARY KEY, " +
                FDContract.TbEntry.COLUMN_COMPETITION_ID + " INTEGER PRIMARY KEY, " +   // int
                FDContract.TbEntry.COLUMN_TEAM_ID + " INTEGER NOT NULL, " +             // int
                FDContract.TbEntry.COLUMN_COMPETITION_MATCHDAY + " INTEGER NOT NULL, " +    // int
                FDContract.TbEntry.COLUMN_LEAGUE_CAPTION + " TEXT NOT NULL, " +         // string
                FDContract.TbEntry.COLUMN_TEAM_POSITION + " INTEGER NOT NULL, " +       // int
                FDContract.TbEntry.COLUMN_TEAM_NAME + " TEXT NOT NULL, " +              // string
                FDContract.TbEntry.COLUMN_CREST_URI + " TEXT, " +              // string
                FDContract.TbEntry.COLUMN_TEAM_PLAYED_GAMES + " INTEGER NOT NULL, " +   // int
                FDContract.TbEntry.COLUMN_TEAM_POINTS + " INTEGER NOT NULL, " +         // int
                FDContract.TbEntry.COLUMN_TEAM_GOALS + " INTEGER NOT NULL, " +          // int
                FDContract.TbEntry.COLUMN_TEAM_GOALS_AGAINST + " INTEGER NOT NULL, " +  // int
                FDContract.TbEntry.COLUMN_TEAM_GOALS_DIFFERENCE + " INTEGER NOT NULL, " +   // int
                FDContract.TbEntry.COLUMN_TEAM_WINS + " INTEGER NOT NULL, " +           // int
                FDContract.TbEntry.COLUMN_TEAM_DRAWS + " INTEGER NOT NULL, " +          // int
                FDContract.TbEntry.COLUMN_TEAM_LOSSES + " INTEGER NOT NULL, " +         // int
                FDContract.TbEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";         // int from date

        final String CREATE_TABLE_PLAYERS = "CREATE TABLE " + FDContract.PlEntry.TABLE_NAME + " (" +
//                FDContract.PlEntry._ID + " INTEGER PRIMARY KEY, " +
                FDContract.PlEntry.COLUMN_PLAYER_ID + " INTEGER PRIMARY KEY, " +        // int
                FDContract.PlEntry.COLUMN_TEAM_ID + " INTEGER NOT NULL, " +             // int
                FDContract.PlEntry.COLUMN_PLAYER_NAME + " TEXT NOT NULL, " +            // string
                FDContract.PlEntry.COLUMN_PLAYER_POSITION + " TEXT, " +                 // string
                FDContract.PlEntry.COLUMN_PLAYER_JERSEY_NUMBER + " INTEGER, " +         // int
                FDContract.PlEntry.COLUMN_PLAYER_DATE_BIRTH + " TEXT, " +               // int from date
                FDContract.PlEntry.COLUMN_PLAYER_NATIONALITY + " TEXT, " +              // string
                FDContract.PlEntry.COLUMN_PLAYER_DATE_CONTRACT + " TEXT, " +            // int from date
                FDContract.PlEntry.COLUMN_PLAYER_MARKET_VALUE + " TEXT, " +             // string
                FDContract.PlEntry.COLUMN_LAST_REFRESH + " INTEGER NOT NULL);";         // int from date

        db.execSQL(CREATE_TABLE_COMPETITIONS);
        db.execSQL(CREATE_TABLE_COMPETITION_TEAMS);
        db.execSQL(CREATE_TABLE_COMPETITION_FIXTURES);
        db.execSQL(CREATE_TABLE_TEAMS);
        db.execSQL(CREATE_TABLE_FIXTURES);
        db.execSQL(CREATE_TABLE_TABLES);
        db.execSQL(CREATE_TABLE_PLAYERS);

    }

    /**
     * Upgrades old version database to new version
     *
     * @param db         SQLiteDatabase database for upgrading
     * @param oldVersion int old version to compare
     * @param newVersion int new version to compare
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.CpEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.CpTmEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.CpFxEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.TmEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.FxEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.TbEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FDContract.PlEntry.TABLE_NAME);
        onCreate(db);
    }

    public interface ICpEntry {
        int COLUMN_COMPETITION_ID = 0;          // int
        int COLUMN_COMPETITION_CAPTION = 1;     // string
        int COLUMN_COMPETITION_LEAGUE = 2;      // string
        int COLUMN_COMPETITION_YEAR = 3;        // string
        int COLUMN_CURRENT_MATCHDAY = 4;        // int
        int COLUMN_NUMBER_MATCHDAYS = 5;        // int
        int COLUMN_NUMBER_TEAMS = 6;            // int
        int COLUMN_NUMBER_GAMES = 7;            // int
        int COLUMN_LAST_UPDATE = 8;             // int from date
        int COLUMN_LAST_REFRESH = 9;            // int from date
    }


    public interface ICpTmEntry {
        int _ID = 0;                            // int
        int COLUMN_COMPETITION_ID = 1;          // int
        int COLUMN_TEAM_ID = 2;                 // int
        int COLUMN_LAST_REFRESH = 3;            // int from date
    }

    public interface ICpFxEntry {
        int _ID = 0;                            // int
        int COLUMN_COMPETITION_ID = 1;          // int
        int COLUMN_FIXTURE_ID = 2;              // int
        int COLUMN_LAST_REFRESH = 3;            // int from date
    }

    public interface ITmEntry {
        int COLUMN_TEAM_ID = 0;                 // int
        int COLUMN_TEAM_NAME = 1;               // string
        int COLUMN_TEAM_CODE = 2;               // string
        int COLUMN_TEAM_SHORT_NAME = 3;         // string
        int COLUMN_TEAM_MARKET_VALUE = 4;       // string
        int COLUMN_TEAM_CREST_URI = 5;          // string
        int COLUMN_LAST_REFRESH = 6;            // int from date
    }

    public interface IFxEntry {
        int COLUMN_FIXTURE_ID = 0;              // int
        int COLUMN_FIXTURE_DATE = 1;            // int from date
        int COLUMN_FIXTURE_STATUS = 2;          // string
        int COLUMN_FIXTURE_MATCHDAY = 3;        // int
        int COLUMN_FIXTURE_TEAM_HOME = 4;       // string
        int COLUMN_FIXTURE_TEAM_AWAY = 5;       // string
        int COLUMN_FIXTURE_GOALS_HOME = 6;      // int
        int COLUMN_FIXTURE_GOALS_AWAY = 7;      // int
        int COLUMN_FIXTURE_ODDS_WIN = 8;        // real
        int COLUMN_FIXTURE_ODDS_DRAW = 9;      // real
        int COLUMN_FIXTURE_ODDS_AWAY = 10;      // real
        int COLUMN_LAST_REFRESH = 11;           // int from date
    }

    public interface ITbEntry {
        int COLUMN_COMPETITION_ID = 0;          // int
        int COLUMN_TEAM_ID = 1;                 // int
        int COLUMN_COMPETITION_MATCHDAY = 2;    // int
        int COLUMN_LEAGUE_CAPTION = 3;          // string
        int COLUMN_TEAM_POSITION = 4;           // int
        int COLUMN_TEAM_NAME = 5;               // string
        int COLUMN_CREST_URI = 6;               // string
        int COLUMN_TEAM_PLAYED_GAMES = 7;       // int
        int COLUMN_TEAM_POINTS = 8;             // int
        int COLUMN_TEAM_GOALS = 9;              // int
        int COLUMN_TEAM_GOALS_AGAINST = 10;     // int
        int COLUMN_TEAM_GOALS_DIFFERENCE = 11;  // int
        int COLUMN_TEAM_WINS = 12;              // int
        int COLUMN_TEAM_DRAWS = 13;             // int
        int COLUMN_TEAM_LOSSES = 14;            // int
        int COLUMN_LAST_REFRESH = 15;           // int from date
    }

    public interface IPlEntry {
        //  int _ID + " INTEGER PRIMARY KEY, " +
        int COLUMN_PLAYER_ID = 0;               // int
        int COLUMN_TEAM_ID = 1;                 // int
        int COLUMN_PLAYER_NAME = 2;             // string
        int COLUMN_PLAYER_POSITION = 3;         // string
        int COLUMN_PLAYER_JERSEY_NUMBER = 4;    // int
        int COLUMN_PLAYER_DATE_BIRTH = 5;       // int from date
        int COLUMN_PLAYER_NATIONALITY = 6;      // string
        int COLUMN_PLAYER_DATE_CONTRACT = 7;    // int from date
        int COLUMN_PLAYER_MARKET_VALUE = 8;     // string
        int COLUMN_LAST_REFRESH = 9;            // int from date
    }

}
