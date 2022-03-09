import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author zz
 * @date 2022/3/7
 */
public class InArea {

    /**
     * 地球半径
     */
    private static double EARTH_RADIUS = 6378138.0;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 计算是否在圆上（单位/千米）
     *
     * @param radius 半径
     * @param lat1   纬度
     * @param lng1   经度
     * @return double
     * @throws
     * @Title: GetDistance
     * @Description: TODO()
     */
    public static boolean isInCircle(double radius, double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        if (s > radius) {//不在圆上
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        //eg:圆形
        boolean inCircle = isInCircle(2497.2017066855014, 116.390615, 39.933416, 116.405994, 39.916263);
        System.out.println(inCircle);
        //eg:多边形
        // 被检测的经纬度点
        Map<String, String> orderLocation = new HashMap<String, String>();
        orderLocation.put("X", "217.228117");
        orderLocation.put("Y", "31.830429");
// 商业区域（百度多边形区域经纬度集合）
        String partitionLocation = "31.839064_117.219116,31.83253_117.219403,31.828511_117.218146,31.826763_117.219259,31.826118_117.220517,31.822713_117.23586,31.822958_117.238375,31.838512_117.23798,31.839617_117.226194,31.839586_117.222925";
        System.out.println(isInPolygon(orderLocation, partitionLocation));
    }


    /**
     * 是否在矩形区域内
     *
     * @param lat    测试点经度
     * @param lng    测试点纬度
     * @param minLat 纬度范围限制1
     * @param maxLat 纬度范围限制2
     * @param minLng 经度限制范围1
     * @param maxLng 经度范围限制2
     * @return boolean
     * @throws
     * @Title: isInArea
     * @Description: TODO()
     */
    public static boolean isInRectangleArea(double lat, double lng, double minLat,
                                            double maxLat, double minLng, double maxLng) {
        if (isInRange(lat, minLat, maxLat)) {//如果在纬度的范围内
            if (minLng * maxLng > 0) {
                if (isInRange(lng, minLng, maxLng)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (Math.abs(minLng) + Math.abs(maxLng) < 180) {
                    if (isInRange(lng, minLng, maxLng)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    double left = Math.max(minLng, maxLng);
                    double right = Math.min(minLng, maxLng);
                    if (isInRange(lng, left, 180) || isInRange(lng, right, -180)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }


    /**
     * 判断是否在经纬度范围内
     *
     * @param point
     * @param left
     * @param right
     * @return boolean
     * @throws
     * @Title: isInRange
     * @Description: TODO()
     */
    public static boolean isInRange(double point, double left, double right) {
        if (point >= Math.min(left, right) && point <= Math.max(left, right)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断当前位置是否在多边形区域内
     *
     * @param orderLocation     当前点
     * @param partitionLocation 区域顶点
     * @return
     */
    public static boolean isInPolygon(Map orderLocation, String partitionLocation) {

        double p_x = Double.parseDouble((String) orderLocation.get("X"));
        double p_y = Double.parseDouble((String) orderLocation.get("Y"));
        Point2D.Double point = new Point2D.Double(p_x, p_y);

        List<Point2D.Double> pointList = new ArrayList<Point2D.Double>();
        String[] strList = partitionLocation.split(",");

        for (String str : strList) {
            String[] points = str.split("_");
            double polygonPoint_x = Double.parseDouble(points[1]);
            double polygonPoint_y = Double.parseDouble(points[0]);
            Point2D.Double polygonPoint = new Point2D.Double(polygonPoint_x, polygonPoint_y);
            pointList.add(polygonPoint);
        }
        return IsPtInPoly(point, pointList);
    }


    /**
     * 判断点是否在多边形内，如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
     *
     * @param point 检测点
     * @param pts   多边形的顶点
     * @return 点在多边形内返回true, 否则返回false
     */
    public static boolean IsPtInPoly(Point2D.Double point, List<Point2D.Double> pts) {

        int N = pts.size();
        boolean boundOrVertex = true; //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        int intersectCount = 0;//cross points count of x
        double precision = 2e-10; //浮点类型计算时候与0比较时候的容差
        Point2D.Double p1, p2;//neighbour bound vertices
        Point2D.Double p = point; //当前点

        p1 = pts.get(0);//left vertex
        for (int i = 1; i <= N; ++i) {//check all rays
            if (p.equals(p1)) {
                return boundOrVertex;//p is an vertex
            }

            p2 = pts.get(i % N);//right vertex
            if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)) {//ray is outside of our interests
                p1 = p2;
                continue;//next ray left point
            }

            if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)) {//ray is crossing over by the algorithm (common part of)
                if (p.y <= Math.max(p1.y, p2.y)) {//x is before of ray
                    if (p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)) {//overlies on a horizontal ray
                        return boundOrVertex;
                    }

                    if (p1.y == p2.y) {//ray is vertical
                        if (p1.y == p.y) {//overlies on a vertical ray
                            return boundOrVertex;
                        } else {//before ray
                            ++intersectCount;
                        }
                    } else {//cross point on the left side
                        double xinters = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;//cross point of y
                        if (Math.abs(p.y - xinters) < precision) {//overlies on a ray
                            return boundOrVertex;
                        }

                        if (p.y < xinters) {//before ray
                            ++intersectCount;
                        }
                    }
                }
            } else {//special case when ray is crossing through the vertex
                if (p.x == p2.x && p.y <= p2.y) {//p crossing over p2
                    Point2D.Double p3 = pts.get((i + 1) % N); //next vertex
                    if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)) {//p.x lies between p1.x & p3.x
                        ++intersectCount;
                    } else {
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2;//next ray left point
        }

        if (intersectCount % 2 == 0) {//偶数在多边形外
            return false;
        } else { //奇数在多边形内
            return true;
        }
    }

}
