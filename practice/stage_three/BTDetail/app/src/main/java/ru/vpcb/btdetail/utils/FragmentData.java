package ru.vpcb.btdetail.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by V1 on 13-Nov-17.
 */

public class FragmentData {
    private static final String[] cards = new String[] {
            "Recipe Green   Card 1",
            "Recipe Blue    Card 2",
            "Recipe Red     Card 3",
            "Recipe Yellow  Card 4",
            "Recipe Brown   Card 5",
            "Recipe Magenta Card 6",
            "Recipe White   Card 7",
            "Recipe Orange  Card 8"
    };

    private static final String[] details = new String[] {
            "Recipe Ingredients",
            "Details Purple Step 1",
            "Details Orange Step 2",
            "Details White  Step 3",
            "Details Red    Step 4",
            "Details Yellow Step 5",
            "Details Green  Step 6",
            "Details Black  Step 7",
            "Details Blue   Step 8"
    };



    public static List<String>  loadMockCards() {

        return new ArrayList<>(Arrays.asList(cards));
    }

    public static List<String>  loadMockDetails() {

        return new ArrayList<>(Arrays.asList(details));
    }
}
