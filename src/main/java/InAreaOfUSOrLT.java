import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description:
 *
 * @author zz
 * @date 2022/3/7
 */

@Description(name = "get_province_us_lt_udf",
        value = "_FUNC_(cuntryCode,lng,lat) - Returns province name",
        extended = "Example:\n"
                + " > SELECT _FUNC_('us','10.5050949','43.8450298'); \n")
public class InAreaOfUSOrLT extends GenericUDF {
    private static Logger logger = Logger.getLogger(InAreaOfUSOrLT.class);
    private static HashMap<String, String> jsonMap = null;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 3) {
            throw new UDFArgumentException("The operator 'get_province_us_lt_udf' accepts 3 arguments.");
        }
        if (jsonMap == null) {
            try {
                getJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(PrimitiveObjectInspector.PrimitiveCategory.STRING);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object country = arguments[0].get();
        Object lng = arguments[1].get();
        Object lat = arguments[2].get();
        if (null == country || null == lat || null == lng) {
            return null;
        }
        try {
            return getProvincebyLngLat(String.valueOf(country), Double.valueOf(String.valueOf(lng)), Double.valueOf(String.valueOf(lat)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        StringBuilder sb = new StringBuilder();
        sb.append("返回 " + children[1] + "," + children[2] + "所在的省份")
                .append("\n")
                .append("Usage: get_province_us_lt_udf(countryCode,lng,lat)")
                .append("\n")
                .append("省份名");
        return sb.toString();
    }


    public static void main(String[] args) throws JSONException {
        if (jsonMap == null) {
            try {
                getJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(getProvincebyLngLat("US", Double.valueOf("-122.40167"), Double.valueOf("37.80397")));

    }

    private static synchronized String getProvincebyLngLat(String country, Double lng, Double lat) throws JSONException {

        JSONArray jsonArray = new JSONObject(jsonMap.get(country)).getJSONArray("features");

        for (int i = 0; i < jsonArray.length(); i++) {

            String type = jsonArray.getJSONObject(i).getJSONObject("geometry").getString("type");
            JSONArray coordinates = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
            ArrayList<Point> pointsTmp = new ArrayList<>();
            for (int p = 0; p < coordinates.length(); p++) {
                JSONArray coordinates0 = coordinates.getJSONArray(p);
                for (int j = 0; j < coordinates0.length(); j++) {
                    if (type.equals("Polygon")) {
                        pointsTmp.add(new Point(coordinates0.getJSONArray(j).getDouble(0), coordinates0.getJSONArray(j).getDouble(1)));
                    } else {
                        for (int k = 0; k < coordinates0.getJSONArray(j).length(); k++) {
                            pointsTmp.add(new Point(coordinates0.getJSONArray(j).getJSONArray(k).getDouble(0), coordinates0.getJSONArray(j).getJSONArray(k).getDouble(1)));
                        }
                    }
                }
                // System.out.println(name);
                Point[] point = pointsTmp.toArray(new Point[pointsTmp.size()]);
                boolean ptInPoly = isPtInPoly(lng, lat, point);
                if (ptInPoly) {
                    return jsonArray.getJSONObject(i).getJSONObject("properties").getString("name");
                }
            }

        }

        return null;
    }

    private static void getJson() {

        InputStreamReader usIs = new InputStreamReader(InAreaOfUSOrLT.class.getClassLoader().getResourceAsStream("US.json"));
        InputStreamReader itIs = new InputStreamReader(InAreaOfUSOrLT.class.getClassLoader().getResourceAsStream("IT.json"));
        try (BufferedReader usReader = new BufferedReader(usIs); BufferedReader itReader = new BufferedReader(itIs)) {
            String line = "";
            String tmpUs = "";
            String tmpIT = "";
            while ((line = usReader.readLine()) != null) {
                tmpUs += line;
            }
            while ((line = itReader.readLine()) != null) {
                tmpIT += line;
            }
            jsonMap = new HashMap<>();
            jsonMap.put("US", tmpUs);
            jsonMap.put("IT", tmpIT);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    public static boolean isPtInPoly(double ALon, double ALat, Point[] ps) {
        int iSum, iCount, iIndex;
        double dLon1 = 0, dLon2 = 0, dLat1 = 0, dLat2 = 0, dLon;
        if (ps.length < 3) {
            return false;
        }
        iSum = 0;
        iCount = ps.length;
        for (iIndex = 0; iIndex < iCount; iIndex++) {
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
                    dLon = dLon1 - ((dLon1 - dLon2) * (dLat1 - ALat)) / (dLat1 - dLat2);
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
