package th.melody;

/**
 * 十二平均律
 * C大调的do是 PIANO_c1 中央C，即SPN_C4
 * http://en.wikipedia.org/wiki/Equal_temperament#Twelve-tone_equal_temperament
 */
public class TwelveToneEqualTemperament {

    public static final int SPN_A4_INDEX = 49;
    public static final int SPN_A4_PITCH = 440000; // in milli-Hz

    public static int getPitchBySpnOffset(int semitoneOffset) {
        double f = (SPN_A4_PITCH * Math.pow(2, (semitoneOffset) / 12.0));
        return (int) (f + 0.5);
    }

    public static int getPitchBySpnIndex(int index) {
        return getPitchBySpnOffset(index - SPN_A4_INDEX);
    }

}

/**
 * 纯八度：do-do 极完全协和音程 弦长比2:1
 * 纯五度：do-so 完全协和音程 弦长比3:2
 */
