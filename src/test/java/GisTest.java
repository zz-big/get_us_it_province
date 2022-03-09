/**
 * Description:
 *
 * @author zz
 * @date 2022/3/7
 */
public class GisTest {
    public static void main(String[] args) {
        Point[] ps = new Point[] { new Point(120.2043 , 30.2795), new Point(120.2030 , 30.2511), new Point(120.1810 , 30.2543), new Point(120.1798 , 30.2781), new Point(120.1926,30.2752) };
        Point n1 = new Point(120.1936 , 30.2846);
        Point n2 = new Point(120.1823 , 30.2863);
        Point n3 = new Point(120.2189 , 30.2712);
        Point y1 = new Point(120.1902 , 30.2712);
        Point y2 = new Point(120.1866 , 30.2672);
        Point y4 = new Point(120.1869 , 30.2718);
        System.out.println( "n1:" + isPtInPoly(n1.getX() , n1.getY() , ps));
        System.out.println( "n2:" + isPtInPoly(n2.getX() , n2.getY() , ps));
        System.out.println( "n3:" + isPtInPoly(n3.getX() , n3.getY() , ps));
        System.out.println( "y1:" + isPtInPoly(y1.getX() , y1.getY() , ps));
        System.out.println( "y2:" + isPtInPoly(y2.getX() , y2.getY() , ps));
        System.out.println( "y4:" + isPtInPoly(y4.getX() , y4.getY() , ps));
    }
    public static boolean isPtInPoly (double ALon , double ALat , Point[] ps) {
        int iSum, iCount, iIndex;
        double dLon1 = 0, dLon2 = 0, dLat1 = 0, dLat2 = 0, dLon;
        if (ps.length < 3) {
            return false;
        }
        iSum = 0;
        iCount = ps.length;
        for (iIndex = 0; iIndex<iCount;iIndex++) {
            if (iIndex == iCount - 1) {
                dLon1 = ps[iIndex].getX();
                dLat1 = ps[iIndex].getY();
                dLon2 = ps[0].getX();
                dLat2 = ps[0].getY();
            } else {
                dLon1 = ps[iIndex].getX();
                dLat1 = ps[iIndex].getY();
                dLon2 = ps[iIndex + 1].getX();
                dLat2 = ps[iIndex + 1].getY();
            }
            // 以下语句判断A点是否在边的两端点的水平平行线之间，在则可能有交点，开始判断交点是否在左射线上
            if (((ALat >= dLat1) && (ALat < dLat2)) || ((ALat >= dLat2) && (ALat < dLat1))) {
                if (Math.abs(dLat1 - dLat2) > 0) {
                    //得到 A点向左射线与边的交点的x坐标：
                    dLon = dLon1 - ((dLon1 - dLon2) * (dLat1 - ALat) ) / (dLat1 - dLat2);
                    // 如果交点在A点左侧（说明是做射线与 边的交点），则射线与边的全部交点数加一：
                    if (dLon < ALon) {
                        iSum++;
                    }
                }
            }
        }
        if ((iSum % 2) != 0) {
            return true;
        }
        return false;
    }


}
