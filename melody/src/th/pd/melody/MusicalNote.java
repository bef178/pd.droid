package th.pd.melody;

/**
 * 声音的三要素
 * 音调：声源的主频率。
 * 响度：声音大小的主观感觉。与振幅有关。与声源远近有关。
 * 音色：声源的主观标识，与谐振/和弦有关。
 * 这个类标识一个音。感觉它应该是个枚举，用属性标识定值，便于序列化。乐曲的属性用方法的参数给出。
 */
public class MusicalNote {

//    private static final int TICKS_PER_BEAT = 32;

    // 频率
//    private int frequency;

    // 音高(主观感受)
//    private int pitch;

    // 音的强度
//    private int strength;

    // 振幅
//    private int amplitude;

    /**
     * 音的时长(时间)
     * 拍是一个相对的概念。每拍具体时长须在乐曲开头规定。常见有60/min，80/min，106/min等。
     * 全音符有4拍，四分音符为1拍，三十二分音符为1/8拍。双附点三十二分音符为1/8+1/16+1/32=7/32拍。
     * 若以1/32拍为单位(tick)，则时长可以表示成整数。
     * see TICKS_PER_BEAT
     */
//    private int tickCount;

}
