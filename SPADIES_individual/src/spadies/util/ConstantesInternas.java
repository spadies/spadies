package spadies.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class ConstantesInternas {
  public static /*final*/ Set<Integer> ies_bloqueadas = Collections.unmodifiableSet(new TreeSet<Integer>(
      Arrays.asList(1101,1102,1103,1105,1106,1108,1109,1110,1111,1112,1113,1114,1115,1117,1119,1121,1122,1206,1213,1217,1218,1301,1703,1704,1707,1709,1710,1711,1712,1713,1714,1715,1716,1718,1719,1720,1722,1725,1728,1801,1804,1805,1811,1812,1813,1817,1818,1823,1825,1828,1830,1831,2104,2114,2209,2301,2702,2704,2707,2708,2709,2710,2711,2712,2723,2724,2725,2730,2736,2746,2749,2811,2812,2823,2825,2829,2832,2838,2847,2850,3204,3302,3705,3713,3810,3811,3831,3834,4101,4701,4719,4822)
  ));
  public static /*final*/ Set<Integer> ies_tipo_a = Collections.unmodifiableSet(new TreeSet<Integer>(
      Arrays.asList(1101,1102,1103,1104,1105,1106,1107,1108,1109,1112,1114,1115,1117,1118,1119,1120,1121,1122,1124,1201,1202,1204,1205,1206,1208,1209,1213,1214,1217,1301,1701,1702,1703,1704,1705,1706,1707,1709,1710,1711,1712,1713,1714,1715,1716,1717,1718,1719,1720,1722,1726,1728,1732,1734,1735,1801,1804,1805,1808,1809,1810,1811,1812,1813,1818,181814,18183,1819,1823,1825,1827,1828,1830,1831,1832,1833,2102,2104,2110,2114,2301,2701,2704,2707,2708,2709,2710,2711,2712,2719,2721,2723,2724,2725,2727,2728,2730,2740,2744,2746,2748,2749,2805,2811,2812,2813,2820,2823,2825,2829,2830,2832,2837,2838,2841,2847,2848,2850,3103,3115,3204,3301,3302,3705,3710,3713,3716,3718,3720,3801,3808,3810,3811,3812,3819,3820,3821,3822,3831,3833,3834,4101,4112,4701,4702,4709,4719,4726,4727,4801,4808,4810,4813,4822,4827,4835,4837,5802,9116,9120)
  ));
  public static /*final*/ Set<Integer> ies_tipo_b = Collections.unmodifiableSet(new TreeSet<Integer>(
      Arrays.asList(1125,1203,1207,1212,1219,1220,1221,1222,1223,1702,1717,1729,1814,1816,18181,181812,181813,181814,181815,18182,18183,18185,1820,1826,1833,2102,2106,2207,2211,2713,2731,2732,2740,2744,2815,2824,2833,2840,2842,3117,3201,3702,3703,3716,3725,3806,3807,3817,3820,3826,3828,3830,4107,4108,4111,4708,4801,4806,4817,4818,4825)
  ));
  public static /*final*/ Set<Integer> ies_tipo_c = Collections.unmodifiableSet(new TreeSet<Integer>(
      Arrays.asList(181810,2206,2715,2720,2745,2747,2834,2849,3102,3107,3715,3803,3809,3824,4102,4711,4803,4811,9101)
  ));
  static {
    File f = new File("clasificacion_ies.csv");
    try {
      if (!f.exists()) throw new Throwable("No existe");
      ies_tipo_a = new TreeSet<Integer>();
      ies_tipo_b = new TreeSet<Integer>();
      ies_tipo_c = new TreeSet<Integer>();
      ies_bloqueadas = new TreeSet<Integer>();
      BufferedReader br = new BufferedReader(new FileReader(f));
      for (String s=br.readLine();s!=null;s=br.readLine()) {
        String[] sp = s.split(";");
        int ies = Integer.parseInt(sp[0]);
        String t = sp[1];
        if (t.equals("A"))
          ies_tipo_a.add(ies);
        else if (t.equals("B"))
          ies_tipo_b.add(ies);
        else if (t.equals("C"))
          ies_tipo_c.add(ies);
        else if (t.equals("L"))
          ies_bloqueadas.add(ies);
      }
      br.close();
      System.out.println("Leido "+f.getAbsolutePath());
    } catch (Throwable t) {
      System.out.println("NO Leido "+f.getAbsolutePath());
    }
  }
}