package ru.vpcb.contentprovider.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 24-Jan-18
 * Email: vadim.v.voronov@gmail.com
 */
public class FDTable {
    @SerializedName("_links")
    @Expose
    private FDLinks links;

    @SerializedName("matchday")
    @Expose
    private int matchDay;

    @SerializedName("leagueCaption")
    @Expose
    private String leagueCaption;

    @SerializedName("standing")
    @Expose
    private List<FDStanding> standing;
// cup
    @SerializedName("standings")
    @Expose
    private FDStandingGroup standings;

    private int id;

    private boolean isChampionship;

    public FDTable() {
        this.id = -1;
    }

    public class FDLinks {
        @SerializedName("self")
        @Expose
        private FDLink self;

        @SerializedName("competition")
        @Expose
        private FDLink competition;

    }

    public void setId(int id) throws NumberFormatException {
        this.id = id;
        if (id == -1) throw new NumberFormatException();
    }


    public int getId() {
        return id;
    }

    public List<FDStanding> getStanding() {
        return standing;
    }

    public void setStanding(List<FDStanding> standing) {
        this.standing = standing;
    }

    public FDStandingGroup getStandings() {
        return standings;
    }

    public void setStandings(FDStandingGroup standings) {
        this.standings = standings;
    }

    public boolean isChampionship() {
        return isChampionship;
    }

    public void setChampionship(boolean championship) {
        isChampionship = championship;
    }
}
